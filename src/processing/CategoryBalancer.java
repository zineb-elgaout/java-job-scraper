package processing;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class CategoryBalancer {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/job_scraper";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    public static void main(String[] args) {
        System.out.println("⚖️  ÉTAPE 3 : ÉQUILIBRAGE DES CATÉGORIES");
        System.out.println("═".repeat(60));
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            
            // 1. Appliquer la catégorisation équilibrée
            applyBalancedCategorization(conn);
            
            // 2. Normaliser les localisations
            normalizeLocations(conn);
            
            // 3. Analyser les résultats
            analyzeResults(conn);
            
            System.out.println("\n✅ ÉTAPE 3 TERMINÉE : Catégories équilibrées");
            
        } catch (SQLException | IOException e) {
            System.err.println("❌ Erreur lors de l'équilibrage : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void applyBalancedCategorization(Connection conn) throws SQLException {
        System.out.println("\n🔍 APPLICATION DE LA CATÉGORISATION ÉQUILIBRÉE");
        System.out.println("─".repeat(45));
        
        try (Statement stmt = conn.createStatement()) {
            // Créer la table équilibrée
            stmt.executeUpdate("DROP TABLE IF EXISTS jobs_balanced");
            
            String createTable = """
                CREATE TABLE jobs_balanced AS
                SELECT 
                    jf.*,
                    -- Catégorisation PRÉCISE et ÉQUILIBRÉE
                    CASE 
                        -- IT & DÉVELOPPEMENT (spécifique)
                        WHEN (jf.title_clean LIKE '%développeur%' OR 
                              jf.title_clean LIKE '%developer%' OR
                              jf.title_clean LIKE '%programmeur%' OR
                              jf.title_clean LIKE '%software engineer%' OR
                              jf.title_clean LIKE '%ingénieur logiciel%' OR
                              jf.description_clean LIKE '%java%' OR
                              jf.description_clean LIKE '%python%' OR
                              jf.description_clean LIKE '%javascript%' OR
                              jf.description_clean LIKE '%php%' OR
                              jf.title_clean LIKE '%fullstack%' OR
                              jf.title_clean LIKE '%frontend%' OR
                              jf.title_clean LIKE '%backend%' OR
                              jf.title_clean LIKE '%web developer%') 
                              AND jf.title_clean NOT LIKE '%data%'
                              THEN 'IT_DEVELOPPEMENT'
                        
                        -- DATA & IA (très spécifique)
                        WHEN (jf.title_clean LIKE '%data scientist%' OR
                              jf.title_clean LIKE '%data analyst%' OR
                              jf.title_clean LIKE '%machine learning%' OR
                              jf.title_clean LIKE '%intelligence artificielle%' OR
                              jf.title_clean LIKE '%ml engineer%')
                              THEN 'DATA_IA'
                        
                        -- COMMERCIAL & VENTE
                        WHEN jf.title_clean LIKE '%commercial%' OR
                             jf.title_clean LIKE '%vendeur%' OR
                             jf.title_clean LIKE '%sales%' OR
                             jf.title_clean LIKE '%account manager%' OR
                             jf.title_clean LIKE '%business developer%'
                             THEN 'COMMERCIAL_VENTE'
                        
                        -- MARKETING
                        WHEN jf.title_clean LIKE '%marketing%' OR
                             jf.title_clean LIKE '%community manager%' OR
                             jf.title_clean LIKE '%social media%' OR
                             jf.title_clean LIKE '%digital marketing%'
                             THEN 'MARKETING_COM'
                        
                        -- RH
                        WHEN jf.title_clean LIKE '%rh%' OR
                             jf.title_clean LIKE '%ressources humaines%' OR
                             jf.title_clean LIKE '%recruteur%' OR
                             jf.title_clean LIKE '%talent acquisition%'
                             THEN 'RESSOURCES_HUMAINES'
                        
                        -- COMPTABILITÉ
                        WHEN jf.title_clean LIKE '%comptable%' OR
                             jf.title_clean LIKE '%comptabilité%' OR
                             jf.title_clean LIKE '%contrôleur de gestion%' OR
                             jf.title_clean LIKE '%auditeur%'
                             THEN 'COMPTABILITE_FINANCE'
                        
                        -- LOGISTIQUE
                        WHEN jf.title_clean LIKE '%logistique%' OR
                             jf.title_clean LIKE '%supply chain%' OR
                             jf.title_clean LIKE '%transport%' OR
                             jf.title_clean LIKE '%chauffeur%' OR
                             jf.title_clean LIKE '%acheteur%'
                             THEN 'LOGISTIQUE_TRANSPORT'
                        
                        -- PRODUCTION
                        WHEN jf.title_clean LIKE '%technicien%' OR
                             jf.title_clean LIKE '%production%' OR
                             jf.title_clean LIKE '%opérateur%' OR
                             jf.title_clean LIKE '%maintenance%' OR
                             jf.title_clean LIKE '%conducteur%'
                             THEN 'PRODUCTION_INDUSTRIE'
                        
                        -- ADMINISTRATIF
                        WHEN jf.title_clean LIKE '%administratif%' OR
                             jf.title_clean LIKE '%secrétaire%' OR
                             jf.title_clean LIKE '%assistant%' OR
                             jf.title_clean LIKE '%accueil%'
                             THEN 'ADMINISTRATIF'
                        
                        -- INGÉNIERIE (général)
                        WHEN jf.title_clean LIKE '%ingénieur%' AND
                             jf.title_clean NOT LIKE '%ingénieur logiciel%'
                             THEN 'INGENIERIE'
                        
                        -- MANAGEMENT
                        WHEN jf.title_clean LIKE '%manager%' OR
                             jf.title_clean LIKE '%directeur%' OR
                             jf.title_clean LIKE '%responsable%' OR
                             jf.title_clean LIKE '%chef de%'
                             THEN 'MANAGEMENT'
                        
                        -- Par défaut
                        ELSE 'AUTRE'
                    END as balanced_category
                    
                FROM job_features jf;
                """;
            
            stmt.executeUpdate(createTable);
            System.out.println("✅ Table jobs_balanced créée");
        }
    }
    
    private static void normalizeLocations(Connection conn) throws SQLException {
        System.out.println("\n📍 NORMALISATION DES LOCALISATIONS");
        System.out.println("─".repeat(35));
        
        try (Statement stmt = conn.createStatement()) {
            // Mettre à jour les localisations
            String updateQuery = """
                UPDATE jobs_balanced 
                SET location_clean = 
                    CASE 
                        WHEN location_clean LIKE '%casablanca%' THEN 'casablanca'
                        WHEN location_clean LIKE '%rabat%' THEN 'rabat'
                        WHEN location_clean LIKE '%marrakech%' THEN 'marrakech'
                        WHEN location_clean LIKE '%tanger%' THEN 'tanger'
                        WHEN location_clean LIKE '%agadir%' THEN 'agadir'
                        WHEN location_clean LIKE '%fès%' OR location_clean LIKE '%fes%' THEN 'fès'
                        WHEN location_clean LIKE '%mohammedia%' THEN 'mohammedia'
                        WHEN location_clean LIKE '%kenitra%' THEN 'kenitra'
                        WHEN location_clean LIKE '%salé%' OR location_clean LIKE '%sale%' THEN 'salé'
                        WHEN location_clean LIKE '%tétouan%' OR location_clean LIKE '%tetouan%' THEN 'tétouan'
                        WHEN location_clean = 'non_spécifié' THEN 'autre'
                        ELSE location_clean
                    END;
                """;
            
            int updated = stmt.executeUpdate(updateQuery);
            System.out.println("✅ " + updated + " localisations normalisées");
        }
    }
    
    private static void analyzeResults(Connection conn) throws SQLException, IOException {
        System.out.println("\n📊 ANALYSE DES RÉSULTATS");
        System.out.println("─".repeat(25));
        
        try (Statement stmt = conn.createStatement()) {
            // Distribution des catégories
            String query = """
                SELECT 
                    balanced_category,
                    COUNT(*) as count,
                    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM jobs_balanced), 1) as percentage
                FROM jobs_balanced
                GROUP BY balanced_category
                ORDER BY count DESC;
                """;
            
            try (ResultSet rs = stmt.executeQuery(query)) {
                System.out.println("📋 DISTRIBUTION DES CATÉGORIES :");
                System.out.println("─".repeat(45));
                
                int total = 0;
                while (rs.next()) {
                    String category = rs.getString("balanced_category");
                    int count = rs.getInt("count");
                    double percentage = rs.getDouble("percentage");
                    total += count;
                    
                    System.out.printf("  %-25s : %4d offres (%5.1f%%)\n",
                        category, count, percentage);
                }
                
                // Exporter la distribution
                exportDistribution(conn);
            }
            
            // Statistiques par ville
            System.out.println("\n🏙️  TOP 10 VILLES :");
            System.out.println("─".repeat(25));
            
            String citiesQuery = """
                SELECT location_clean, COUNT(*) as count
                FROM jobs_balanced
                WHERE location_clean NOT IN ('non_spécifié', 'autre')
                GROUP BY location_clean
                ORDER BY count DESC
                LIMIT 10;
                """;
            
            try (ResultSet rs = stmt.executeQuery(citiesQuery)) {
                while (rs.next()) {
                    System.out.printf("  %-15s : %4d offres\n",
                        rs.getString("location_clean"),
                        rs.getInt("count"));
                }
            }
        }
    }
    
    private static void exportDistribution(Connection conn) throws SQLException, IOException {
        String query = """
            SELECT balanced_category, COUNT(*) as count
            FROM jobs_balanced
            GROUP BY balanced_category
            ORDER BY count DESC;
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             FileWriter writer = new FileWriter("03_distribution_categories.csv")) {
            
            writer.write("catégorie,nombre_offres\n");
            
            int total = 0;
            while (rs.next()) {
                writer.write(rs.getString("balanced_category") + "," + 
                           rs.getInt("count") + "\n");
                total += rs.getInt("count");
            }
            
            System.out.println("\n💾 Fichier : 03_distribution_categories.csv");
            System.out.println("  " + total + " offres analysées");
        }
    }
}