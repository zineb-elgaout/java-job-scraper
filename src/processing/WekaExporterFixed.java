package processing;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class WekaExporterFixed {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/job_scraper";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static void main(String[] args) {
        System.out.println("🚀 ÉTAPE 4 : EXPORT POUR WEKA (CORRIGÉ)");
        System.out.println("═".repeat(60));

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            // 1. Exporter le dataset complet
            exportFullDatasetFixed(conn);

            // 2. Exporter le dataset simplifié
            exportSimplifiedDatasetFixed(conn);

            // 3. Générer le fichier ARFF
            generateARFFixed(conn);

            // 4. Générer le guide
            generateGuideFixed();

            System.out.println("\nÉTAPE 4 TERMINÉE : Données prêtes pour Weka !");

        } catch (SQLException | IOException e) {
            System.err.println(" Erreur lors de l'export : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void exportFullDatasetFixed(Connection conn) throws SQLException, IOException {
        System.out.println("\n EXPORT DU DATASET COMPLET");
        System.out.println("─".repeat(30));

        // Utiliser jobs_advanced_categories au lieu de jobs_balanced_fixed
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
                    final_category as balanced_category
                FROM jobs_advanced_categories
                WHERE desc_length > 30;
                """;

        exportToCSV(conn, query, "04_dataset_complet_fixed.csv", true);
        System.out.println("✓ Fichier : 04_dataset_complet_fixed.csv");
    }

    private static void exportSimplifiedDatasetFixed(Connection conn) throws SQLException, IOException {
        System.out.println("\nEXPORT DU DATASET SIMPLIFIÉ");
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
                    final_category as category
                FROM jobs_advanced_categories
                WHERE final_category IN (
                    'IT_DEVELOPPEMENT', 'COMMERCIAL_VENTE', 'MARKETING_COM',
                    'RESSOURCES_HUMAINES', 'COMPTABILITE_FINANCE',
                    'LOGISTIQUE_TRANSPORT', 'PRODUCTION_INDUSTRIE',
                    'ADMINISTRATIF_SUPPORT', 'MANAGEMENT_DIRECTION',
                    'DATA_IA', 'INGENIERIE', 'AUTRE_SECTEUR'
                )
                AND desc_length > 50;
                """;

        exportToCSV(conn, query, "05_dataset_simplifié_fixed.csv", true);
        System.out.println("✓ Fichier : 05_dataset_simplifié_fixed.csv");
    }

    private static void generateARFFixed(Connection conn) throws SQLException, IOException {
        System.out.println("\n🤖 GÉNÉRATION DU FICHIER ARFF CORRIGÉ");
        System.out.println("─".repeat(40));

        try (FileWriter writer = new FileWriter("06_weka_ready_fixed.arff")) {
            writer.write("@RELATION offres_emploi_maroc\n\n");

            // Attributs
            writer.write("@ATTRIBUTE desc_length NUMERIC\n");
            writer.write("@ATTRIBUTE word_count NUMERIC\n");
            writer.write("@ATTRIBUTE is_remote {0,1}\n");
            writer.write("@ATTRIBUTE has_education {0,1}\n");
            writer.write("@ATTRIBUTE has_experience {0,1}\n");
            writer.write(
                    "@ATTRIBUTE location {casablanca,rabat,marrakech,tanger,agadir,fes,mohammedia,sale,kenitra,autre}\n");
            writer.write(
                    "@ATTRIBUTE category {IT_DEVELOPPEMENT,COMMERCIAL_VENTE,MARKETING_COM,RESSOURCES_HUMAINES,COMPTABILITE_FINANCE,LOGISTIQUE_TRANSPORT,PRODUCTION_INDUSTRIE,ADMINISTRATIF_SUPPORT,MANAGEMENT_DIRECTION,DATA_IA,INGENIERIE,SPECIAL_AUTRES,AUTRE_SECTEUR}\n\n");

            writer.write("@DATA\n");

            // Récupérer les données depuis jobs_advanced_categories
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
                            WHEN 'fes' THEN 'fes'
                            WHEN 'mohammedia' THEN 'mohammedia'
                            WHEN 'kenitra' THEN 'kenitra'
                            WHEN 'salé' THEN 'sale'
                            ELSE 'autre'
                        END as location,
                        final_category
                    FROM jobs_advanced_categories
                    WHERE desc_length > 30
                    AND final_category IS NOT NULL
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
                    writer.write(rs.getString("final_category") + "\n");

                    count++;
                }

                System.out.println("✓ Fichier : 06_weka_ready_fixed.arff");
                System.out.println("  " + count + " instances pour Weka");
            }
        }
    }

    // ... (les autres méthodes restent inchangées)
    private static void generateGuideFixed() throws IOException {
        try (FileWriter writer = new FileWriter("07_guide_weka_fixed.txt")) {
            writer.write("📚 GUIDE COMPLET POUR WEKA (VERSION CORRIGÉE)\n");
            writer.write("=".repeat(60) + "\n\n");

            writer.write(" NOTE IMPORTANTE :\n");
            writer.write("La table jobs_balanced_fixed n'existait pas.\n");
            writer.write("Les données sont exportées depuis jobs_advanced_categories\n\n");

            writer.write(" FICHIERS GÉNÉRÉS :\n");
            writer.write("1. 01_données_nettoyées.csv - Données brutes nettoyées\n");
            writer.write("2. 02_features_enriched.csv - Features extraites\n");
            writer.write("3. 03_categories_complete_distribution.csv - Statistiques des catégories\n");
            writer.write("4. 04_dataset_complet_fixed.csv - Dataset complet corrigé\n");
            writer.write("5. 05_dataset_simplifié_fixed.csv - Dataset simplifié pour débuter\n");
            writer.write("6. 06_weka_ready_fixed.arff - Format ARFF corrigé pour Weka\n");
            writer.write("7. 07_guide_weka_fixed.txt - Ce guide\n\n");

            writer.write("🚀 DÉMARRAGE RAPIDE (RECOMMANDÉ) :\n");
            writer.write("-".repeat(40) + "\n");
            writer.write("1. Ouvrir Weka Explorer\n");
            writer.write("2. Cliquer sur 'Open file'\n");
            writer.write("3. Sélectionner '05_dataset_simplifié_fixed.csv'\n");
            writer.write("4. Explorer les données dans 'Preprocess'\n");
            writer.write("5. Pour l'analyse :\n");
            writer.write("   • Onglet 'Classify' → Choisir 'trees > J48'\n");
            writer.write("   • Test option : Cross-validation (folds=10)\n");
            writer.write("   • Cliquez sur 'Start'\n\n");

            writer.write("🔧 ANALYSES RECOMMANDÉES :\n");
            writer.write("-".repeat(30) + "\n");
            writer.write("A. CLASSIFICATION (prédire la catégorie) :\n");
            writer.write("   • J48 (arbre de décision) - Simple à comprendre\n");
            writer.write("   • RandomForest - Plus précis\n");
            writer.write("   • NaiveBayes - Rapide\n\n");

            writer.write("B. CLUSTERING (regrouper les offres) :\n");
            writer.write("   • SimpleKMeans - 5 clusters\n");
            writer.write("   • DBSCAN - Regroupement par densité\n\n");

            writer.write("C. ASSOCIATION (trouver des règles) :\n");
            writer.write("   • Apriori - Règles comme 'IT à Casablanca'\n\n");

            writer.write(" CATÉGORIES DISPONIBLES :\n");
            writer.write("-".repeat(30) + "\n");
            writer.write("• IT_DEVELOPPEMENT : Postes IT et développement\n");
            writer.write("• DATA_IA : Data science et intelligence artificielle\n");
            writer.write("• COMMERCIAL_VENTE : Vente et business\n");
            writer.write("• MARKETING_COM : Marketing et communication\n");
            writer.write("• RESSOURCES_HUMAINES : RH et recrutement\n");
            writer.write("• COMPTABILITE_FINANCE : Comptabilité et finance\n");
            writer.write("• LOGISTIQUE_TRANSPORT : Logistique et transport\n");
            writer.write("• PRODUCTION_INDUSTRIE : Production et industrie\n");
            writer.write("• ADMINISTRATIF_SUPPORT : Secrétariat et administration\n");
            writer.write("• MANAGEMENT_DIRECTION : Management et direction\n");
            writer.write("• AUTRE_SECTEUR : Autres métiers\n\n");

            writer.write(" CONSEILS PRATIQUES :\n");
            writer.write("-".repeat(25) + "\n");
            writer.write("1. Commencez toujours par visualiser les données\n");
            writer.write("2. Testez plusieurs algorithmes pour comparer\n");
            writer.write("3. Exportez les modèles entraînés\n");
            writer.write("4. Utilisez la validation croisée\n");
            writer.write("5. Consultez la matrice de confusion\n\n");

            writer.write("📞 EN CAS DE PROBLÈME :\n");
            writer.write("-".repeat(25) + "\n");
            writer.write("1. Vérifiez que Weka est à jour\n");
            writer.write("2. Assurez-vous d'avoir assez de mémoire\n");
            writer.write("3. Pour les gros fichiers, augmentez la mémoire\n");
            writer.write("   java -Xmx2g -jar weka.jar\n");

            System.out.println("✓ Fichier : 07_guide_weka_fixed.txt");
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
                if (i < columnCount)
                    writer.append(",");
            }
            writer.append("\n");

            // Données
            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    writer.append(escapeCSV(value));
                    if (i < columnCount)
                        writer.append(",");
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
        if (value == null)
            return "";
        value = value.replace("\"", "\"\"");
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = "\"" + value + "\"";
        }
        return value;
    }
}