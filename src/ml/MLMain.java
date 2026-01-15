package ml;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class MLMain {
    public static Scanner scanner = new Scanner(System.in);
    public static JobDataLoader dataLoader = new JobDataLoader();
    public static JobClassifier jobClassifier = new JobClassifier();
    public static ModelEvaluator modelEvaluator = new ModelEvaluator();

    public static Instances dataset;
    public static Map<String, Instances> trainTestSplit;
    public static Classifier trainedClassifier;

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
        System.out.println("========================================");
        System.out.println("   SYSTEME DE MACHINE LEARNING");
        System.out.println("   Analyse d'offres d'emploi");
        System.out.println("========================================");

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
                        modelsManagementMenu();
                        break;
                    case 4:
                        runQuickTests();
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

        System.out.println("\n========================================");
        System.out.println("   FIN DE L'EXECUTION");
        System.out.println("========================================");
        scanner.close();
    }

    public static void showMainMenu() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1. Charger et préparer les données");
        System.out.println("2. Classification");
        System.out.println("3. Gestion des modèles");
        System.out.println("4. Tests rapides");
        System.out.println("0. Quitter");
    }

    public static void loadAndPrepareData() throws Exception {
        System.out.println("\n=== CHARGEMENT DES DONNEES ===");

        // Vérifier si le fichier existe
        dataLoader.checkDataFile();

        // Charger les données
        System.out.println("\nChargement des données...");
        dataset = dataLoader.loadDataset();

        // Afficher les statistiques
        dataLoader.printDataStatistics(dataset);

        // Préparer pour la classification
        System.out.println("\nPreparation des données...");
        dataset = dataLoader.prepareForClassification(dataset);

        // Diviser les données (80/20 par défaut)
        double percentage = 80.0;
        System.out.println("Division des données (" + percentage + "% train, " + (100 - percentage) + "% test)...");
        trainTestSplit = dataLoader.splitTrainTest(dataset, percentage);

        System.out.println("\n Données prêtes !");
        System.out.println("   Total : " + dataset.numInstances() + " instances");
        System.out.println("   Entrainement : " + trainTestSplit.get("train").numInstances() + " instances");
        System.out.println("   Test : " + trainTestSplit.get("test").numInstances() + " instances");
    }

    public static void classificationMenu() throws Exception {
        if (trainTestSplit == null) {
            System.out.println("Veuillez d'abord charger les données (option 1).");
            return;
        }

        System.out.println("\n=== CLASSIFICATION ===");
        System.out.println("1. Comparer tous les algorithmes");
        System.out.println("2. Entraîner J48 (arbre de décision)");
        System.out.println("3. Entraîner Naive Bayes");
        System.out.println("4. Entraîner Random Forest");
        System.out.println("5. Entraîner k-NN");
        System.out.println("6. Retour");

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
                return;
            default:
                System.out.println("Choix invalide.");
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
            default:
                System.out.println("Algorithme non reconnu : " + algorithmName);
                return;
        }

        if (classifier != null) {
            trainedClassifier = classifier;

            // Évaluation simple
            System.out.println("\n--- EVALUATION ---");
            Evaluation eval = jobClassifier.evaluateClassifier(classifier,
                    trainTestSplit.get("train"), trainTestSplit.get("test"));

            System.out.println("\nPERFORMANCES :");
            System.out.println("   Précision : " + String.format("%.2f", eval.pctCorrect()) + "%");
            System.out.println("   Kappa : " + String.format("%.3f", eval.kappa()));

            // Sauvegarde automatique
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "models/" + algorithmName.toLowerCase() + "_" + timestamp + ".model";

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
        }
    }

    public static void modelsManagementMenu() {
        System.out.println("\n=== GESTION DES MODELES ===");
        System.out.println("1. Lister les modèles disponibles");
        System.out.println("2. Charger un modèle");
        System.out.println("3. Retour");

        int choice = getIntInput("\nVotre choix : ");

        switch (choice) {
            case 1:
                listModels();
                break;
            case 2:
                loadModel();
                break;
            case 3:
                return;
            default:
                System.out.println("Choix invalide.");
        }
    }

    public static void listModels() {
        File modelsDir = new File("models");
        if (modelsDir.exists() && modelsDir.listFiles() != null) {
            File[] modelFiles = modelsDir.listFiles((dir, name) -> name.endsWith(".model"));

            if (modelFiles != null && modelFiles.length > 0) {
                System.out.println("\n MODÈLES DISPONIBLES (" + modelFiles.length + ") :");
                System.out.println("=".repeat(50));

                // Trier par date de modification
                Arrays.sort(modelFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                for (File file : modelFiles) {
                    double sizeKB = file.length() / 1024.0;
                    String date = new SimpleDateFormat("dd/MM/yyyy HH:mm")
                            .format(new Date(file.lastModified()));
                    System.out.printf("  %-35s %6.1f KB  %s\n", file.getName(), sizeKB, date);
                }
            } else {
                System.out.println("Aucun modèle disponible dans le dossier 'models'.");
            }
        } else {
            System.out.println("Dossier 'models' non trouvé.");
        }
    }

    public static void loadModel() {
        System.out.print("Nom du modèle (sans extension) : ");
        String modelName = scanner.nextLine().trim();

        // Essayer plusieurs chemins
        String[] possiblePaths = {
                modelName,
                modelName + ".model",
                "models/" + modelName,
                "models/" + modelName + ".model"
        };

        for (String path : possiblePaths) {
            if (new File(path).exists()) {
                try {
                    trainedClassifier = jobClassifier.loadModel(path);
                    System.out.println(" Modèle chargé : " + path);
                    return;
                } catch (Exception e) {
                    System.out.println("Erreur avec " + path + ": " + e.getMessage());
                }
            }
        }

        System.out.println(" Modèle non trouvé. Essayer: models/nom_du_modele.model");
    }

    public static void runQuickTests() throws Exception {
        if (dataset == null) {
            System.out.println("Veuillez d'abord charger les données (option 1).");
            return;
        }

        System.out.println("\n=== TESTS RAPIDES ===");
        System.out.print("Exécuter les tests ? (o/n) : ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (!response.equals("o") && !response.equals("oui")) {
            System.out.println("Tests annulés.");
            return;
        }

        System.out.println("\n1. Test de classification...");
        jobClassifier.compareAlgorithms(trainTestSplit.get("train"), trainTestSplit.get("test"));

        System.out.println("\n Tests terminés !");
    }
}