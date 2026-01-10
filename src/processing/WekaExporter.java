package processing;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class WekaExporter {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/job_scraper";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    public static void main(String[] args) {
        System.out.println("🚀 ÉTAPE 4 : EXPORT POUR WEKA");
        System.out.println("═".repeat(60));
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            
            // 1. Exporter le dataset complet
            exportFullDataset(conn);
            
            // 2. Exporter le dataset simplifié
            exportSimplifiedDataset(conn);
            
            // 3. Générer le fichier ARFF
            generateARFF(conn);
            
            // 4. Générer le guide
            generateGuide();
            
            System.out.println("\n✅ ÉTAPE 4 TERMINÉE : Données prêtes pour Weka !");
            
        } catch (SQLException | IOException e) {
            System.err.println("❌ Erreur lors de l'export : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void exportFullDataset(Connection conn) throws SQLException, IOException {
        System.out.println("\n📦 EXPORT DU DATASET COMPLET");
        System.out.println("─".repeat(30));
        
        String query = """
            SELECT 
                title_clean,
                company_clean,
                location_clean,
                desc_length,
                word_count,
                is_remote,
                has_contract_info,
                has_experience_info,
                has_education_info,
                has_salary_info,
                balanced_category
            FROM jobs_balanced
            WHERE desc_length > 30;
            """;
        
        exportToCSV(conn, query, "04_dataset_complet.csv", true);
        System.out.println("✓ Fichier : 04_dataset_complet.csv");
    }
    
    private static void exportSimplifiedDataset(Connection conn) throws SQLException, IOException {
        System.out.println("\n🎯 EXPORT DU DATASET SIMPLIFIÉ");
        System.out.println("─".repeat(35));
        
        String query = """
            SELECT 
                desc_length,
                word_count,
                is_remote,
                has_education_info,
                has_experience_info,
                CASE location_clean
                    WHEN 'casablanca' THEN 'casablanca'
                    WHEN 'rabat' THEN 'rabat'
                    WHEN 'marrakech' THEN 'marrakech'
                    WHEN 'tanger' THEN 'tanger'
                    WHEN 'agadir' THEN 'agadir'
                    ELSE 'autre'
                END as location,
                balanced_category as category
            FROM jobs_balanced
            WHERE balanced_category IN (
                'IT_DEVELOPPEMENT', 'COMMERCIAL_VENTE', 'MARKETING_COM',
                'RESSOURCES_HUMAINES', 'COMPTABILITE_FINANCE',
                'LOGISTIQUE_TRANSPORT', 'PRODUCTION_INDUSTRIE'
            )
            AND desc_length > 50;
            """;
        
        exportToCSV(conn, query, "05_dataset_simplifié.csv", true);
        System.out.println("✓ Fichier : 05_dataset_simplifié.csv");
    }
    
    private static void generateARFF(Connection conn) throws SQLException, IOException {
        System.out.println("\n🤖 GÉNÉRATION DU FICHIER ARFF");
        System.out.println("─".repeat(30));
        
        try (FileWriter writer = new FileWriter("06_weka_ready.arff")) {
            writer.write("@RELATION offres_emploi_maroc\n\n");
            
            // Attributs
            writer.write("@ATTRIBUTE desc_length NUMERIC\n");
            writer.write("@ATTRIBUTE word_count NUMERIC\n");
            writer.write("@ATTRIBUTE is_remote {0,1}\n");
            writer.write("@ATTRIBUTE has_education {0,1}\n");
            writer.write("@ATTRIBUTE has_experience {0,1}\n");
            writer.write("@ATTRIBUTE location {casablanca,rabat,marrakech,tanger,agadir,fès,mohammedia,autre}\n");
            writer.write("@ATTRIBUTE category {IT_DEVELOPPEMENT,COMMERCIAL_VENTE,MARKETING_COM,RESSOURCES_HUMAINES,COMPTABILITE_FINANCE,LOGISTIQUE_TRANSPORT,PRODUCTION_INDUSTRIE,ADMINISTRATIF,INGENIERIE,MANAGEMENT,DATA_IA,AUTRE}\n\n");
            
            writer.write("@DATA\n");
            
            // Récupérer les données
            String query = """
                SELECT 
                    desc_length,
                    word_count,
                    is_remote,
                    has_education_info,
                    has_experience_info,
                    CASE location_clean
                        WHEN 'casablanca' THEN 'casablanca'
                        WHEN 'rabat' THEN 'rabat'
                        WHEN 'marrakech' THEN 'marrakech'
                        WHEN 'tanger' THEN 'tanger'
                        WHEN 'agadir' THEN 'agadir'
                        WHEN 'fès' THEN 'fès'
                        WHEN 'mohammedia' THEN 'mohammedia'
                        ELSE 'autre'
                    END as location,
                    balanced_category
                FROM jobs_balanced
                WHERE desc_length > 30
                LIMIT 1500;
                """;
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                int count = 0;
                while (rs.next()) {
                    writer.write(rs.getInt("desc_length") + ",");
                    writer.write(rs.getInt("word_count") + ",");
                    writer.write(rs.getInt("is_remote") + ",");
                    writer.write(rs.getInt("has_education_info") + ",");
                    writer.write(rs.getInt("has_experience_info") + ",");
                    writer.write(rs.getString("location") + ",");
                    writer.write(rs.getString("balanced_category") + "\n");
                    
                    count++;
                }
                
                System.out.println("✓ Fichier : 06_weka_ready.arff");
                System.out.println("  " + count + " instances pour Weka");
            }
        }
    }
    
    private static void generateGuide() throws IOException {
        try (FileWriter writer = new FileWriter("07_guide_weka.txt")) {
            writer.write("📚 GUIDE COMPLET POUR WEKA\n");
            writer.write("=".repeat(50) + "\n\n");
            
            writer.write("🎯 FICHIERS GÉNÉRÉS :\n");
            writer.write("1. 01_données_nettoyées.csv - Données brutes nettoyées\n");
            writer.write("2. 02_features_extraits.csv - Features extraites\n");
            writer.write("3. 03_distribution_categories.csv - Statistiques des catégories\n");
            writer.write("4. 04_dataset_complet.csv - Dataset complet\n");
            writer.write("5. 05_dataset_simplifié.csv - Dataset simplifié pour débuter\n");
            writer.write("6. 06_weka_ready.arff - Format ARFF pour Weka\n");
            writer.write("7. 07_guide_weka.txt - Ce guide\n\n");
            
            writer.write("🚀 DÉMARRAGE RAPIDE :\n");
            writer.write("-".repeat(25) + "\n");
            writer.write("1. Ouvrir Weka Explorer\n");
            writer.write("2. Cliquer sur 'Open file'\n");
            writer.write("3. Sélectionner '06_weka_ready.arff'\n");
            writer.write("4. Explorer les données dans 'Preprocess'\n\n");
            
            writer.write("🔧 ANALYSES RECOMMANDÉES :\n");
            writer.write("-".repeat(30) + "\n");
            writer.write("A. CLASSIFICATION :\n");
            writer.write("   • Algorithme : J48 (arbre de décision)\n");
            writer.write("   • Test : 10-fold cross-validation\n");
            writer.write("   • Classe : category\n");
            writer.write("   → Prédit la catégorie d'une offre\n\n");
            
            writer.write("B. CLUSTERING :\n");
            writer.write("   • Algorithme : SimpleKMeans\n");
            writer.write("   • Clusters : 5\n");
            writer.write("   → Regroupe les offres similaires\n\n");
            
            writer.write("C. ASSOCIATION :\n");
            writer.write("   • Algorithme : Apriori\n");
            writer.write("   → Trouve des règles d'association\n\n");
            
            writer.write("📊 CATÉGORIES DISPONIBLES :\n");
            writer.write("-".repeat(30) + "\n");
            writer.write("• IT_DEVELOPPEMENT : Postes IT et développement\n");
            writer.write("• COMMERCIAL_VENTE : Vente et business\n");
            writer.write("• MARKETING_COM : Marketing et communication\n");
            writer.write("• RESSOURCES_HUMAINES : RH et recrutement\n");
            writer.write("• COMPTABILITE_FINANCE : Comptabilité et finance\n");
            writer.write("• LOGISTIQUE_TRANSPORT : Logistique et transport\n");
            writer.write("• PRODUCTION_INDUSTRIE : Production et industrie\n");
            writer.write("• ADMINISTRATIF : Secrétariat et administration\n");
            writer.write("• INGENIERIE : Ingénierie générale\n");
            writer.write("• MANAGEMENT : Management et direction\n");
            writer.write("• DATA_IA : Data science et intelligence artificielle\n");
            writer.write("• AUTRE : Autres métiers\n\n");
            
            writer.write("💡 CONSEILS :\n");
            writer.write("- Commencez par le dataset simplifié (05_dataset_simplifié.csv)\n");
            writer.write("- Visualisez les données avec 'Visualize'\n");
            writer.write("- Exportez les modèles entraînés\n");
            
            System.out.println("✓ Fichier : 07_guide_weka.txt");
        }
    }
    
    private static void exportToCSV(Connection conn, String query, String fileName, boolean showCount) 
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
            
            if (showCount) {
                System.out.println("  " + rowCount + " lignes exportées");
            }
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