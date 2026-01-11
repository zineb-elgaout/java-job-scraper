package ml;

import weka.associations.Apriori;
import weka.associations.FPGrowth;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import java.util.*;

public class AssociationMiner {

    /**
     * Trouve des règles d'association avec Apriori
     */
    public Apriori findAssociationRules(Instances data, double minSupport,
            double minConfidence) throws Exception {
        System.out.println("\n RECHERCHE DE RÈGLES D'ASSOCIATION (APRIORI)");
        System.out.println("─".repeat(60));

        // Convertir les attributs numériques en nominaux pour Apriori
        Instances nominalData = new Instances(data);
        for (int i = 0; i < nominalData.numAttributes(); i++) {
            if (nominalData.attribute(i).isNumeric()) {
                NumericToNominal convert = new NumericToNominal();
                convert.setAttributeIndices(String.valueOf(i + 1));
                convert.setInputFormat(nominalData);
                nominalData = Filter.useFilter(nominalData, convert);
            }
        }

        Apriori apriori = new Apriori();
        apriori.setClassIndex(-1); // Pas d'attribut de classe pour les règles d'association

        // Paramètres
        apriori.setMinMetric(minConfidence);
        apriori.setLowerBoundMinSupport(minSupport);
        apriori.setUpperBoundMinSupport(1.0);
        apriori.setNumRules(50); // Réduire pour éviter les problèmes
        apriori.setCar(false);

        apriori.buildAssociations(nominalData);

        System.out.println(" Règles d'association trouvées");
        System.out.println("   Support minimum : " + minSupport);
        System.out.println("   Confiance minimum : " + minConfidence);

        return apriori;
    }

    /**
     * Affiche les règles d'association
     */
    public void printAssociationRules(Apriori apriori) throws Exception {
        System.out.println("\n📜 RÈGLES D'ASSOCIATION TROUVÉES :");
        System.out.println("─".repeat(70));

        // Dans Weka, les règles sont obtenues via toString()
        String rulesString = apriori.toString();
        String[] lines = rulesString.split("\n");

        boolean inRulesSection = false;
        int ruleCount = 0;

        for (String line : lines) {
            if (line.contains("Best rules found:")) {
                inRulesSection = true;
                continue;
            }

            if (inRulesSection && line.trim().isEmpty()) {
                inRulesSection = false;
                continue;
            }

            if (inRulesSection && line.contains("==>")) {
                ruleCount++;
                if (ruleCount <= 20) { // Limiter à 20 règles
                    System.out.println("Règle " + ruleCount + " : " + line.trim());

                    // Extraire les métriques si présentes
                    if (line.contains("conf:(")) {
                        int start = line.indexOf("conf:(");
                        int end = line.indexOf(")", start);
                        if (start != -1 && end != -1) {
                            String metrics = line.substring(start, end + 1);
                            System.out.println("         " + metrics);
                        }
                    }
                    System.out.println();
                }
            }
        }

        if (ruleCount == 0) {
            System.out.println(" Aucune règle trouvée avec les paramètres actuels");
        } else {
            System.out.println(" Total de règles trouvées : " + ruleCount);

            // Afficher les statistiques
            System.out.println("\n STATISTIQUES DES RÈGLES :");
            System.out.println("─".repeat(30));

            // Extraire les métriques globales
            for (String line : lines) {
                if (line.contains("Number of rules generated:")) {
                    System.out.println("Nombre de règles générées : " +
                            line.substring(line.indexOf(":") + 1).trim());
                } else if (line.contains("Minimum support:")) {
                    System.out.println("Support minimum : " +
                            line.substring(line.indexOf(":") + 1).trim());
                } else if (line.contains("Minimum metric <confidence>:")) {
                    System.out.println("Confiance minimum : " +
                            line.substring(line.indexOf(":") + 1).trim());
                }
            }
        }
    }

    /**
     * Méthode alternative pour afficher les règles avec format personnalisé
     */
    public void printRulesFormatted(Apriori apriori) throws Exception {
        System.out.println("\n RÈGLES FORMATÉES :");
        System.out.println("─".repeat(70));

        String rulesString = apriori.toString();
        String[] lines = rulesString.split("\n");

        List<String> rules = new ArrayList<>();
        List<Double> confidences = new ArrayList<>();
        List<Double> supports = new ArrayList<>();

        // Parser les règles
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.contains("==>")) {
                // C'est une règle
                String rulePart = line.substring(0, line.indexOf("conf:(")).trim();
                rules.add(rulePart);

                // Extraire confiance et support
                int confStart = line.indexOf("conf:(") + 6;
                int confEnd = line.indexOf(")", confStart);
                String confStr = line.substring(confStart, confEnd);
                double confidence = Double.parseDouble(confStr);
                confidences.add(confidence);

                // Chercher le support dans la même ligne ou la suivante
                if (line.contains("lev:")) {
                    int levStart = line.indexOf("lev:(") + 5;
                    int levEnd = line.indexOf(")", levStart);
                    String levStr = line.substring(levStart, levEnd);
                    double support = Double.parseDouble(levStr);
                    supports.add(support);
                } else if (i + 1 < lines.length && lines[i + 1].contains("lev:")) {
                    String nextLine = lines[i + 1].trim();
                    int levStart = nextLine.indexOf("lev:(") + 5;
                    int levEnd = nextLine.indexOf(")", levStart);
                    String levStr = nextLine.substring(levStart, levEnd);
                    double support = Double.parseDouble(levStr);
                    supports.add(support);
                } else {
                    supports.add(0.0);
                }
            }
        }

        // Afficher les règles avec leurs métriques
        for (int i = 0; i < Math.min(15, rules.size()); i++) {
            System.out.printf("Règle %2d : %s\n", i + 1, rules.get(i));
            System.out.printf("         Confiance: %.3f, Support: %.3f\n\n",
                    confidences.get(i), supports.get(i));
        }

        if (rules.isEmpty()) {
            System.out.println(" Aucune règle n'a été trouvée");
            System.out.println(" Essayez de réduire le support minimum");
        } else {
            System.out.println(" " + rules.size() + " règles trouvées");

            // Calculer les moyennes
            double avgConf = confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double avgSup = supports.stream().mapToDouble(Double::doubleValue).average().orElse(0);

            System.out.printf(" Moyennes - Confiance: %.3f, Support: %.3f\n", avgConf, avgSup);
        }
    }

    /**
     * Trouve des règles spécifiques à une catégorie
     */
    public List<String> findCategoryRules(Apriori apriori, String category) {
        List<String> categoryRules = new ArrayList<>();
        String rulesString = apriori.toString();
        String[] lines = rulesString.split("\n");

        for (String line : lines) {
            if (line.contains("==>") && line.contains(category)) {
                categoryRules.add(line.trim());
            }
        }

        // Trier par confiance (si on peut l'extraire)
        categoryRules.sort((r1, r2) -> {
            try {
                double conf1 = extractConfidence(r1);
                double conf2 = extractConfidence(r2);
                return Double.compare(conf2, conf1); // Ordre décroissant
            } catch (Exception e) {
                return 0;
            }
        });

        return categoryRules;
    }

    /**
     * Extrait la confiance d'une règle
     */
    private double extractConfidence(String ruleLine) {
        if (ruleLine.contains("conf:(")) {
            int start = ruleLine.indexOf("conf:(") + 6;
            int end = ruleLine.indexOf(")", start);
            try {
                return Double.parseDouble(ruleLine.substring(start, end));
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Analyse les patterns fréquents avec FP-Growth
     */
    public void analyzeFrequentPatterns(Instances data) throws Exception {
        System.out.println("\n ANALYSE DES PATTERNS FRÉQUENTS (FP-Growth)");
        System.out.println("─".repeat(60));

        try {
            FPGrowth fpg = new FPGrowth();
            fpg.setMinMetric(0.1); // Support minimum
            fpg.setNumRulesToFind(20); // Limiter le nombre

            fpg.buildAssociations(data);

            // Afficher les résultats
            String results = fpg.toString();
            String[] lines = results.split("\n");

            System.out.println(" Patterns fréquents trouvés");

            // Afficher les premières lignes
            int count = 0;
            for (String line : lines) {
                if (line.contains("==>") || line.contains("Size of set of large itemsets")) {
                    System.out.println(line);
                    count++;
                    if (count >= 15)
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("  FP-Growth non disponible : " + e.getMessage());
            System.out.println(" Utilisez Apriori à la place");
        }
    }

    /**
     * Analyse simple des associations pour débuter
     */
    public void simpleAssociationAnalysis(Instances data) throws Exception {
        System.out.println("\n ANALYSE SIMPLE DES ASSOCIATIONS");
        System.out.println("═".repeat(60));

        System.out.println("Paramètres par défaut :");
        System.out.println("  - Support minimum : 0.1 (10%)");
        System.out.println("  - Confiance minimum : 0.8 (80%)");
        System.out.println("  - Nombre maximum de règles : 20");

        Apriori apriori = findAssociationRules(data, 0.1, 0.8);
        printRulesFormatted(apriori);

        // Suggestions d'interprétation
        System.out.println("\n INTERPRÉTATION DES RÈGLES :");
        System.out.println("─".repeat(30));
        System.out.println("1. Regardez les règles avec la confiance la plus élevée");
        System.out.println("2. Identifiez les associations surprenantes ou utiles");
        System.out.println("3. Utilisez ces insights pour des recommandations");
        System.out.println("\n Exemple : 'Si A alors B' signifie que");
        System.out.println("   quand A est présent, B est souvent présent aussi");
    }

    /**
     * Recherche de règles spécifiques aux offres IT
     */
    public void findITJobRules(Instances data) throws Exception {
        System.out.println("\n RÈGLES POUR LES OFFRES IT");
        System.out.println("═".repeat(60));

        // Rechercher des règles avec support et confiance modérés
        Apriori apriori = findAssociationRules(data, 0.05, 0.7);
        String rulesString = apriori.toString();

        List<String> itRules = new ArrayList<>();
        String[] keywords = { "IT", "DEVELOPPEMENT", "TECHNOLOGIE", "LOGICIEL", "PROGRAMMATION", "CODE" };

        String[] lines = rulesString.split("\n");
        for (String line : lines) {
            line = line.toUpperCase();
            for (String keyword : keywords) {
                if (line.contains(keyword) && line.contains("==>")) {
                    itRules.add(line);
                    break;
                }
            }
        }

        if (itRules.isEmpty()) {
            System.out.println(" Aucune règle spécifique aux offres IT trouvée");
            System.out.println(" Essayez avec des paramètres plus permissifs");
        } else {
            System.out.println(" " + itRules.size() + " règles IT trouvées :");
            for (int i = 0; i < Math.min(10, itRules.size()); i++) {
                System.out.println("\nRègle " + (i + 1) + " :");
                System.out.println("  " + itRules.get(i));
            }
        }
    }

    /**
     * Analyse les corrélations entre attributs
     */
    public void analyzeAttributeCorrelations(Instances data) throws Exception {
        System.out.println("\n ANALYSE DES CORRÉLATIONS ENTRE ATTRIBUTS");
        System.out.println("─".repeat(60));

        System.out.println("Recherche de corrélations simples...");

        // Pour un dataset simplifié, on peut analyser manuellement
        // Dans un cas réel, on utiliserait des méthodes statistiques

        System.out.println("\n ATTRIBUTS À ANALYSER :");
        for (int i = 0; i < Math.min(10, data.numAttributes()); i++) {
            System.out.printf("  %-20s : %s\n",
                    data.attribute(i).name(),
                    data.attribute(i).isNumeric() ? "Numérique"
                            : data.attribute(i).isNominal() ? "Nominal (" + data.attribute(i).numValues() + " valeurs)"
                                    : "Autre");
        }

        System.out.println("\n CORRÉLATIONS POSSIBLES À EXPLORER :");
        System.out.println("  1. Télétravail <-->  Type de contrat");
        System.out.println("  2. Localisation <-->  Secteur d'activité");
        System.out.println("  3. Niveau d'éducation <-->  Expérience requise");
        System.out.println("  4. Mention salariale <-->  Type d'entreprise");

        System.out.println("\n POUR ALLER PLUS LOIN :");
        System.out.println("  - Utilisez des algorithmes d'association (Apriori)");
        System.out.println("  - Analysez les fréquences d'apparition conjointes");
        System.out.println("  - Testez différentes valeurs de support et confiance");
    }
}