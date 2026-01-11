package ml;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.clusterers.Clusterer;
import weka.core.Instances;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class MLMain {
    public static Scanner scanner = new Scanner(System.in);
    public static JobDataLoader dataLoader = new JobDataLoader();
    public static JobClassifier jobClassifier = new JobClassifier();
    public static JobClustering jobClustering = new JobClustering();
    public static AssociationMiner associationMiner = new AssociationMiner();
    public static ModelEvaluator modelEvaluator = new ModelEvaluator();
    public static PredictionService predictionService = new PredictionService();

    public static Instances dataset;
    public static Map<String, Instances> trainTestSplit;
    public static Classifier trainedClassifier;
    public static Clusterer trainedClusterer;
    public static Map<String, Classifier> trainedClassifiers = new HashMap<>();

    public static int getIntInput(String message) {
        System.out.print(message);
        while (!scanner.hasNextInt()) {
            System.out.println("Veuillez entrer un nombre entier.");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); 
        return value;
    }

    public static double getDoubleInput(String message) {
        System.out.print(message);
        while (!scanner.hasNextDouble()) {
            System.out.println("Veuillez entrer un nombre decimal.");
            scanner.next();
        }
        double value = scanner.nextDouble();
        scanner.nextLine(); 
        return value;
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   SYSTEME DE MACHINE LEARNING");
        System.out.println("   pour l'analyse d'offres d'emploi");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            showMainMenu();
            int choice = getIntInput("\nVotre choix : ");

            try {
                switch (choice) {
                    case 1:
                        loadAndPrepareData();
                        break;
                    case 2:
                        classificationMenu();
                        break;
                    case 3:
                        clusteringMenu();
                        break;
                    case 4:
                        associationRulesMenu();
                        break;
                    case 5:
                        predictionMenu();
                        break;
                    case 6:
                        runAllTests();
                        break;
                    case 7:
                        modelsManagementMenu();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Fin du programme.");
                        break;
                    default:
                        System.out.println("Choix invalide.");
                }
            } catch (Exception e) {
                System.out.println("Erreur : " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("\n=========================================");
        System.out.println("   FIN DE L'EXECUTION");
        System.out.println("=========================================");
        scanner.close();
    }

    public static void showMainMenu() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1. Charger et préparer les données");
        System.out.println("2. Classification");
        System.out.println("3. Clustering");
        System.out.println("4. Règles d'association");
        System.out.println("5. Prédiction et recommandations");
        System.out.println("6. Exécuter tous les tests");
        System.out.println("7. Gestion des modèles");
        System.out.println("0. Quitter");
    }

    public static void loadAndPrepareData() throws Exception {
        System.out.println("\n=== CHARGEMENT ET PREPARATION DES DONNEES ===");

        // Vérifier si le fichier existe
        dataLoader.checkDataFile();

        // Charger les données
        System.out.println("\nChargement des données en cours...");
        dataset = dataLoader.loadDataset();

        // Afficher les statistiques
        dataLoader.printDataStatistics(dataset);

        // Préparer pour la classification
        System.out.println("\nPreparation des donnees pour la classification...");
        dataset = dataLoader.prepareForClassification(dataset);

        // Demander le pourcentage de division
        System.out.print("\nPourcentage d'entrainement (0-100) [80] : ");
        String percStr = scanner.nextLine().trim();
        double percentage = percStr.isEmpty() ? 80.0 : Double.parseDouble(percStr);

        // Diviser les données
        System.out.println("Division des donnees (" + percentage + "% train, " + (100 - percentage) + "% test)...");
        trainTestSplit = dataLoader.splitTrainTest(dataset, percentage);

        System.out.println("\n Donnees chargees et preparees avec succes !");
        System.out.println("   - Nombre total d'instances : " + dataset.numInstances());
        System.out.println("   - Entrainement : " + trainTestSplit.get("train").numInstances() + " instances");
        System.out.println("   - Test : " + trainTestSplit.get("test").numInstances() + " instances");
    }

    public static void classificationMenu() throws Exception {
        if (trainTestSplit == null) {
            System.out.println(" Veuillez d'abord charger et préparer les données (option 1).");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n=== CLASSIFICATION ===");
            System.out.println("1. Comparer tous les algorithmes");
            System.out.println("2. Entraîner J48 (arbre de décision)");
            System.out.println("3. Entraîner Naive Bayes");
            System.out.println("4. Entraîner Random Forest");
            System.out.println("5. Entraîner k-NN");
            System.out.println("6. Entraîner Neural Network");
            System.out.println("7. Entraîner Regression Logistique");
            System.out.println("8. Entraîner classifieur par vote");
            System.out.println("9. Validation croisée");
            System.out.println("10. Évaluation détaillée");
            System.out.println("11. Retour au menu principal");

            int choice = getIntInput("\nVotre choix : ");

            switch (choice) {
                case 1:
                    System.out.println("\n--- COMPARAISON DES ALGORITHMES ---");
                    jobClassifier.compareAlgorithms(trainTestSplit.get("train"), trainTestSplit.get("test"));
                    break;
                case 2:
                    trainAndSaveAlgorithm("J48");
                    break;
                case 3:
                    trainAndSaveAlgorithm("NaiveBayes");
                    break;
                case 4:
                    trainAndSaveAlgorithm("RandomForest");
                    break;
                case 5:
                    trainAndSaveAlgorithm("KNN");
                    break;
                case 6:
                    trainAndSaveAlgorithm("NeuralNetwork");
                    break;
                case 7:
                    trainAndSaveAlgorithm("Logistic");
                    break;
                case 8:
                    trainVotingClassifier();
                    break;
                case 9:
                    if (trainedClassifier == null) {
                        System.out.println("  Aucun classifieur entraîné. Entraînez d'abord un algorithme.");
                    } else {
                        System.out.print("Nombre de folds [10] : ");
                        String foldsStr = scanner.nextLine().trim();
                        int folds = foldsStr.isEmpty() ? 10 : Integer.parseInt(foldsStr);
                        System.out.println("\n--- VALIDATION CROISEE (" + folds + " folds) ---");
                        jobClassifier.crossValidate(trainedClassifier, dataset, folds);
                    }
                    break;
                case 10:
                    if (trainedClassifier == null) {
                        System.out.println("  Aucun classifieur entraîné. Entraînez d'abord un algorithme.");
                    } else {
                        System.out.println("\n--- EVALUATION DETAILLEE ---");
                        modelEvaluator.evaluateClassificationModel(trainedClassifier,
                                trainTestSplit.get("train"), trainTestSplit.get("test"));
                    }
                    break;
                case 11:
                    back = true;
                    break;
                default:
                    System.out.println(" Choix invalide.");
            }
        }
    }

    public static void trainAndSaveAlgorithm(String algorithmName) throws Exception {
        System.out.println("\n=== ENTRAINEMENT " + algorithmName + " ===");

        Classifier classifier = null;

        switch (algorithmName) {
            case "J48":
                classifier = jobClassifier.trainJ48(trainTestSplit.get("train"));
                break;
            case "NaiveBayes":
                classifier = jobClassifier.trainNaiveBayes(trainTestSplit.get("train"));
                break;
            case "RandomForest":
                classifier = jobClassifier.trainRandomForest(trainTestSplit.get("train"));
                break;
            case "KNN":
                classifier = jobClassifier.trainKNN(trainTestSplit.get("train"));
                break;
            case "NeuralNetwork":
                classifier = jobClassifier.trainNeuralNetwork(trainTestSplit.get("train"));
                break;
            case "Logistic":
                classifier = jobClassifier.trainLogistic(trainTestSplit.get("train"));
                break;
            default:
                System.out.println(" Algorithme non reconnu : " + algorithmName);
                return;
        }

        if (classifier != null) {
            trainedClassifier = classifier;
            trainedClassifiers.put(algorithmName, classifier);

            // Évaluation
            System.out.println("\n--- EVALUATION ---");
            Evaluation eval = jobClassifier.evaluateClassifier(classifier,
                    trainTestSplit.get("train"), trainTestSplit.get("test"));

            System.out.println("\n📊 RESUME DES PERFORMANCES :");
            System.out.println("   Précision : " + String.format("%.2f", eval.pctCorrect()) + "%");
            System.out.println("   Kappa : " + String.format("%.3f", eval.kappa()));
            System.out.println("   AUC : " + String.format("%.3f", eval.weightedAreaUnderROC()));

            // Sauvegarde
            System.out.print("\n Sauvegarder le modèle ? (o/n) [o] : ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.isEmpty() || response.equals("o") || response.equals("oui")) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String defaultName = "models/" + algorithmName.toLowerCase() + "_" + timestamp + ".model";

                System.out.print("   Nom du fichier [" + defaultName + "] : ");
                String filename = scanner.nextLine().trim();
                if (filename.isEmpty())
                    filename = defaultName;

                if (!filename.endsWith(".model"))
                    filename += ".model";

                // Créer le dossier models si nécessaire
                File modelsDir = new File("models");
                if (!modelsDir.exists()) {
                    modelsDir.mkdirs();
                    System.out.println("   Dossier 'models' créé.");
                }

                jobClassifier.saveModel(classifier, filename);
                System.out.println(" Modèle sauvegardé : " + filename);

                // Afficher la taille
                File modelFile = new File(filename);
                if (modelFile.exists()) {
                    double sizeKB = modelFile.length() / 1024.0;
                    System.out.println("   Taille : " + String.format("%.1f", sizeKB) + " KB");
                }
            } else {
                System.out.println("  Modèle non sauvegardé.");
            }
        }
    }

    public static void trainVotingClassifier() throws Exception {
        System.out.println("\n=== CLASSIFIEUR PAR VOTE ===");
        System.out.println("1. Vote simple (majorité)");
        System.out.println("2. Vote avancé (choix de la règle)");

        int choice = getIntInput("Votre choix : ");

        Classifier classifier = null;
        String ruleName = "";

        if (choice == 1) {
            classifier = jobClassifier.trainVotingClassifier(trainTestSplit.get("train"));
            ruleName = "majorité";
        } else {
            System.out.println("\nRègles disponibles : MAJORITY, AVERAGE, PRODUCT, MIN, MAX, MEDIAN");
            System.out.print("Choisissez une règle [MAJORITY] : ");
            String rule = scanner.nextLine().trim();
            if (rule.isEmpty())
                rule = "MAJORITY";

            classifier = jobClassifier.trainAdvancedVotingClassifier(trainTestSplit.get("train"), rule);
            ruleName = rule.toLowerCase();
        }

        if (classifier != null) {
            trainedClassifier = classifier;
            trainedClassifiers.put("Voting_" + ruleName, classifier);

            System.out.println("\n--- EVALUATION ---");
            Evaluation eval = jobClassifier.evaluateClassifier(classifier,
                    trainTestSplit.get("train"), trainTestSplit.get("test"));

            System.out.println("\n RESUME DES PERFORMANCES :");
            System.out.println("   Précision : " + String.format("%.2f", eval.pctCorrect()) + "%");
            System.out.println("   Règle de vote : " + ruleName);

            // Sauvegarde
            System.out.print("\n Sauvegarder le modèle ? (o/n) [o] : ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.isEmpty() || response.equals("o") || response.equals("oui")) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filename = "models/voting_" + ruleName + "_" + timestamp + ".model";

                // Créer le dossier models si nécessaire
                File modelsDir = new File("models");
                if (!modelsDir.exists())
                    modelsDir.mkdirs();

                jobClassifier.saveModel(classifier, filename);
                System.out.println(" Modèle vote sauvegardé : " + filename);
            }
        }
    }

    public static void clusteringMenu() throws Exception {
        if (dataset == null) {
            System.out.println(" Veuillez d'abord charger les données (option 1).");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n=== CLUSTERING ===");
            System.out.println("1. Recherche k optimal (K-Means)");
            System.out.println("2. K-Means clustering");
            System.out.println("3. EM clustering");
            System.out.println("4. Clustering hiérarchique");
            System.out.println("5. X-Means clustering");
            System.out.println("6. Analyser les clusters");
            System.out.println("7. Visualiser les clusters");
            System.out.println("8. Comparer algorithmes de clustering");
            System.out.println("9. Retour au menu principal");

            int choice = getIntInput("\nVotre choix : ");

            switch (choice) {
                case 1:
                    System.out.print("Nombre maximum de clusters à tester [10] : ");
                    String maxKStr = scanner.nextLine().trim();
                    int maxK = maxKStr.isEmpty() ? 10 : Integer.parseInt(maxKStr);
                    System.out.println("\n--- RECHERCHE DU K OPTIMAL (2 à " + maxK + ") ---");
                    jobClustering.findOptimalK(dataset, maxK);
                    break;
                case 2:
                    System.out.print("Nombre de clusters (k) [3] : ");
                    String kStr = scanner.nextLine().trim();
                    int k = kStr.isEmpty() ? 3 : Integer.parseInt(kStr);
                    System.out.println("\n--- K-MEANS CLUSTERING (k=" + k + ") ---");
                    trainedClusterer = jobClustering.applyKMeans(dataset, k);
                    jobClustering.analyzeClusters(trainedClusterer, dataset);
                    break;
                case 3:
                    System.out.print("Nombre maximum de clusters [3] : ");
                    String maxClustersStr = scanner.nextLine().trim();
                    int maxClusters = maxClustersStr.isEmpty() ? 3 : Integer.parseInt(maxClustersStr);
                    System.out.println("\n--- EM CLUSTERING ---");
                    trainedClusterer = jobClustering.applyEM(dataset, maxClusters);
                    jobClustering.analyzeClusters(trainedClusterer, dataset);
                    break;
                case 4:
                    System.out.print("Nombre de clusters [3] : ");
                    String numClustersStr = scanner.nextLine().trim();
                    int numClusters = numClustersStr.isEmpty() ? 3 : Integer.parseInt(numClustersStr);
                    System.out.println("\n--- CLUSTERING HIERARCHIQUE ---");
                    trainedClusterer = jobClustering.applyHierarchicalClustering(dataset, numClusters);
                    jobClustering.analyzeClusters(trainedClusterer, dataset);
                    break;
                case 5:
                    System.out.println("\n--- X-MEANS CLUSTERING ---");
                    trainedClusterer = jobClustering.applyXMeans(dataset);
                    jobClustering.analyzeClusters(trainedClusterer, dataset);
                    break;
                case 6:
                    if (trainedClusterer == null) {
                        System.out.println("  Aucun clusterer entraîné. Appliquez d'abord un clustering.");
                    } else {
                        System.out.println("\n--- ANALYSE DES CLUSTERS ---");
                        jobClustering.analyzeClusters(trainedClusterer, dataset);
                    }
                    break;
                case 7:
                    if (trainedClusterer == null) {
                        System.out.println("  Aucun clusterer entraîné. Appliquez d'abord un clustering.");
                    } else {
                        System.out.println("\n--- VISUALISATION DES CLUSTERS ---");
                        jobClustering.visualizeClusters(trainedClusterer, dataset);
                    }
                    break;
                case 8:
                    System.out.println("\n--- COMPARAISON DES ALGORITHMES DE CLUSTERING ---");
                    jobClustering.compareClusteringAlgorithms(dataset);
                    break;
                case 9:
                    back = true;
                    break;
                default:
                    System.out.println(" Choix invalide.");
            }
        }
    }

    public static void associationRulesMenu() throws Exception {
        if (dataset == null) {
            System.out.println(" Veuillez d'abord charger les données (option 1).");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n=== REGLES D'ASSOCIATION ===");
            System.out.println("1. Analyse simple");
            System.out.println("2. Recherche Apriori (paramètres personnalisés)");
            System.out.println("3. Règles pour offres IT");
            System.out.println("4. Afficher règles formatées");
            System.out.println("5. FP-Growth (patterns fréquents)");
            System.out.println("6. Analyse des corrélations");
            System.out.println("7. Retour au menu principal");

            int choice = getIntInput("\nVotre choix : ");

            switch (choice) {
                case 1:
                    System.out.println("\n--- ANALYSE SIMPLE DES ASSOCIATIONS ---");
                    associationMiner.simpleAssociationAnalysis(dataset);
                    break;
                case 2:
                    System.out.print("Support minimum (0.0-1.0) [0.1] : ");
                    String supportStr = scanner.nextLine().trim();
                    double minSupport = supportStr.isEmpty() ? 0.1 : Double.parseDouble(supportStr);

                    System.out.print("Confiance minimum (0.0-1.0) [0.7] : ");
                    String confidenceStr = scanner.nextLine().trim();
                    double minConfidence = confidenceStr.isEmpty() ? 0.7 : Double.parseDouble(confidenceStr);

                    System.out.println("\n--- RECHERCHE APRIORI ---");
                    System.out.println("   Support : " + minSupport);
                    System.out.println("   Confiance : " + minConfidence);

                    var apriori = associationMiner.findAssociationRules(dataset, minSupport, minConfidence);
                    associationMiner.printAssociationRules(apriori);
                    break;
                case 3:
                    System.out.println("\n--- REGLES POUR OFFRES IT ---");
                    associationMiner.findITJobRules(dataset);
                    break;
                case 4:
                    System.out.print("Support minimum (0.0-1.0) [0.1] : ");
                    supportStr = scanner.nextLine().trim();
                    minSupport = supportStr.isEmpty() ? 0.1 : Double.parseDouble(supportStr);

                    System.out.print("Confiance minimum (0.0-1.0) [0.7] : ");
                    confidenceStr = scanner.nextLine().trim();
                    minConfidence = confidenceStr.isEmpty() ? 0.7 : Double.parseDouble(confidenceStr);

                    System.out.println("\n--- REGLES FORMATEES ---");
                    apriori = associationMiner.findAssociationRules(dataset, minSupport, minConfidence);
                    associationMiner.printRulesFormatted(apriori);
                    break;
                case 5:
                    System.out.println("\n--- FP-GROWTH (PATTERNS FREQUENTS) ---");
                    associationMiner.analyzeFrequentPatterns(dataset);
                    break;
                case 6:
                    System.out.println("\n--- ANALYSE DES CORRELATIONS ---");
                    associationMiner.analyzeAttributeCorrelations(dataset);
                    break;
                case 7:
                    back = true;
                    break;
                default:
                    System.out.println(" Choix invalide.");
            }
        }
    }

    public static void predictionMenu() throws Exception {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== PREDICTION ET RECOMMANDATIONS ===");
            System.out.println("1. Générer recommandations par catégorie");
            System.out.println("2. Charger et utiliser un modèle");
            System.out.println("3. Évaluer modèle sur test set");
            System.out.println("4. Retour au menu principal");

            int choice = getIntInput("\nVotre choix : ");

            switch (choice) {
                case 1:
                    System.out.print("Catégorie [IT_DEVELOPPEMENT] : ");
                    String category = scanner.nextLine().trim();
                    if (category.isEmpty())
                        category = "IT_DEVELOPPEMENT";

                    System.out.println("\n--- RECOMMANDATIONS POUR : " + category + " ---");
                    var recommendations = predictionService.generateRecommendations(category);

                    System.out.println("\n📋 CONSEILS :");
                    List<String> conseils = (List<String>) recommendations.get("conseils");
                    if (conseils != null) {
                        for (String conseil : conseils) {
                            System.out.println("   • " + conseil);
                        }
                    }

                    System.out.println("\n🎓 FORMATIONS SUGGÉRÉES :");
                    List<String> formations = (List<String>) recommendations.get("formations_suggerees");
                    if (formations != null) {
                        for (String formation : formations) {
                            System.out.println("   • " + formation);
                        }
                    }

                    System.out.println("\n COMPÉTENCES CLÉS :");
                    List<String> competences = (List<String>) recommendations.get("competences_cles");
                    if (competences != null) {
                        for (String competence : competences) {
                            System.out.println("   • " + competence);
                        }
                    }
                    break;
                case 2:
                    System.out.print("Chemin du modèle : ");
                    String modelPath = scanner.nextLine();
                    System.out.print("Chemin des données de structure : ");
                    String dataPath = scanner.nextLine();

                    try {
                        predictionService.loadModel(modelPath, dataPath);
                        System.out.println(" Modèle chargé avec succès.");
                    } catch (Exception e) {
                        System.out.println(" Erreur lors du chargement : " + e.getMessage());
                    }
                    break;
                case 3:
                    if (trainedClassifier == null) {
                        System.out.println("  Aucun modèle entraîné ou chargé.");
                    } else {
                        System.out.print("Chemin des données de test : ");
                        String testPath = scanner.nextLine();
                        predictionService.evaluateOnTestSet(testPath);
                    }
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println(" Choix invalide.");
            }
        }
    }

    public static void modelsManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== GESTION DES MODELES ===");
            System.out.println("1. Lister les modèles disponibles");
            System.out.println("2. Sauvegarder le modèle courant");
            System.out.println("3. Charger un modèle");
            System.out.println("4. Retour au menu principal");

            int choice = getIntInput("\nVotre choix : ");

            switch (choice) {
                case 1:
                    File modelsDir = new File("models");
                    if (modelsDir.exists() && modelsDir.listFiles() != null) {
                        File[] modelFiles = modelsDir.listFiles((dir, name) -> name.endsWith(".model"));

                        if (modelFiles != null && modelFiles.length > 0) {
                            System.out.println("\n📁 MODÈLES DISPONIBLES (" + modelFiles.length + ") :");
                            System.out.println("==========================================");

                            // Trier par date de modification (du plus récent au plus ancien)
                            Arrays.sort(modelFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                            for (File file : modelFiles) {
                                double sizeKB = file.length() / 1024.0;
                                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm")
                                        .format(new Date(file.lastModified()));
                                System.out.printf("  %-35s %6.1f KB  %s\n", file.getName(), sizeKB, date);
                            }
                        } else {
                            System.out.println(" Aucun modèle disponible dans le dossier 'models'.");
                        }
                    } else {
                        System.out.println(" Dossier 'models' non trouvé ou vide.");
                    }
                    break;
                case 2:
                    if (trainedClassifier == null) {
                        System.out.println("  Aucun modèle entraîné à sauvegarder.");
                    } else {
                        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String defaultName = "model_current_" + timestamp + ".model";

                        System.out.print("Nom du fichier [" + defaultName + "] : ");
                        String filename = scanner.nextLine().trim();
                        if (filename.isEmpty())
                            filename = defaultName;

                        if (!filename.endsWith(".model"))
                            filename += ".model";

                        // Assurer que le chemin commence par "models/"
                        if (!filename.startsWith("models/")) {
                            filename = "models/" + filename;
                        }

                        File dir = new File("models");
                        if (!dir.exists())
                            dir.mkdirs();

                        try {
                            jobClassifier.saveModel(trainedClassifier, filename);
                            System.out.println(" Modèle sauvegardé : " + filename);
                        } catch (Exception e) {
                            System.out.println(" Erreur lors de la sauvegarde : " + e.getMessage());
                        }
                    }
                    break;
                case 3:
                    System.out.print("Chemin du modèle : ");
                    String modelPath = scanner.nextLine();

                    // Essayer avec le chemin tel quel
                    if (!new File(modelPath).exists()) {
                        // Essayer avec "models/" préfixé
                        if (new File("models/" + modelPath).exists()) {
                            modelPath = "models/" + modelPath;
                        } else if (new File(modelPath + ".model").exists()) {
                            modelPath = modelPath + ".model";
                        } else if (new File("models/" + modelPath + ".model").exists()) {
                            modelPath = "models/" + modelPath + ".model";
                        }
                    }

                    try {
                        trainedClassifier = jobClassifier.loadModel(modelPath);
                        System.out.println(" Modèle chargé avec succès : " + modelPath);
                    } catch (Exception e) {
                        System.out.println(" Erreur lors du chargement : " + e.getMessage());
                        System.out.println("   Essayer : models/nom_du_modele.model");
                    }
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println(" Choix invalide.");
            }
        }
    }

    public static void runAllTests() throws Exception {
        System.out.println("\n=== EXECUTION DE TOUS LES TESTS ===");
        System.out.println("Cette opération va exécuter une série de tests prédéfinis.");
        System.out.print("Voulez-vous continuer ? (o/n) [o] : ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("n") || response.equals("non")) {
            System.out.println(" Opération annulée.");
            return;
        }

        System.out.println("\n1️1.  CHARGEMENT DES DONNEES...");
        loadAndPrepareData();

        System.out.println("\n2️2.  TESTS DE CLASSIFICATION...");
        testClassificationAlgorithms();

        System.out.println("\n3️3.  TESTS DE CLUSTERING...");
        testClusteringAlgorithms();

        System.out.println("\n4️4.  TESTS DE REGLES D'ASSOCIATION...");
        testAssociationRules();

        System.out.println("\n5️5.  TESTS DE PREDICTION...");
        testPrediction();

        System.out.println("\n TOUS LES TESTS ONT ÉTÉ EXÉCUTÉS AVEC SUCCÈS !");
    }

    public static void testClassificationAlgorithms() throws Exception {
        System.out.println("\n--- COMPARAISON DES ALGORITHMES ---");
        jobClassifier.compareAlgorithms(trainTestSplit.get("train"), trainTestSplit.get("test"));

        System.out.println("\n--- RANDOM FOREST ---");
        trainedClassifier = jobClassifier.trainRandomForest(trainTestSplit.get("train"));
        jobClassifier.evaluateClassifier(trainedClassifier,
                trainTestSplit.get("train"), trainTestSplit.get("test"));

        System.out.println("\n--- VALIDATION CROISEE (10 folds) ---");
        jobClassifier.crossValidate(trainedClassifier, dataset, 10);

        System.out.println("\n--- EVALUATION DETAILLEE ---");
        modelEvaluator.evaluateClassificationModel(trainedClassifier,
                trainTestSplit.get("train"), trainTestSplit.get("test"));

        System.out.println("\n--- SAUVEGARDE RANDOM FOREST ---");
        jobClassifier.saveModel(trainedClassifier, "models/random_forest_test.model");
        System.out.println(" Random Forest sauvegardé.");

        System.out.println("\n--- J48 ---");
        trainedClassifier = jobClassifier.trainJ48(trainTestSplit.get("train"));
        jobClassifier.evaluateClassifier(trainedClassifier,
                trainTestSplit.get("train"), trainTestSplit.get("test"));

        System.out.println("\n--- NAIVE BAYES ---");
        trainedClassifier = jobClassifier.trainNaiveBayes(trainTestSplit.get("train"));
        jobClassifier.evaluateClassifier(trainedClassifier,
                trainTestSplit.get("train"), trainTestSplit.get("test"));
    }

    public static void testClusteringAlgorithms() throws Exception {
        System.out.println("\n--- RECHERCHE K OPTIMAL ---");
        Map<Integer, Double> elbowResults = jobClustering.findOptimalK(dataset, 10);

        // Déterminer le k optimal (simplifié)
        int optimalK = 3;
        System.out.println("K optimal suggéré : " + optimalK);

        System.out.println("\n--- K-MEANS (k=" + optimalK + ") ---");
        trainedClusterer = jobClustering.applyKMeans(dataset, optimalK);
        jobClustering.analyzeClusters(trainedClusterer, dataset);

        System.out.println("\n--- VISUALISATION DES CLUSTERS ---");
        jobClustering.visualizeClusters(trainedClusterer, dataset);

        System.out.println("\n--- EM CLUSTERING ---");
        trainedClusterer = jobClustering.applyEM(dataset, optimalK);
        jobClustering.analyzeClusters(trainedClusterer, dataset);
    }

    public static void testAssociationRules() throws Exception {
        System.out.println("\n--- ANALYSE SIMPLE ---");
        associationMiner.simpleAssociationAnalysis(dataset);

        System.out.println("\n--- APRIORI (support=0.15, confiance=0.7) ---");
        var apriori = associationMiner.findAssociationRules(dataset, 0.15, 0.7);
        associationMiner.printRulesFormatted(apriori);

        System.out.println("\n--- REGLES POUR OFFRES IT ---");
        associationMiner.findITJobRules(dataset);

        System.out.println("\n--- RECHERCHE PERMISSIVE ---");
        var aprioriPermissif = associationMiner.findAssociationRules(dataset, 0.02, 0.6);

        System.out.println("\n--- ANALYSE DES CORRELATIONS ---");
        associationMiner.analyzeAttributeCorrelations(dataset);
    }

    public static void testPrediction() throws Exception {
        System.out.println("\n--- RECOMMANDATIONS PAR CATEGORIE ---");

        String[] categories = { "IT_DEVELOPPEMENT", "COMMERCIAL_VENTE", "RESSOURCES_HUMAINES", "DATA_IA" };

        for (String category : categories) {
            System.out.println("\n CATEGORIE : " + category);
            var recommendations = predictionService.generateRecommendations(category);

            System.out.println(" Conseils : " + recommendations.get("conseils"));
            System.out.println(" Formations : " + recommendations.get("formations_suggerees"));
            System.out.println(" Compétences : " + recommendations.get("competences_cles"));
            System.out.println("─".repeat(50));
        }
    }
}