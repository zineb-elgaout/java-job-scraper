package processing;

import java.sql.*;

public class DataQualityFix {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3307/job_scraper";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            Statement stmt = conn.createStatement();
            
            System.out.println("🔧 CORRECTION DE LA QUALITÉ DES DONNÉES");
            System.out.println("═".repeat(50));
            
            // 1. Extraire le télétravail (nouveau)
            extractRemoteWork(conn);
            
            // 2. Améliorer l'extraction des catégories
            improveCategoryExtraction(conn);
            
            // 3. Vérifier la qualité finale
            checkFinalQuality(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void extractRemoteWork(Connection conn) throws SQLException {
        System.out.println("\n🏠 EXTRACTION DU TÉLÉTRAVAIL");
        
        String updateQuery = """
            UPDATE prepared_data 
            SET is_remote = CASE
                WHEN LOWER(description_clean) LIKE '%remote%' THEN TRUE
                WHEN LOWER(description_clean) LIKE '%télétravail%' THEN TRUE
                WHEN LOWER(description_clean) LIKE '%teletravail%' THEN TRUE
                WHEN LOWER(description_clean) LIKE '%home office%' THEN TRUE
                WHEN LOWER(description_clean) LIKE '%travail à distance%' THEN TRUE
                WHEN LOWER(title_clean) LIKE '%remote%' THEN TRUE
                WHEN LOWER(title_clean) LIKE '%télétravail%' THEN TRUE
                ELSE FALSE
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            int updated = stmt.executeUpdate(updateQuery);
            
            String countQuery = "SELECT SUM(is_remote) as remote_count FROM prepared_data";
            ResultSet rs = stmt.executeQuery(countQuery);
            if (rs.next()) {
                System.out.println("✅ " + rs.getInt("remote_count") + " offres en télétravail");
            }
        }
    }
    
    private static void improveCategoryExtraction(Connection conn) throws SQLException {
        System.out.println("\n🏷️ AMÉLIORATION DE LA CLASSIFICATION");
        
        String updateQuery = """
            UPDATE prepared_data pd
            SET category = CASE
                WHEN category = 'Autre' THEN
                    CASE
                        -- IT & TECH
                        WHEN LOWER(pd.title_clean) LIKE '%technicien%' 
                             OR LOWER(pd.description_clean) LIKE '%maintenance%'
                             OR LOWER(pd.title_clean) LIKE '%support%'
                             OR LOWER(pd.title_clean) LIKE '%helpdesk%' THEN 'IT - Support'
                             
                        -- MARKETING & COMMUNICATION
                        WHEN LOWER(pd.title_clean) LIKE '%marketing%'
                             OR LOWER(pd.title_clean) LIKE '%communication%'
                             OR LOWER(pd.title_clean) LIKE '%community%' THEN 'Marketing/Communication'
                             
                        -- SANTÉ & SOCIAL
                        WHEN LOWER(pd.title_clean) LIKE '%infirmier%'
                             OR LOWER(pd.title_clean) LIKE '%médecin%'
                             OR LOWER(pd.description_clean) LIKE '%santé%' THEN 'Santé/Social'
                             
                        -- ÉDUCATION
                        WHEN LOWER(pd.title_clean) LIKE '%enseignant%'
                             OR LOWER(pd.title_clean) LIKE '%professeur%'
                             OR LOWER(pd.title_clean) LIKE '%formateur%' THEN 'Éducation/Formation'
                             
                        -- VENTE & RETAIL
                        WHEN LOWER(pd.title_clean) LIKE '%vendeur%'
                             OR LOWER(pd.title_clean) LIKE '%caissier%'
                             OR LOWER(pd.title_clean) LIKE '%conseiller clientèle%' THEN 'Vente/Retail'
                             
                        -- PRODUCTION & INDUSTRIE
                        WHEN LOWER(pd.title_clean) LIKE '%opérateur%'
                             OR LOWER(pd.title_clean) LIKE '%production%'
                             OR LOWER(pd.title_clean) LIKE '%conducteur%' THEN 'Production/Industrie'
                             
                        ELSE 'Autre'
                    END
                ELSE category
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            int updated = stmt.executeUpdate(updateQuery);
            
            String countQuery = """
                SELECT 
                    COUNT(*) as total,
                    SUM(CASE WHEN category != 'Autre' THEN 1 ELSE 0 END) as classified,
                    COUNT(DISTINCT category) as categories_count
                FROM prepared_data;
                """;
            
            ResultSet rs = stmt.executeQuery(countQuery);
            if (rs.next()) {
                int total = rs.getInt("total");
                int classified = rs.getInt("classified");
                System.out.println("✅ Classification améliorée : " + classified + " offres classées");
                System.out.println("   Soit " + String.format("%.1f", classified * 100.0 / total) + "%");
                System.out.println("   Nombre de catégories : " + rs.getInt("categories_count"));
            }
        }
    }
    
    private static void checkFinalQuality(Connection conn) throws SQLException {
        System.out.println("\n📊 QUALITÉ FINALE DES DONNÉES");
        System.out.println("═".repeat(40));
        
        String query = """
            SELECT 
                COUNT(*) as total_offers,
                COUNT(DISTINCT category) as unique_categories,
                COUNT(DISTINCT location_clean) as unique_locations,
                COUNT(CASE WHEN salary_min IS NOT NULL THEN 1 END) as valid_salaries,
                COUNT(CASE WHEN contract_type != 'Non spécifié' THEN 1 END) as with_contract,
                COUNT(CASE WHEN experience_level != 'Non spécifié' THEN 1 END) as with_experience,
                COUNT(CASE WHEN salary_category != 'Non spécifié' THEN 1 END) as with_salary_info,
                SUM(is_remote) as remote_jobs
            FROM prepared_data;
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int total = rs.getInt("total_offers");
                System.out.println("Total offres : " + total);
                System.out.println("Catégories uniques : " + rs.getInt("unique_categories"));
                System.out.println("Villes uniques : " + rs.getInt("unique_locations"));
                System.out.println("Salaires valides : " + rs.getInt("valid_salaries"));
                System.out.println("Avec type de contrat : " + rs.getInt("with_contract") + 
                    " (" + String.format("%.1f", rs.getInt("with_contract") * 100.0 / total) + "%)");
                System.out.println("Avec niveau d'expérience : " + rs.getInt("with_experience") + 
                    " (" + String.format("%.1f", rs.getInt("with_experience") * 100.0 / total) + "%)");
                System.out.println("Avec info salaire : " + rs.getInt("with_salary_info") + 
                    " (" + String.format("%.1f", rs.getInt("with_salary_info") * 100.0 / total) + "%)");
                System.out.println("Télétravail : " + rs.getInt("remote_jobs") + 
                    " (" + String.format("%.1f", rs.getInt("remote_jobs") * 100.0 / total) + "%)");
            }
        }
    }
}