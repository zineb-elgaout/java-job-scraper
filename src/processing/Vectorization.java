package processing;

import java.sql.*;

public class Vectorization {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3307/job_scraper";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            createMLReadyTable(conn);
            populateMLReadyTable(conn);
            showStatistics(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createMLReadyTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS ml_ready_data");
            
            String createTable = """
                CREATE TABLE ml_ready_data (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    
                    -- TEXTE (pour NLP)
                    title TEXT,
                    description TEXT,
                    company TEXT,
                    
                    -- CATÉGORIES (pour encoding)
                    category VARCHAR(100),
                    contract_type VARCHAR(50),
                    experience_level VARCHAR(50),
                    education_level VARCHAR(50),
                    location VARCHAR(200),
                    salary_category VARCHAR(50),
                    
                    -- NUMÉRIQUES (pour normalisation)
                    salary INT,
                    description_length INT,
                    title_length INT,
                    
                    -- BOOLÉENS
                    has_salary_info BOOLEAN,
                    is_remote BOOLEAN,
                    has_experience BOOLEAN,
                    
                    -- MÉTADONNÉES
                    source VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;
            
            stmt.executeUpdate(createTable);
            System.out.println("✅ Table ml_ready_data créée (format brut)");
        }
    }
    
    private static void populateMLReadyTable(Connection conn) throws SQLException {
        String insertQuery = """
            INSERT INTO ml_ready_data (
                title, description, company,
                category, contract_type, experience_level,
                education_level, location, salary_category,
                salary, description_length, title_length,
                has_salary_info, is_remote, has_experience, source
            )
            SELECT 
                pd.title_clean,
                pd.description_clean,
                pd.company_clean,
                pd.category,
                pd.contract_type,
                pd.experience_level,
                pd.education_level,
                pd.location_clean,
                pd.salary_category,
                pd.salary_min,
                pd.description_length,
                pd.title_length,
                pd.has_salary_info,
                pd.is_remote,
                pd.has_experience,
                pd.source
            FROM prepared_data pd
            WHERE pd.title_clean IS NOT NULL;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            int inserted = stmt.executeUpdate(insertQuery);
            System.out.println("📥 " + inserted + " offres prêtes pour ML");
        }
    }
    
    private static void showStatistics(Connection conn) throws SQLException {
        String query = """
            SELECT 
                COUNT(*) as total,
                COUNT(DISTINCT category) as categories,
                COUNT(DISTINCT location) as locations,
                COUNT(DISTINCT salary_category) as salary_categories,
                COUNT(CASE WHEN salary > 8000 THEN 1 END) as high_salary,
                COUNT(CASE WHEN category LIKE 'IT - %%' THEN 1 END) as tech_jobs,
                SUM(is_remote) as remote_jobs,
                SUM(has_salary_info) as with_salary_info
            FROM ml_ready_data;
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("\n📊 STATISTIQUES FINALES :");
                System.out.println("═".repeat(40));
                System.out.println("Total offres : " + total);
                System.out.println("Catégories uniques : " + rs.getInt("categories"));
                System.out.println("Villes uniques : " + rs.getInt("locations"));
                System.out.println("Catégories salaire : " + rs.getInt("salary_categories"));
                System.out.println("Salaire > 8000 DH : " + rs.getInt("high_salary") + 
                                 " (" + String.format("%.1f", rs.getInt("high_salary")*100.0/total) + "%)");
                System.out.println("Postes techniques : " + rs.getInt("tech_jobs") + 
                                 " (" + String.format("%.1f", rs.getInt("tech_jobs")*100.0/total) + "%)");
                System.out.println("Télétravail : " + rs.getInt("remote_jobs") + 
                                 " (" + String.format("%.1f", rs.getInt("remote_jobs")*100.0/total) + "%)");
                System.out.println("Avec info salaire : " + rs.getInt("with_salary_info") + 
                                 " (" + String.format("%.1f", rs.getInt("with_salary_info")*100.0/total) + "%)");
                System.out.println("═".repeat(40));
            }
        }
    }
}