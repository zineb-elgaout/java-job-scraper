package processing;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class CleanDataBasic {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/job_scraper";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    public static void main(String[] args) {
        System.out.println("🧹 ÉTAPE 1 : NETTOYAGE DES DONNÉES BRUTES");
        System.out.println("═".repeat(60));
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Statement stmt = conn.createStatement();
            
            // 1. Analyser la table
            analyzeData(conn);
            
            // 2. Nettoyer les données
            cleanData(conn);
            
            // 3. Exporter les données nettoyées
            exportCleanedData(conn);
            
            System.out.println("\n✅ ÉTAPE 1 TERMINÉE : Données nettoyées");
            
        } catch (SQLException | IOException e) {
            System.err.println("❌ Erreur lors du nettoyage : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void analyzeData(Connection conn) throws SQLException {
        System.out.println("\n📊 ANALYSE INITIALE DES DONNÉES");
        System.out.println("─".repeat(40));
        
        String query = """
            SELECT 
                COUNT(*) as total,
                COUNT(DISTINCT title) as unique_titles,
                COUNT(DISTINCT company) as unique_companies,
                COUNT(DISTINCT location) as unique_locations,
                AVG(LENGTH(description)) as avg_desc_length
            FROM jobs;
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                System.out.println("Total offres : " + rs.getInt("total"));
                System.out.println("Titres uniques : " + rs.getInt("unique_titles"));
                System.out.println("Entreprises uniques : " + rs.getInt("unique_companies"));
                System.out.println("Localisations uniques : " + rs.getInt("unique_locations"));
                System.out.println("Longueur moyenne description : " + 
                    String.format("%.0f", rs.getDouble("avg_desc_length")) + " caractères");
            }
        }
    }
    
    private static void cleanData(Connection conn) throws SQLException {
        System.out.println("\n🔧 NETTOYAGE DES DONNÉES");
        System.out.println("─".repeat(30));
        
        try (Statement stmt = conn.createStatement()) {
            // Supprimer l'ancienne table
            stmt.executeUpdate("DROP TABLE IF EXISTS cleaned_jobs");
            
            // Créer la table nettoyée
            String createTable = """
                CREATE TABLE cleaned_jobs AS
                SELECT 
                    -- Nettoyer le titre
                    LOWER(TRIM(title)) as title_clean,
                    
                    -- Nettoyer l'entreprise
                    CASE 
                        WHEN company IS NULL OR TRIM(company) = '' THEN 'non_spécifié'
                        ELSE LOWER(TRIM(company))
                    END as company_clean,
                    
                    -- Nettoyer la localisation
                    CASE 
                        WHEN location IS NULL OR TRIM(location) = '' THEN 'non_spécifié'
                        ELSE LOWER(TRIM(location))
                    END as location_clean,
                    
                    -- Nettoyer la description
                    LOWER(TRIM(description)) as description_clean,
                    
                    -- Métriques
                    LENGTH(description) as desc_length,
                    LENGTH(title) as title_length,
                    CHAR_LENGTH(description) - CHAR_LENGTH(REPLACE(description, ' ', '')) + 1 as word_count,
                    
                    -- Source
                    COALESCE(source, 'scraped') as source,
                    
                    -- Link
                    link
                    
                FROM jobs
                WHERE title IS NOT NULL 
                  AND TRIM(title) != ''
                  AND description IS NOT NULL
                  AND LENGTH(description) > 30;
                """;
            
            stmt.executeUpdate(createTable);
            
            // Compter les résultats
            String countQuery = "SELECT COUNT(*) FROM cleaned_jobs";
            try (ResultSet rs = stmt.executeQuery(countQuery)) {
                if (rs.next()) {
                    System.out.println("✅ " + rs.getInt(1) + " offres nettoyées");
                }
            }
        }
    }
    
    private static void exportCleanedData(Connection conn) throws SQLException, IOException {
        System.out.println("\n💾 EXPORT DES DONNÉES NETTOYÉES");
        System.out.println("─".repeat(35));
        
        exportToCSV(conn, "cleaned_jobs", "01_données_nettoyées.csv");
        System.out.println("✓ Fichier : 01_données_nettoyées.csv");
    }
    
    private static void exportToCSV(Connection conn, String tableName, String fileName) 
            throws SQLException, IOException {
        
        String query = "SELECT * FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             FileWriter writer = new FileWriter(fileName)) {
            
            // En-tête
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
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