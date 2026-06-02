package processing;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FeatureExtractorEnhanced {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/job_scraper";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static void main(String[] args) {
        System.out.println(" ÉTAPE 2 : EXTRACTION DES CARACTÉRISTIQUES AMÉLIORÉE");
        System.out.println("═".repeat(70));

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            // 1. Créer la table de features avec catégories enrichies
            createEnhancedFeaturesTable(conn);

            // 2. Exporter les features
            exportFeaturesEnhanced(conn);

            System.out.println("\nÉTAPE 2 TERMINÉE : Features extraites avec catégories enrichies");

        } catch (SQLException | IOException e) {
            System.err.println(" Erreur lors de l'extraction : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createEnhancedFeaturesTable(Connection conn) throws SQLException {
        System.out.println("\n CRÉATION DE LA TABLE DE FEATURES ENRICHIE");
        System.out.println("─".repeat(50));

        try (Statement stmt = conn.createStatement()) {
            // Supprimer l'ancienne table
            stmt.executeUpdate("DROP TABLE IF EXISTS job_features_enhanced");

            // Créer la table avec catégories enrichies
            // Note: Utilisation de doubles apostrophes pour échapper les apostrophes
            // simples dans SQL
            String createTable = """
                    CREATE TABLE job_features_enhanced AS
                    SELECT
                        cj.title_clean,
                        cj.company_clean,
                        cj.location_clean,
                        cj.description_clean,
                        cj.source,
                        cj.desc_length,
                        cj.title_length,
                        cj.word_count,

                        -- Features booléennes directes
                        CASE
                            WHEN cj.description_clean LIKE '%remote%' OR
                                 cj.description_clean LIKE '%télétravail%' OR
                                 cj.description_clean LIKE '%teletravail%' OR
                                 cj.description_clean LIKE '%home office%' OR
                                 cj.description_clean LIKE '%travail à distance%' THEN 1
                            ELSE 0
                        END as is_remote,

                        CASE
                            WHEN cj.description_clean LIKE '%cdi%' OR cj.title_clean LIKE '%cdi%' THEN 1
                            WHEN cj.description_clean LIKE '%cdd%' OR cj.title_clean LIKE '%cdd%' THEN 1
                            WHEN cj.description_clean LIKE '%stage%' OR cj.title_clean LIKE '%stage%' THEN 1
                            WHEN cj.description_clean LIKE '%alternance%' OR cj.title_clean LIKE '%alternance%' THEN 1
                            WHEN cj.description_clean LIKE '%intérim%' OR cj.title_clean LIKE '%intérim%' THEN 1
                            WHEN cj.description_clean LIKE '%freelance%' OR cj.title_clean LIKE '%freelance%' THEN 1
                            ELSE 0
                        END as has_contract_info,

                        CASE
                            WHEN cj.description_clean LIKE '%junior%' OR
                                 cj.description_clean LIKE '%débutant%' OR
                                 cj.description_clean LIKE '%jeune diplômé%' OR
                                 cj.description_clean LIKE '%première expérience%' THEN 1
                            WHEN cj.description_clean LIKE '%senior%' OR
                                 cj.description_clean LIKE '%expérimenté%' OR
                                 cj.description_clean LIKE '%confirmé%' OR
                                 cj.description_clean LIKE '%expert%' THEN 1
                            ELSE 0
                        END as has_experience_info,

                        CASE
                            WHEN cj.description_clean LIKE '%bac%' OR
                                 cj.description_clean LIKE '%licence%' OR
                                 cj.description_clean LIKE '%master%' OR
                                 cj.description_clean LIKE '%diplôme%' OR
                                 cj.description_clean LIKE '%bts%' OR
                                 cj.description_clean LIKE '%dut%' OR
                                 cj.description_clean LIKE '%ingénieur%' OR
                                 cj.description_clean LIKE '%doctorat%' THEN 1
                            ELSE 0
                        END as has_education_info,

                        CASE
                            WHEN cj.description_clean LIKE '%salaire%' OR
                                 cj.description_clean LIKE '%rémunération%' OR
                                 cj.description_clean LIKE '%dh%' OR
                                 cj.description_clean LIKE '%mad%' OR
                                 cj.description_clean LIKE '%euro%' OR
                                 cj.description_clean LIKE '%€%' OR
                                 cj.description_clean LIKE '%package%' OR
                                 cj.description_clean LIKE '%compensation%' THEN 1
                            ELSE 0
                        END as has_salary_info,

                        -- Catégorie enrichie avec beaucoup plus de mots-clés
                        CASE
                            -- IT & Développement
                            WHEN cj.title_clean LIKE '%développeur%' OR
                                 cj.title_clean LIKE '%developer%' OR
                                 cj.title_clean LIKE '%programmeur%' OR
                                 cj.title_clean LIKE '%software%' OR
                                 cj.title_clean LIKE '%ingénieur logiciel%' OR
                                 cj.title_clean LIKE '%codeur%' OR
                                 cj.description_clean LIKE '%java%' OR
                                 cj.description_clean LIKE '%python%' OR
                                 cj.description_clean LIKE '%javascript%' OR
                                 cj.description_clean LIKE '%php%' OR
                                 cj.description_clean LIKE '%c++%' OR
                                 cj.description_clean LIKE '%c#%' OR
                                 cj.description_clean LIKE '%ruby%' OR
                                 cj.description_clean LIKE '%sql%' OR
                                 cj.description_clean LIKE '%node.js%' OR
                                 cj.description_clean LIKE '%react%' OR
                                 cj.description_clean LIKE '%angular%' OR
                                 cj.description_clean LIKE '%vue%' OR
                                 cj.description_clean LIKE '%spring%' OR
                                 cj.title_clean LIKE '%fullstack%' OR
                                 cj.title_clean LIKE '%frontend%' OR
                                 cj.title_clean LIKE '%front-end%' OR
                                 cj.title_clean LIKE '%backend%' OR
                                 cj.title_clean LIKE '%back-end%' OR
                                 cj.title_clean LIKE '%web developer%' OR
                                 cj.title_clean LIKE '%mobile developer%' OR
                                 cj.title_clean LIKE '%android%' OR
                                 cj.title_clean LIKE '%ios%' THEN 'IT_DEVELOPPEMENT'

                            -- DATA & IA
                            WHEN cj.title_clean LIKE '%data scientist%' OR
                                 cj.title_clean LIKE '%data analyst%' OR
                                 cj.title_clean LIKE '%data engineer%' OR
                                 cj.title_clean LIKE '%machine learning%' OR
                                 cj.title_clean LIKE '%deep learning%' OR
                                 cj.title_clean LIKE '%intelligence artificielle%' OR
                                 cj.title_clean LIKE '%ai%' OR
                                 cj.title_clean LIKE '%business intelligence%' OR
                                 cj.title_clean LIKE '%bi%' OR
                                 cj.title_clean LIKE '%big data%' OR
                                 cj.title_clean LIKE '%analyste données%' OR
                                 cj.title_clean LIKE '%statisticien%' OR
                                 cj.description_clean LIKE '%tensorflow%' OR
                                 cj.description_clean LIKE '%pytorch%' OR
                                 cj.description_clean LIKE '%tableau%' OR
                                 cj.description_clean LIKE '%power bi%' THEN 'DATA_IA'

                            -- Base de données & Administration système
                            WHEN cj.title_clean LIKE '%administrateur base de données%' OR
                                 cj.title_clean LIKE '%dba%' OR
                                 cj.title_clean LIKE '%administrateur système%' OR
                                 cj.title_clean LIKE '%sysadmin%' OR
                                 cj.title_clean LIKE '%admin réseau%' OR
                                 cj.title_clean LIKE '%network%' OR
                                 cj.title_clean LIKE '%devops%' OR
                                 cj.title_clean LIKE '%sre%' OR
                                 cj.title_clean LIKE '%cloud%' OR
                                 cj.title_clean LIKE '%aws%' OR
                                 cj.title_clean LIKE '%azure%' OR
                                 cj.title_clean LIKE '%google cloud%' OR
                                 cj.title_clean LIKE '%oracle%' OR
                                 cj.title_clean LIKE '%sql server%' OR
                                 cj.title_clean LIKE '%mysql%' OR
                                 cj.title_clean LIKE '%postgresql%' OR
                                 cj.title_clean LIKE '%mongodb%' THEN 'ADMIN_SYSTEME_CLOUD'

                            -- Commercial & Vente
                            WHEN cj.title_clean LIKE '%commercial%' OR
                                 cj.title_clean LIKE '%vendeur%' OR
                                 cj.title_clean LIKE '%sales%' OR
                                 cj.title_clean LIKE '%business developer%' OR
                                 cj.title_clean LIKE '%account manager%' OR
                                 cj.title_clean LIKE '%chargé d''affaires%' OR  -- Apostrophe échappée
                                 cj.title_clean LIKE '%conseiller commercial%' OR
                                 cj.title_clean LIKE '%téléconseiller%' OR
                                 cj.title_clean LIKE '%agent commercial%' OR
                                 cj.title_clean LIKE '%représentant%' OR
                                 cj.title_clean LIKE '%business development%' OR
                                 cj.title_clean LIKE '%key account%' OR
                                 cj.title_clean LIKE '%vente%' THEN 'COMMERCIAL_VENTE'

                            -- Marketing & Communication
                            WHEN cj.title_clean LIKE '%marketing%' OR
                                 cj.title_clean LIKE '%community manager%' OR
                                 cj.title_clean LIKE '%social media%' OR
                                 cj.title_clean LIKE '%digital marketing%' OR
                                 cj.title_clean LIKE '%chargé marketing%' OR
                                 cj.title_clean LIKE '%communication%' OR
                                 cj.title_clean LIKE '%content manager%' OR
                                 cj.title_clean LIKE '%brand manager%' OR
                                 cj.title_clean LIKE '%seo%' OR
                                 cj.title_clean LIKE '%sea%' OR
                                 cj.title_clean LIKE '%traffic manager%' OR
                                 cj.title_clean LIKE '%publicité%' OR
                                 cj.title_clean LIKE '%media planner%' THEN 'MARKETING_COM'

                            -- RH & Recrutement
                            WHEN cj.title_clean LIKE '%rh%' OR
                                 cj.title_clean LIKE '%ressources humaines%' OR
                                 cj.title_clean LIKE '%recruteur%' OR
                                 cj.title_clean LIKE '%talent acquisition%' OR
                                 cj.title_clean LIKE '%hr%' OR
                                 cj.title_clean LIKE '%responsable rh%' OR
                                 cj.title_clean LIKE '%gestionnaire paie%' OR
                                 cj.title_clean LIKE '%chargé recrutement%' OR
                                 cj.title_clean LIKE '%formation%' OR
                                 cj.title_clean LIKE '%développement personnel%' OR
                                 cj.title_clean LIKE '%relations sociales%' THEN 'RESSOURCES_HUMAINES'

                            -- Comptabilité & Finance
                            WHEN cj.title_clean LIKE '%comptable%' OR
                                 cj.title_clean LIKE '%comptabilité%' OR
                                 cj.title_clean LIKE '%contrôleur de gestion%' OR
                                 cj.title_clean LIKE '%auditeur%' OR
                                 cj.title_clean LIKE '%finance%' OR
                                 cj.title_clean LIKE '%analyste financier%' OR
                                 cj.title_clean LIKE '%gestionnaire%' OR
                                 cj.title_clean LIKE '%trésorerie%' OR
                                 cj.title_clean LIKE '%credit manager%' OR
                                 cj.title_clean LIKE '%risk manager%' OR
                                 cj.title_clean LIKE '%accountant%' OR
                                 cj.title_clean LIKE '%audit%' THEN 'COMPTABILITE_FINANCE'

                            -- Logistique & Transport
                            WHEN cj.title_clean LIKE '%logistique%' OR
                                 cj.title_clean LIKE '%supply chain%' OR
                                 cj.title_clean LIKE '%transport%' OR
                                 cj.title_clean LIKE '%chauffeur%' OR
                                 cj.title_clean LIKE '%acheteur%' OR
                                 cj.title_clean LIKE '%gestionnaire logistique%' OR
                                 cj.title_clean LIKE '%responsable logistique%' OR
                                 cj.title_clean LIKE '%planificateur%' OR
                                 cj.title_clean LIKE '%expédition%' OR
                                 cj.title_clean LIKE '%entreposage%' OR
                                 cj.title_clean LIKE '%stock%' OR
                                 cj.title_clean LIKE '%inventaire%' OR
                                 cj.title_clean LIKE '%livraison%' OR
                                 cj.title_clean LIKE '%douane%' THEN 'LOGISTIQUE_TRANSPORT'

                            -- Production & Industrie
                            WHEN cj.title_clean LIKE '%technicien%' OR
                                 cj.title_clean LIKE '%production%' OR
                                 cj.title_clean LIKE '%opérateur%' OR
                                 cj.title_clean LIKE '%maintenance%' OR
                                 cj.title_clean LIKE '%conducteur%' OR
                                 cj.title_clean LIKE '%ingénieur production%' OR
                                 cj.title_clean LIKE '%technicien qualité%' OR
                                 cj.title_clean LIKE '%qualité%' OR
                                 cj.title_clean LIKE '%contrôle qualité%' OR
                                 cj.title_clean LIKE '%hse%' OR
                                 cj.title_clean LIKE '%sécurité%' OR
                                 cj.title_clean LIKE '%mécanicien%' OR
                                 cj.title_clean LIKE '%électricien%' OR
                                 cj.title_clean LIKE '%maintenance industrielle%' THEN 'PRODUCTION_INDUSTRIE'

                            -- Administratif & Support
                            WHEN cj.title_clean LIKE '%administratif%' OR
                                 cj.title_clean LIKE '%secrétaire%' OR
                                 cj.title_clean LIKE '%assistant%' OR
                                 cj.title_clean LIKE '%accueil%' OR
                                 cj.title_clean LIKE '%standardiste%' OR
                                 cj.title_clean LIKE '%réceptionniste%' OR
                                 cj.title_clean LIKE '%assistante%' OR
                                 cj.title_clean LIKE '%office manager%' OR
                                 cj.title_clean LIKE '%gestionnaire administratif%' OR
                                 cj.title_clean LIKE '%support administratif%' OR
                                 cj.title_clean LIKE '%assistant de direction%' THEN 'ADMINISTRATIF_SUPPORT'

                            -- Management & Direction
                            WHEN cj.title_clean LIKE '%manager%' OR
                                 cj.title_clean LIKE '%directeur%' OR
                                 cj.title_clean LIKE '%responsable%' OR
                                 cj.title_clean LIKE '%chef de%' OR
                                 cj.title_clean LIKE '%gérant%' OR
                                 cj.title_clean LIKE '%superviseur%' OR
                                 cj.title_clean LIKE '%coordinateur%' OR
                                 cj.title_clean LIKE '%team leader%' OR
                                 cj.title_clean LIKE '%head of%' OR
                                 cj.title_clean LIKE '%directeur général%' OR
                                 cj.title_clean LIKE '%directeur technique%' OR
                                 cj.title_clean LIKE '%directeur commercial%' OR
                                 cj.title_clean LIKE '%chef d''équipe%' OR  -- Apostrophe échappée
                                 cj.title_clean LIKE '%chef d''agence%' OR   -- Apostrophe échappée
                                 cj.title_clean LIKE '%chef de projet%' THEN 'MANAGEMENT_DIRECTION'

                            -- Hôtellerie & Restauration
                            WHEN cj.title_clean LIKE '%hôtellerie%' OR
                                 cj.title_clean LIKE '%restauration%' OR
                                 cj.title_clean LIKE '%serveur%' OR
                                 cj.title_clean LIKE '%cuisinier%' OR
                                 cj.title_clean LIKE '%chef cuisinier%' OR
                                 cj.title_clean LIKE '%barman%' OR
                                 cj.title_clean LIKE '%réceptionniste hotel%' OR
                                 cj.title_clean LIKE '%housekeeping%' OR
                                 cj.title_clean LIKE '%agent lingerie%' THEN 'HOTELLERIE_RESTAURATION'

                            -- Bâtiment & Construction
                            WHEN cj.title_clean LIKE '%bâtiment%' OR
                                 cj.title_clean LIKE '%construction%' OR
                                 cj.title_clean LIKE '%architecte%' OR
                                 cj.title_clean LIKE '%génie civil%' OR
                                 cj.title_clean LIKE '%maçon%' OR
                                 cj.title_clean LIKE '%plombier%' OR
                                 cj.title_clean LIKE '%menuisier%' OR
                                 cj.title_clean LIKE '%électricien bâtiment%' OR
                                 cj.title_clean LIKE '%conducteur travaux%' THEN 'BATIMENT_CONSTRUCTION'

                            -- Santé & Médical
                            WHEN cj.title_clean LIKE '%infirmier%' OR
                                 cj.title_clean LIKE '%médecin%' OR
                                 cj.title_clean LIKE '%pharmacien%' OR
                                 cj.title_clean LIKE '%kinésithérapeute%' OR
                                 cj.title_clean LIKE '%aide soignant%' OR
                                 cj.title_clean LIKE '%santé%' OR
                                 cj.title_clean LIKE '%medical%' OR
                                 cj.title_clean LIKE '%paramédical%' THEN 'SANTE_MEDICAL'

                            -- Éducation & Formation
                            WHEN cj.title_clean LIKE '%enseignant%' OR
                                 cj.title_clean LIKE '%professeur%' OR
                                 cj.title_clean LIKE '%formateur%' OR
                                 cj.title_clean LIKE '%éducateur%' OR
                                 cj.title_clean LIKE '%enseigne%' OR
                                 cj.title_clean LIKE '%pédagogie%' OR
                                 cj.title_clean LIKE '%formation%' OR
                                 cj.title_clean LIKE '%tuteur%' THEN 'EDUCATION_FORMATION'

                            -- Juridique & Droit
                            WHEN cj.title_clean LIKE '%juriste%' OR
                                 cj.title_clean LIKE '%avocat%' OR
                                 cj.title_clean LIKE '%notaire%' OR
                                 cj.title_clean LIKE '%droit%' OR
                                 cj.title_clean LIKE '%legal%' OR
                                 cj.title_clean LIKE '%conseil juridique%' OR
                                 cj.title_clean LIKE '%compliance%' THEN 'JURIDIQUE_DROIT'

                            -- Télécom & Réseaux
                            WHEN cj.title_clean LIKE '%télécom%' OR
                                 cj.title_clean LIKE '%réseau%' OR
                                 cj.title_clean LIKE '%telecom%' OR
                                 cj.title_clean LIKE '%technicien réseau%' OR
                                 cj.title_clean LIKE '%ingénieur réseau%' OR
                                 cj.title_clean LIKE '%fibre optique%' OR
                                 cj.title_clean LIKE '%téléphonie%' THEN 'TELECOM_RESEAUX'

                            -- Automobile & Mécanique
                            WHEN cj.title_clean LIKE '%automobile%' OR
                                 cj.title_clean LIKE '%mécanicien%' OR
                                 cj.title_clean LIKE '%garagiste%' OR
                                 cj.title_clean LIKE '%carrossier%' OR
                                 cj.title_clean LIKE '%vendeur automobile%' OR
                                 cj.title_clean LIKE '%technicien automobile%' OR
                                 cj.title_clean LIKE '%mettreur clé en main%' THEN 'AUTOMOBILE_MECANIQUE'

                            -- Vente au détail & Distribution
                            WHEN cj.title_clean LIKE '%caissier%' OR
                                 cj.title_clean LIKE '%vendeur magasin%' OR
                                 cj.title_clean LIKE '%conseiller vente%' OR
                                 cj.title_clean LIKE '%chef de rayon%' OR
                                 cj.title_clean LIKE '%responsable magasin%' OR
                                 cj.title_clean LIKE '%gérant magasin%' OR
                                 cj.title_clean LIKE '%distributeur%' OR
                                 cj.title_clean LIKE '%retail%' THEN 'VENTE_DETAIL'

                            -- Immobilier
                            WHEN cj.title_clean LIKE '%immobilier%' OR
                                 cj.title_clean LIKE '%agent immobilier%' OR
                                 cj.title_clean LIKE '%gestionnaire immobilier%' OR
                                 cj.title_clean LIKE '%promoteur%' OR
                                 cj.title_clean LIKE '%courtier%' THEN 'IMMOBILIER'

                            -- Énergie & Environnement
                            WHEN cj.title_clean LIKE '%énergie%' OR
                                 cj.title_clean LIKE '%environnement%' OR
                                 cj.title_clean LIKE '%renouvelable%' OR
                                 cj.title_clean LIKE '%solaire%' OR
                                 cj.title_clean LIKE '%éolien%' OR
                                 cj.title_clean LIKE '%ingénieur énergie%' OR
                                 cj.title_clean LIKE '%technicien énergie%' THEN 'ENERGIE_ENVIRONNEMENT'

                            -- Par défaut - catégorie basée sur le titre
                            ELSE CONCAT('SPECIAL_', UPPER(REPLACE(SUBSTRING_INDEX(cj.title_clean, ' ', 1), ',', '')))
                        END as enhanced_category

                    FROM cleaned_jobs cj;
                    """;

            stmt.executeUpdate(createTable);

            // Compter et afficher les statistiques
            showEnhancedFeatureStats(conn);
        }
    }

    private static void showEnhancedFeatureStats(Connection conn) throws SQLException {
        String query = """
                SELECT
                    COUNT(*) as total,
                    SUM(is_remote) as remote_count,
                    SUM(has_contract_info) as contract_count,
                    SUM(has_experience_info) as experience_count,
                    SUM(has_education_info) as education_count,
                    SUM(has_salary_info) as salary_count,
                    COUNT(DISTINCT enhanced_category) as categories_count
                FROM job_features_enhanced;
                """;

        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("\n STATISTIQUES DES FEATURES ENRICHIES :");
                System.out.println("─".repeat(45));
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

            // Distribution des catégories enrichies
            String catQuery = """
                    SELECT
                        CASE
                            WHEN enhanced_category LIKE 'SPECIAL_%' THEN 'SPECIAL_CATEGORIES'
                            ELSE enhanced_category
                        END as category_group,
                        COUNT(*) as count,
                        ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM job_features_enhanced), 1) as percentage
                    FROM job_features_enhanced
                    GROUP BY category_group
                    ORDER BY count DESC;
                    """;

            System.out.println("\n DISTRIBUTION DES CATÉGORIES ENRICHIES :");
            System.out.println("─".repeat(50));
            try (ResultSet catRs = stmt.executeQuery(catQuery)) {
                while (catRs.next()) {
                    System.out.printf("  %-30s : %4d offres (%5.1f%%)\n",
                            catRs.getString("category_group"),
                            catRs.getInt("count"),
                            catRs.getDouble("percentage"));
                }
            }

            // Afficher les catégories spéciales créées
            String specialCatQuery = """
                    SELECT enhanced_category, COUNT(*) as count
                    FROM job_features_enhanced
                    WHERE enhanced_category LIKE 'SPECIAL_%'
                    GROUP BY enhanced_category
                    ORDER BY count DESC
                    LIMIT 15;
                    """;

            System.out.println("\n TOP 15 CATÉGORIES SPÉCIALES (basées sur titre) :");
            System.out.println("─".repeat(45));
            try (ResultSet specialRs = stmt.executeQuery(specialCatQuery)) {
                while (specialRs.next()) {
                    System.out.printf("  %-25s : %4d offres\n",
                            specialRs.getString("enhanced_category"),
                            specialRs.getInt("count"));
                }
            }
        }
    }

    private static void exportFeaturesEnhanced(Connection conn) throws SQLException, IOException {
        System.out.println("\n EXPORT DES FEATURES ENRICHIES");
        System.out.println("─".repeat(35));

        String query = "SELECT * FROM job_features_enhanced";
        exportToCSVEnhanced(conn, query, "02_features_enriched.csv");
        System.out.println("✓ Fichier : 02_features_enriched.csv");
    }

    private static void exportToCSVEnhanced(Connection conn, String query, String fileName)
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