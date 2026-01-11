package processing;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class CategoryBalancer {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/job_scraper";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static void main(String[] args) {
        System.out.println("⚖️ ÉTAPE 3 : ÉQUILIBRAGE AVANCÉ DES CATÉGORIES");
        System.out.println("═".repeat(70));

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            // 1. Appliquer la catégorisation avancée
            applyAdvancedCategorization(conn);

            // 2. Normaliser les localisations
            normalizeLocationsAdvanced(conn);

            // 3. Analyser et optimiser les résultats
            analyzeAndOptimizeResults(conn);

            // 4. Exporter les résultats finaux
            exportFinalResults(conn);

            System.out.println("\nÉTAPE 3 TERMINÉE : Catégories avancées créées et équilibrées");

        } catch (SQLException | IOException e) {
            System.err.println(" Erreur lors de l'équilibrage : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void applyAdvancedCategorization(Connection conn) throws SQLException {
        System.out.println("\n APPLICATION DE LA CATÉGORISATION AVANCÉE");
        System.out.println("─".repeat(45));

        try (Statement stmt = conn.createStatement()) {
            // Créer la table avancée
            stmt.executeUpdate("DROP TABLE IF EXISTS jobs_advanced_categories");

            String createTable = """
                    CREATE TABLE jobs_advanced_categories AS
                    SELECT
                        jfe.*,

                        -- Catégorie finale optimisée
                        CASE
                            -- Fusionner les catégories spéciales similaires
                            WHEN jfe.enhanced_category LIKE 'SPECIAL_INGENIEUR%' THEN 'INGENIERIE'
                            WHEN jfe.enhanced_category LIKE 'SPECIAL_TECHNICIEN%' THEN 'PRODUCTION_INDUSTRIE'
                            WHEN jfe.enhanced_category LIKE 'SPECIAL_CHEF%' THEN 'MANAGEMENT_DIRECTION'
                            WHEN jfe.enhanced_category LIKE 'SPECIAL_GESTIONNAIRE%' THEN 'COMPTABILITE_FINANCE'
                            WHEN jfe.enhanced_category LIKE 'SPECIAL_ASSISTANT%' THEN 'ADMINISTRATIF_SUPPORT'

                            -- Regrouper les petites catégories
                            WHEN jfe.enhanced_category IN (
                                'HOTELLERIE_RESTAURATION',
                                'BATIMENT_CONSTRUCTION',
                                'SANTE_MEDICAL',
                                'EDUCATION_FORMATION',
                                'JURIDIQUE_DROIT',
                                'TELECOM_RESEAUX',
                                'AUTOMOBILE_MECANIQUE',
                                'VENTE_DETAIL',
                                'IMMOBILIER',
                                'ENERGIE_ENVIRONNEMENT'
                            ) THEN jfe.enhanced_category

                            -- Pour les catégories spéciales restantes, les regrouper
                            WHEN jfe.enhanced_category LIKE 'SPECIAL_%' THEN 'SPECIAL_AUTRES'

                            -- Garder les grandes catégories
                            ELSE jfe.enhanced_category
                        END as final_category,

                        -- Niveau de confiance de la catégorisation
                        CASE
                            WHEN jfe.enhanced_category NOT LIKE 'SPECIAL_%' THEN 'HIGH'
                            WHEN jfe.title_clean LIKE '%ingénieur%' OR
                                 jfe.title_clean LIKE '%technicien%' OR
                                 jfe.title_clean LIKE '%chef%' OR
                                 jfe.title_clean LIKE '%manager%' THEN 'MEDIUM'
                            ELSE 'LOW'
                        END as confidence_level,

                        -- Secteur d'activité basé sur la description
                        CASE
                            WHEN jfe.description_clean LIKE '%finance%' OR
                                 jfe.description_clean LIKE '%banque%' OR
                                 jfe.description_clean LIKE '%crédit%' THEN 'FINANCE_BANQUE'
                            WHEN jfe.description_clean LIKE '%santé%' OR
                                 jfe.description_clean LIKE '%médical%' OR
                                 jfe.description_clean LIKE '%hospital%' THEN 'SANTE'
                            WHEN jfe.description_clean LIKE '%éducation%' OR
                                 jfe.description_clean LIKE '%école%' OR
                                 jfe.description_clean LIKE '%université%' THEN 'EDUCATION'
                            WHEN jfe.description_clean LIKE '%construction%' OR
                                 jfe.description_clean LIKE '%bâtiment%' OR
                                 jfe.description_clean LIKE '%immobilier%' THEN 'BATIMENT'
                            WHEN jfe.description_clean LIKE '%industrie%' OR
                                 jfe.description_clean LIKE '%manufacturing%' OR
                                 jfe.description_clean LIKE '%usine%' THEN 'INDUSTRIE'
                            WHEN jfe.description_clean LIKE '%commerce%' OR
                                 jfe.description_clean LIKE '%retail%' OR
                                 jfe.description_clean LIKE '%magasin%' THEN 'COMMERCE'
                            WHEN jfe.description_clean LIKE '%technologie%' OR
                                 jfe.description_clean LIKE '%it%' OR
                                 jfe.description_clean LIKE '%informatique%' THEN 'TECHNOLOGIE'
                            WHEN jfe.description_clean LIKE '%tourisme%' OR
                                 jfe.description_clean LIKE '%hôtel%' OR
                                 jfe.description_clean LIKE '%restaurant%' THEN 'TOURISME'
                            ELSE 'AUTRE_SECTEUR'
                        END as activity_sector

                    FROM job_features_enhanced jfe;
                    """;

            stmt.executeUpdate(createTable);
            System.out.println("Table jobs_advanced_categories créée");
        }
    }

    private static void normalizeLocationsAdvanced(Connection conn) throws SQLException {
        System.out.println("\n📍 NORMALISATION AVANCÉE DES LOCALISATIONS");
        System.out.println("─".repeat(40));

        try (Statement stmt = conn.createStatement()) {
            // Mettre à jour les localisations avec plus de précision
            String updateQuery = """
                    UPDATE jobs_advanced_categories
                    SET location_clean =
                        CASE
                            WHEN location_clean LIKE '%casablanca%' OR location_clean LIKE '%casa%' THEN 'casablanca'
                            WHEN location_clean LIKE '%rabat%' THEN 'rabat'
                            WHEN location_clean LIKE '%marrakech%' OR location_clean LIKE '%marrakesh%' THEN 'marrakech'
                            WHEN location_clean LIKE '%tanger%' OR location_clean LIKE '%tangier%' THEN 'tanger'
                            WHEN location_clean LIKE '%agadir%' THEN 'agadir'
                            WHEN location_clean LIKE '%fès%' OR location_clean LIKE '%fes%' OR location_clean LIKE '%fès-meknès%' THEN 'fes'
                            WHEN location_clean LIKE '%mohammedia%' THEN 'mohammedia'
                            WHEN location_clean LIKE '%kenitra%' THEN 'kenitra'
                            WHEN location_clean LIKE '%salé%' OR location_clean LIKE '%sale%' THEN 'sale'
                            WHEN location_clean LIKE '%tétouan%' OR location_clean LIKE '%tetouan%' THEN 'tetouan'
                            WHEN location_clean LIKE '%meknès%' OR location_clean LIKE '%meknes%' THEN 'meknes'
                            WHEN location_clean LIKE '%oujda%' THEN 'oujda'
                            WHEN location_clean LIKE '%el jadida%' THEN 'el jadida'
                            WHEN location_clean LIKE '%safi%' THEN 'safi'
                            WHEN location_clean LIKE '%beni mellal%' THEN 'beni mellal'
                            WHEN location_clean LIKE '%nador%' THEN 'nador'
                            WHEN location_clean LIKE '%khouribga%' THEN 'khouribga'
                            WHEN location_clean = 'non_specifie' OR location_clean IS NULL OR location_clean = '' THEN 'non_spécifié'
                            WHEN location_clean LIKE '%maroc%' OR location_clean LIKE '%morocco%' THEN 'maroc'
                            ELSE 'autres_villes'
                        END;
                    """;

            int updated = stmt.executeUpdate(updateQuery);
            System.out.println("" + updated + " localisations normalisées");
        }
    }

    private static void analyzeAndOptimizeResults(Connection conn) throws SQLException {
        System.out.println("\n ANALYSE ET OPTIMISATION DES RÉSULTATS");
        System.out.println("─".repeat(40));

        try (Statement stmt = conn.createStatement()) {
            // Distribution des catégories finales
            String query = """
                    SELECT
                        final_category,
                        COUNT(*) as count,
                        ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM jobs_advanced_categories), 1) as percentage,
                        AVG(CASE WHEN confidence_level = 'HIGH' THEN 1 ELSE 0 END) * 100 as high_confidence_pct
                    FROM jobs_advanced_categories
                    GROUP BY final_category
                    HAVING count >= 3  -- Filtrer les catégories très petites
                    ORDER BY count DESC;
                    """;

            try (ResultSet rs = stmt.executeQuery(query)) {
                System.out.println(" DISTRIBUTION DES CATÉGORIES FINALES :");
                System.out.println("─".repeat(60));
                System.out.printf("%-30s %-10s %-10s %-15s\n", "Catégorie", "Offres", "%", "Confiance Haute");
                System.out.println("-".repeat(60));

                int total = 0;
                while (rs.next()) {
                    String category = rs.getString("final_category");
                    int count = rs.getInt("count");
                    double percentage = rs.getDouble("percentage");
                    double highConfidence = rs.getDouble("high_confidence_pct");
                    total += count;

                    System.out.printf("%-30s %-10d %-9.1f%% %-13.1f%%\n",
                            category, total, percentage, highConfidence);
                }

                // Catégories avec moins de 3 offres
                String smallCatQuery = """
                        SELECT COUNT(DISTINCT final_category) as small_categories_count,
                               SUM(CASE WHEN final_category IN (
                                   SELECT final_category
                                   FROM jobs_advanced_categories
                                   GROUP BY final_category
                                   HAVING COUNT(*) < 3
                               ) THEN 1 ELSE 0 END) as total_small_category_offers
                        FROM jobs_advanced_categories;
                        """;

                try (ResultSet smallRs = stmt.executeQuery(smallCatQuery)) {
                    if (smallRs.next()) {
                        System.out.println("\n PETITES CATÉGORIES (< 3 offres):");
                        System.out.println(
                                "  Nombre de petites catégories : " + smallRs.getInt("small_categories_count"));
                        System.out.println(
                                "  Offres dans petites catégories : " + smallRs.getInt("total_small_category_offers"));
                    }
                }
            }

            // Statistiques par ville
            System.out.println("\n🏙️ TOP 15 VILLES :");
            System.out.println("─".repeat(30));

            String citiesQuery = """
                    SELECT location_clean, COUNT(*) as count,
                           ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM jobs_advanced_categories), 1) as percentage
                    FROM jobs_advanced_categories
                    WHERE location_clean NOT IN ('non_spécifié', 'autres_villes', 'maroc')
                    GROUP BY location_clean
                    ORDER BY count DESC
                    LIMIT 15;
                    """;

            try (ResultSet rs = stmt.executeQuery(citiesQuery)) {
                while (rs.next()) {
                    System.out.printf("  %-20s : %4d offres (%5.1f%%)\n",
                            rs.getString("location_clean"),
                            rs.getInt("count"),
                            rs.getDouble("percentage"));
                }
            }

            // Distribution par secteur d'activité
            System.out.println("\n🏭 DISTRIBUTION PAR SECTEUR D'ACTIVITÉ :");
            System.out.println("─".repeat(40));

            String sectorQuery = """
                    SELECT activity_sector, COUNT(*) as count,
                           ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM jobs_advanced_categories), 1) as percentage
                    FROM jobs_advanced_categories
                    GROUP BY activity_sector
                    ORDER BY count DESC;
                    """;

            try (ResultSet rs = stmt.executeQuery(sectorQuery)) {
                while (rs.next()) {
                    System.out.printf("  %-25s : %4d offres (%5.1f%%)\n",
                            rs.getString("activity_sector"),
                            rs.getInt("count"),
                            rs.getDouble("percentage"));
                }
            }
        }
    }

    private static void exportFinalResults(Connection conn) throws SQLException, IOException {
        System.out.println("\n EXPORT DES RÉSULTATS FINAUX");
        System.out.println("─".repeat(30));

        // Exporter la distribution complète
        String distributionQuery = """
                SELECT final_category, location_clean, activity_sector,
                       confidence_level, COUNT(*) as count
                FROM jobs_advanced_categories
                GROUP BY final_category, location_clean, activity_sector, confidence_level
                ORDER BY final_category, count DESC;
                """;

        exportToCSVAdvanced(conn, distributionQuery, "03_categories_complete_distribution.csv");
        System.out.println("✓ Fichier : 03_categories_complete_distribution.csv");

        // Exporter les données détaillées
        String detailedQuery = """
                SELECT title_clean, company_clean, location_clean,
                       final_category, confidence_level, activity_sector,
                       is_remote, has_contract_info, has_experience_info,
                       has_education_info, has_salary_info, source
                FROM jobs_advanced_categories
                ORDER BY final_category, location_clean;
                """;

        exportToCSVAdvanced(conn, detailedQuery, "03_jobs_categorized_detailed.csv");
        System.out.println("✓ Fichier : 03_jobs_categorized_detailed.csv");

        // Exporter un résumé pour analyse
        String summaryQuery = """
                SELECT
                    final_category,
                    COUNT(*) as total_offers,
                    COUNT(DISTINCT location_clean) as cities_count,
                    ROUND(AVG(is_remote) * 100, 1) as remote_percentage,
                    ROUND(AVG(has_contract_info) * 100, 1) as contract_info_percentage,
                    ROUND(AVG(has_salary_info) * 100, 1) as salary_info_percentage,
                    ROUND(AVG(CASE WHEN confidence_level = 'HIGH' THEN 1 ELSE 0 END) * 100, 1) as high_confidence_percentage
                FROM jobs_advanced_categories
                GROUP BY final_category
                ORDER BY total_offers DESC;
                """;

        exportToCSVAdvanced(conn, summaryQuery, "03_categories_summary.csv");
        System.out.println("✓ Fichier : 03_categories_summary.csv");
    }

    private static void exportToCSVAdvanced(Connection conn, String query, String fileName)
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

            System.out.println("  " + rowCount + " lignes exportées");
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