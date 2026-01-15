package processing;

public class RunCompletePipelineFixed {
    public static void main(String[] args) {
        System.out.println("PIPELINE COMPLET CORRIGÉ POUR WEKA");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println(" Objectif : Transformer vos données scrapées en format Weka");
        System.out.println("════════════════════════════════════════════════════════\n");

        long startTime = System.currentTimeMillis();

        try {
            // Étape 1: Nettoyage
            System.out.println("ÉTAPE 1/4 : 🧹 NETTOYAGE DES DONNÉES BRUTES");
            System.out.println("─".repeat(50));
            CleanDataBasic.main(args);

            System.out.println("\n" + "═".repeat(70) + "\n");

            // Étape 2: Extraction des features (version simplifiée)
            System.out.println("ÉTAPE 2/4 :  EXTRACTION DES CARACTÉRISTIQUES SIMPLIFIÉE");
            System.out.println("─".repeat(50));
            FeatureExtractorEnhanced.main(args);

            System.out.println("\n" + "═".repeat(70) + "\n");

            // Étape 3: Équilibrage des catégories (version corrigée)
            System.out.println("ÉTAPE 3/4 : ⚖️  ÉQUILIBRAGE DES CATÉGORIES");
            System.out.println("─".repeat(50));
            CategoryBalancer.main(args);

            System.out.println("\n" + "═".repeat(70) + "\n");

            // Étape 4: Export pour Weka
            System.out.println("ÉTAPE 4/4 : 🚀 EXPORT POUR WEKA");
            System.out.println("─".repeat(50));
            WekaExporterFixed.main(args);

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            System.out.println("\n" + "═".repeat(70));
            System.out.println("PIPELINE CORRIGÉ TERMINÉ EN " + duration + " SECONDES");
            System.out.println("\n🎉 VOS DONNÉES SONT PRÊTES POUR WEKA !");

            displayFinalSummary();

        } catch (Exception e) {
            System.err.println("\n ERREUR DANS LE PIPELINE : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void displayFinalSummary() {
        System.out.println("\n📂 FICHIERS GÉNÉRÉS :");
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│ 01_données_nettoyées.csv    - Données nettoyées     │");
        System.out.println("│ 02_features_extraits.csv    - Features extraites    │");
        System.out.println("│ 03_distribution_categories.csv - Statistiques       │");
        System.out.println("│ 04_dataset_complet_fixed.csv - Dataset complet      │");
        System.out.println("│ 05_dataset_simplifié_fixed.csv - Pour débuter       │");
        System.out.println("│ 06_weka_ready_fixed.arff    - Format ARFF corrigé   │");
        System.out.println("│ 07_guide_weka_fixed.txt     - Guide d'utilisation   │");
        System.out.println("└─────────────────────────────────────────────────────┘");

        System.out.println("\n🎮 POUR COMMENCER AVEC WEKA (recommandé) :");
        System.out.println("  1. Ouvrir Weka Explorer");
        System.out.println("  2. Cliquer sur 'Open file'");
        System.out.println("  3. Choisir '05_dataset_simplifié_fixed.csv'");
        System.out.println("  4. Suivre les instructions dans '07_guide_weka_fixed.txt'");

        System.out.println("\n🔧 ALGORITHMES À TESTER EN PREMIER :");
        System.out.println("  • J48 (Classification) - Pour comprendre les patterns");
        System.out.println("  • SimpleKMeans (Clustering) - Pour regrouper les offres");
        System.out.println("  • Apriori (Association) - Pour trouver des règles");

        System.out.println("\n CONSEIL : Commencez avec '05_dataset_simplifié_fixed.csv'");
        System.out.println("             pour une première expérience plus simple.");
    }
}