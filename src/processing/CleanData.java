package processing;

import java.sql.*;

public class CleanData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3307/job_scraper";
        String user = "root";
        String password = "";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            Statement stmt = conn.createStatement();
            
            stmt.executeUpdate("DROP TABLE IF EXISTS clean_data");
            System.out.println("✅ Table clean_data supprimée si existante");
            
            String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                          "WHERE TABLE_NAME = 'jobs' AND TABLE_SCHEMA = 'job_scraper'";
            
            StringBuilder columns = new StringBuilder();
            int totalColumns = 0;
            int nonEmptyColumns = 0;
            
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    totalColumns++;
                    String columnName = rs.getString("COLUMN_NAME");
                    
                    String checkQuery = "SELECT COUNT(*) FROM jobs WHERE " + 
                                       columnName + " IS NOT NULL AND TRIM(" + columnName + ") != ''";
                    
                    try (Statement checkStmt = conn.createStatement();
                         ResultSet checkRs = checkStmt.executeQuery(checkQuery)) {
                        
                        if (checkRs.next() && checkRs.getInt(1) > 0) {
                            if (columns.length() > 0) {
                                columns.append(", ");
                            }
                            columns.append(columnName);
                            nonEmptyColumns++;
                        }
                    }
                }
            }
            
            System.out.println("📊 Analyse terminée :");
            System.out.println("   - Colonnes totales dans jobs : " + totalColumns);
            System.out.println("   - Colonnes non vides : " + nonEmptyColumns);
            
            if (columns.length() > 0) {
                String createTableQuery = "CREATE TABLE clean_data AS " +
                                         "SELECT DISTINCT " + columns + " " +
                                         "FROM jobs " +
                                         "WHERE link IS NOT NULL AND TRIM(link) != '' " +
                                         "GROUP BY link";
                
                stmt.executeUpdate(createTableQuery);
                System.out.println("✅ Table clean_data créée avec succès");
                
                String countQuery = "SELECT COUNT(*) FROM clean_data";
                try (ResultSet countRs = stmt.executeQuery(countQuery)) {
                    if (countRs.next()) {
                        System.out.println("📋 Offres nettoyées : " + countRs.getInt(1));
                    }
                }
                
                stmt.executeUpdate("ALTER TABLE clean_data ADD INDEX idx_link (link)");
                System.out.println("🔍 Index ajouté sur la colonne 'link'");
                
                System.out.println("📋 Colonnes conservées : " + columns.toString());
                
            } else {
                System.out.println("⚠️ Aucune colonne non vide trouvée dans la table jobs");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du nettoyage : " + e.getMessage());
            e.printStackTrace();
        }
    }
}