package processing;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class FeatureExtractorBasic {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/job_scraper";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    public static void main(String[] args) {
        System.out.println("🔍 ÉTAPE 2 : EXTRACTION DES CARACTÉRISTIQUES");
        System.out.println("═".repeat(60));
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Statement stmt = conn.createStatement();
            
            // 1. Créer la table de features
            createFeaturesTable(conn);
            
            // 2. Extraire les features
            extractFeatures(conn);
            
            // 3. Exporter les features
            exportFeatures(conn);
            
            System.out.println("\n✅ ÉTAPE 2 TERMINÉE : Features extraites");
            
        } catch (SQLException | IOException e) {
            System.err.println("❌ Erreur lors de l'extraction : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createFeaturesTable(Connection conn) throws SQLException {
        System.out.println("\n📊 CRÉATION DE LA TABLE DE FEATURES");
        System.out.println("─".repeat(40));
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS job_features");
            
            String createTable = """
                CREATE TABLE job_features AS
                SELECT 
                    cj.*,
                    
                    -- Features booléennes
                    CASE 
                        WHEN cj.description_clean LIKE '%remote%' OR 
                             cj.description_clean LIKE '%télétravail%' OR
                             cj.description_clean LIKE '%teletravail%' OR
                             cj.description_clean LIKE '%home office%' THEN 1
                        ELSE 0
                    END as is_remote,
                    
                    CASE 
                        WHEN cj.description_clean LIKE '%cdi%' THEN 1
                        WHEN cj.description_clean LIKE '%cdd%' THEN 1
                        WHEN cj.description_clean LIKE '%stage%' THEN 1
                        WHEN cj.description_clean LIKE '%alternance%' THEN 1
                        ELSE 0
                    END as has_contract_info,
                    
                    CASE 
                        WHEN cj.description_clean LIKE '%junior%' OR 
                             cj.description_clean LIKE '%débutant%' THEN 1
                        WHEN cj.description_clean LIKE '%senior%' OR 
                             cj.description_clean LIKE '%expérimenté%' THEN 1
                        ELSE 0
                    END as has_experience_info,
                    
                    CASE 
                        WHEN cj.description_clean LIKE '%bac%' OR 
                             cj.description_clean LIKE '%licence%' OR
                             cj.description_clean LIKE '%master%' OR
                             cj.description_clean LIKE '%diplôme%' THEN 1
                        ELSE 0
                    END as has_education_info,
                    
                    CASE 
                        WHEN cj.description_clean LIKE '%salaire%' OR 
                             cj.description_clean LIKE '%rémunération%' OR
                             cj.description_clean LIKE '%dh%' OR
                             cj.description_clean LIKE '%mad%' THEN 1
                        ELSE 0
                    END as has_salary_info,
                    
                    -- Catégorie basique (sera améliorée plus tard)
                    'AUTRE' as basic_category
                    
                FROM cleaned_jobs cj;
                """;
            
            stmt.executeUpdate(createTable);
            System.out.println("✅ Table job_features créée");
        }
    }
    
    private static void extractFeatures(Connection conn) throws SQLException {
        System.out.println("\n🔧 EXTRACTION DES FEATURES");
        System.out.println("─".repeat(30));
        
        try (Statement stmt = conn.createStatement()) {
            // Mettre à jour les catégories basiques
            String updateCategories = """
                UPDATE job_features 
                SET basic_category = 
                    CASE 
                        -- IT & Développement
                        WHEN title_clean LIKE '%développeur%' OR 
                             title_clean LIKE '%developer%' OR
                             title_clean LIKE '%programmeur%' OR
                             description_clean LIKE '%java%' OR
                             description_clean LIKE '%python%' OR
                             description_clean LIKE '%javascript%' OR
                             description_clean LIKE '%php%' OR
                             title_clean LIKE '%fullstack%' OR
                             title_clean LIKE '%frontend%' OR
                             title_clean LIKE '%backend%' THEN 'IT_DEVELOPPEMENT'
                        
                        -- Commercial
                        WHEN title_clean LIKE '%commercial%' OR
                             title_clean LIKE '%vendeur%' OR
                             title_clean LIKE '%sales%' OR
                             title_clean LIKE '%business developer%' THEN 'COMMERCIAL_VENTE'
                        
                        -- Marketing
                        WHEN title_clean LIKE '%marketing%' OR
                             title_clean LIKE '%community manager%' OR
                             title_clean LIKE '%digital marketing%' THEN 'MARKETING_COM'
                        
                        -- RH
                        WHEN title_clean LIKE '%rh%' OR
                             title_clean LIKE '%ressources humaines%' OR
                             title_clean LIKE '%recruteur%' THEN 'RESSOURCES_HUMAINES'
                        
                        -- Comptabilité
                        WHEN title_clean LIKE '%comptable%' OR
                             title_clean LIKE '%comptabilité%' THEN 'COMPTABILITE_FINANCE'
                        
                        -- Logistique
                        WHEN title_clean LIKE '%logistique%' OR
                             title_clean LIKE '%supply chain%' OR
                             title_clean LIKE '%transport%' THEN 'LOGISTIQUE_TRANSPORT'
                        
                        -- Production
                        WHEN title_clean LIKE '%technicien%' OR
                             title_clean LIKE '%production%' OR
                             title_clean LIKE '%maintenance%' THEN 'PRODUCTION_INDUSTRIE'
                        
                        -- Par défaut
                        ELSE 'AUTRE'
                    END;
                """;
            
            int updated = stmt.executeUpdate(updateCategories);
            System.out.println("✅ " + updated + " offres catégorisées");
            
            // Statistiques
            showFeatureStats(conn);
        }
    }
    
    private static void showFeatureStats(Connection conn) throws SQLException {
        String query = """
            SELECT 
                COUNT(*) as total,
                SUM(is_remote) as remote_count,
                SUM(has_contract_info) as contract_count,
                SUM(has_experience_info) as experience_count,
                SUM(has_education_info) as education_count,
                SUM(has_salary_info) as salary_count,
                COUNT(DISTINCT basic_category) as categories_count
            FROM job_features;
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("\n📈 STATISTIQUES DES FEATURES :");
                System.out.println("─".repeat(40));
                System.out.println("Total : " + total);
                System.out.println("Télétravail : " + rs.getInt("remote_count") + 
                    " (" + String.format("%.1f", rs.getInt("remote_count") * 100.0 / total) + "%)");
                System.out.println("Contrat mentionné : " + rs.getInt("contract_count") + 
                    " (" + String.format("%.1f", rs.getInt("contract_count") * 100.0 / total) + "%)");
                System.out.println("Expérience mentionnée : " + rs.getInt("experience_count") + 
                    " (" + String.format("%.1f", rs.getInt("experience_count") * 100.0 / total) + "%)");
                System.out.println("Éducation mentionnée : " + rs.getInt("education_count") + 
                    " (" + String.format("%.1f", rs.getInt("education_count") * 100.0 / total) + "%)");
                System.out.println("Salaire mentionné : " + rs.getInt("salary_count") + 
                    " (" + String.format("%.1f", rs.getInt("salary_count") * 100.0 / total) + "%)");
                System.out.println("Catégories détectées : " + rs.getInt("categories_count"));
            }
        }
    }
    
    private static void exportFeatures(Connection conn) throws SQLException, IOException {
        System.out.println("\n💾 EXPORT DES FEATURES");
        System.out.println("─".repeat(25));
        
        String query = "SELECT * FROM job_features";
        exportToCSV(conn, query, "02_features_extraits.csv");
        System.out.println("✓ Fichier : 02_features_extraits.csv");
    }
    
    private static void exportToCSV(Connection conn, String query, String fileName) 
            throws SQLException, IOException {
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             FileWriter writer = new FileWriter(fileName)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // En-tête
            for (int i = 1; i <= columnCount; i++) {
                writer.append(metaData.getColumnName(i));
                if (i < columnCount) writer.append(",");
            }
            writer.append("\n");
            
            // Données
            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    writer.append(escapeCSV(value));
                    if (i < columnCount) writer.append(",");
                }
                writer.append("\n");
                rowCount++;
            }
            
            System.out.println("  " + rowCount + " lignes exportées");
        }
    }
    
    private static String escapeCSV(String value) {
        if (value == null) return "";
        value = value.replace("\"", "\"\"");
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = "\"" + value + "\"";
        }
        return value;
    }
}