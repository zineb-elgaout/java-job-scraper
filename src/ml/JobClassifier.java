package ml;

import weka.classifiers.*;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.IBk;
import weka.classifiers.functions.Logistic;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.Vote;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.SerializationHelper;

import java.io.File;
import java.util.*;

public class JobClassifier {
    
    /**
     * Entraîne un ensemble de classifieurs (Voting)
     */
    public Classifier trainVotingClassifier(Instances trainData) throws Exception {
        System.out.println("\n ENTRAINEMENT CLASSIFIEUR PAR VOTE");
        System.out.println("-".repeat(40));
        
        Vote classifier = new Vote();
        
        // Créer les classifieurs individuels
        Classifier[] classifiers = {
            new J48(),
            new NaiveBayes(),
            new RandomForest()
        };
        
        classifier.setClassifiers(classifiers);
        
        classifier.setCombinationRule(new SelectedTag(
            Vote.MAJORITY_VOTING_RULE, 
            Vote.TAGS_RULES
        ));
        
        classifier.buildClassifier(trainData);
        
        System.out.println(" Classifieur par vote entraine avec succes");
        System.out.println("   Regle de combinaison : Vote majoritaire");
        System.out.println("   Nombre de classifieurs : " + classifiers.length);
        
        return classifier;
    }
    
    /**
     * Entraîne un classifieur par vote avec regles personnalisees
     */
    public Classifier trainAdvancedVotingClassifier(Instances trainData, String rule) throws Exception {
        System.out.println("\n ENTRAINEMENT CLASSIFIEUR PAR VOTE AVANCE");
        System.out.println("-".repeat(50));
        
        Vote classifier = new Vote();
        
        // Créer un ensemble diversifié de classifieurs
        Classifier[] classifiers = {
            new J48(),
            new RandomForest(),
            new NaiveBayes()
        };
        
        classifier.setClassifiers(classifiers);
        
        // Définir la règle de combinaison
        int ruleCode;
        String ruleName;
        
        switch (rule.toUpperCase()) {
            case "MAJORITY":
                ruleCode = Vote.MAJORITY_VOTING_RULE;
                ruleName = "Vote majoritaire";
                break;
            case "AVERAGE":
                ruleCode = Vote.AVERAGE_RULE;
                ruleName = "Moyenne";
                break;
            case "PRODUCT":
                ruleCode = Vote.PRODUCT_RULE;
                ruleName = "Produit";
                break;
            case "MIN":
                ruleCode = Vote.MIN_RULE;
                ruleName = "Minimum";
                break;
            case "MAX":
                ruleCode = Vote.MAX_RULE;
                ruleName = "Maximum";
                break;
            case "MEDIAN":
                ruleCode = Vote.MEDIAN_RULE;
                ruleName = "Mediane";
                break;
            default:
                ruleCode = Vote.MAJORITY_VOTING_RULE;
                ruleName = "Vote majoritaire (par defaut)";
        }
        
        classifier.setCombinationRule(new SelectedTag(ruleCode, Vote.TAGS_RULES));
        classifier.buildClassifier(trainData);
        
        System.out.println(" Classifieur par vote avance entraine");
        System.out.println("   Regle de combinaison : " + ruleName);
        System.out.println("   Nombre de classifieurs : " + classifiers.length);
        System.out.println("   Classifieurs utilises : J48, RandomForest, NaiveBayes");
        
        return classifier;
    }
    
    /**
     * Entraîne un classifieur J48 (arbre de decision)
     */
    public Classifier trainJ48(Instances trainData) throws Exception {
        System.out.println("\n ENTRAINEMENT J48 (ARBRE DE DECISION)");
        System.out.println("-".repeat(40));
        
        J48 classifier = new J48();
        classifier.setConfidenceFactor(0.25f);
        classifier.setMinNumObj(2);
        classifier.setUnpruned(false);
        classifier.setUseLaplace(true);
        
        classifier.buildClassifier(trainData);
        
        System.out.println(" J48 entraine avec succes");
        System.out.println("   Options : " + Arrays.toString(classifier.getOptions()));
        
        return classifier;
    }
    
    /**
     * Entraîne un classifieur Naive Bayes
     */
    public Classifier trainNaiveBayes(Instances trainData) throws Exception {
        System.out.println("\n ENTRAINEMENT NAIVE BAYES");
        System.out.println("-".repeat(40));
        
        NaiveBayes classifier = new NaiveBayes();
        classifier.buildClassifier(trainData);
        
        System.out.println(" Naive Bayes entraine avec succes");
        
        return classifier;
    }
    
    /**
     * Entraîne un classifieur Random Forest
     */
    public Classifier trainRandomForest(Instances trainData) throws Exception {
        System.out.println("\n ENTRAINEMENT RANDOM FOREST");
        System.out.println("-".repeat(40));
        
        RandomForest classifier = new RandomForest();
        classifier.setNumIterations(7);
        classifier.setNumFeatures(10);
        
        classifier.buildClassifier(trainData);
        
        System.out.println(" Random Forest entraine avec succes");
        
        return classifier;
    }
    
    /**
     * Entraîne un reseau de neurones (Multilayer Perceptron)
     */
    public Classifier trainNeuralNetwork(Instances trainData) throws Exception {
        System.out.println("\n ENTRAINEMENT RESEAU DE NEURONES");
        System.out.println("-".repeat(40));
        
        try {
            MultilayerPerceptron classifier = new MultilayerPerceptron();
            classifier.setLearningRate(0.3);
            classifier.setMomentum(0.2);
            classifier.setTrainingTime(100);
            classifier.setHiddenLayers("3");
            
            classifier.buildClassifier(trainData);
            
            System.out.println(" Reseau de neurones entraine avec succes");
            
            return classifier;
        } catch (Exception e) {
            System.out.println(" Erreur avec reseau de neurones : " + e.getMessage());
            System.out.println(" Utilisation de RandomForest a la place");
            return trainRandomForest(trainData);
        }
    }
    
    /**
     * Entraîne un classifieur k-NN (IBk)
     */
    public Classifier trainKNN(Instances trainData) throws Exception {
        System.out.println("\n ENTRAINEMENT K-NN");
        System.out.println("-".repeat(40));
        
        IBk classifier = new IBk();
        classifier.setKNN(3);
        
        classifier.buildClassifier(trainData);
        
        System.out.println(" k-NN entraine avec succes");
        
        return classifier;
    }
    
    /**
     * Entraîne une regression logistique (avec gestion d'erreur)
     */
    public Classifier trainLogistic(Instances trainData) throws Exception {
        System.out.println("\n ENTRAINEMENT REGRESSION LOGISTIQUE");
        System.out.println("-".repeat(40));
        
        try {
            // Vérifier si les données sont adaptées pour la régression logistique
            if (trainData.classAttribute().numValues() > 10) {
                System.out.println(" Trop de classes (" + trainData.classAttribute().numValues() + 
                                 ") pour la regression logistique");
                System.out.println(" Utilisation de NaiveBayes a la place");
                return trainNaiveBayes(trainData);
            }
            
            Logistic classifier = new Logistic();
            // Réduire le nombre d'itérations pour accélérer
            classifier.setMaxIts(30);
            classifier.buildClassifier(trainData);
            
            System.out.println(" Regression logistique entrainee avec succes");
            
            return classifier;
        } catch (Exception e) {
            System.out.println(" Erreur avec regression logistique : " + e.getMessage());
            System.out.println(" Utilisation de NaiveBayes a la place");
            return trainNaiveBayes(trainData);
        }
    }
    
    /**
     * Évalue un classifieur
     */
    public Evaluation evaluateClassifier(Classifier classifier, Instances trainData, 
                                         Instances testData) throws Exception {
        System.out.println("\n EVALUATION DU CLASSIFIEUR");
        System.out.println("-".repeat(40));
        
        // Évaluation sur l'ensemble d'entraînement
        Evaluation trainEval = new Evaluation(trainData);
        trainEval.evaluateModel(classifier, trainData);
        
        // Évaluation sur l'ensemble de test
        Evaluation testEval = new Evaluation(testData);
        testEval.evaluateModel(classifier, testData);
        
        // Afficher les résultats
        System.out.println(" Evaluation terminee :");
        System.out.println("   Precision sur l'entrainement : " + 
                          String.format("%.2f", trainEval.pctCorrect()) + "%");
        System.out.println("   Precision sur le test : " + 
                          String.format("%.2f", testEval.pctCorrect()) + "%");
        
        return testEval;
    }
    
    /**
     * Validation croisée
     */
    public Evaluation crossValidate(Classifier classifier, Instances data, int folds) throws Exception {
        System.out.println("\n VALIDATION CROISEE (" + folds + " folds)");
        System.out.println("-".repeat(40));
        
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(classifier, data, folds, new Random(42));
        
        System.out.println(" Validation croisee terminee :");
        System.out.println("   Precision moyenne : " + 
                          String.format("%.2f", eval.pctCorrect()) + "%");
        
        return eval;
    }
    
    /**
     * Compare plusieurs algorithmes avec gestion d'erreur améliorée
     */
    public Map<String, Double> compareAlgorithms(Instances trainData, Instances testData) {
        System.out.println("\n  COMPARAISON DES ALGORITHMES");
        System.out.println("=".repeat(50));
        
        Map<String, Classifier> classifiers = new LinkedHashMap<>();
        classifiers.put("J48", new J48());
        classifiers.put("NaiveBayes", new NaiveBayes());
        classifiers.put("RandomForest", new RandomForest());
        classifiers.put("k-NN", new IBk());
        
        // Essayer Logistic seulement si peu de classes
        if (trainData.classAttribute().numValues() <= 5) {
            classifiers.put("Logistic", new Logistic());
        }
        
        // Essayer Neural Network séparément car peut échouer
        classifiers.put("NeuralNetwork", new MultilayerPerceptron());
        
        Map<String, Double> results = new TreeMap<>();
        
        for (Map.Entry<String, Classifier> entry : classifiers.entrySet()) {
            String name = entry.getKey();
            Classifier classifier = entry.getValue();
            
            System.out.println("\n " + name + " :");
            System.out.println("-".repeat(20));
            
            try {
                // Configuration spécifique pour certains algorithmes
                if (name.equals("RandomForest")) {
                    ((RandomForest) classifier).setNumIterations(30);
                } else if (name.equals("k-NN")) {
                    ((IBk) classifier).setKNN(3);
                } else if (name.equals("Logistic")) {
                    ((Logistic) classifier).setMaxIts(50);
                } else if (name.equals("NeuralNetwork")) {
                    ((MultilayerPerceptron) classifier).setTrainingTime(30);
                    ((MultilayerPerceptron) classifier).setHiddenLayers("3");
                }
                
                long startTime = System.currentTimeMillis();
                classifier.buildClassifier(trainData);
                long endTime = System.currentTimeMillis();
                
                Evaluation eval = new Evaluation(testData);
                eval.evaluateModel(classifier, testData);
                
                double accuracy = eval.pctCorrect();
                results.put(name, accuracy);
                
                System.out.println("   Precision : " + String.format("%.2f", accuracy) + "%");
                System.out.println("   Temps d'entrainement : " + (endTime - startTime) + "ms");
                
            } catch (Exception e) {
                System.out.println("   Erreur : " + e.getClass().getSimpleName());
                System.out.println("   Message : " + e.getMessage());
                results.put(name, 0.0);
            }
        }
        
        // Afficher le tableau comparatif
        System.out.println("\n RESULTATS COMPARATIFS :");
        System.out.println("-".repeat(40));
        System.out.printf("%-15s %-10s\n", "Algorithme", "Precision");
        System.out.println("-".repeat(25));
        
        for (Map.Entry<String, Double> entry : results.entrySet()) {
            System.out.printf("%-15s %-9.2f%%\n", entry.getKey(), entry.getValue());
        }
        
        // Trouver le meilleur algorithme
        String bestAlgorithm = "";
        double bestAccuracy = 0;
        
        for (Map.Entry<String, Double> entry : results.entrySet()) {
            if (entry.getValue() > bestAccuracy) {
                bestAccuracy = entry.getValue();
                bestAlgorithm = entry.getKey();
            }
        }
        
        if (bestAlgorithm.isEmpty()) {
            System.out.println("\n AUCUN ALGORITHME N'A FONCTIONNE CORRECTEMENT");
        } else {
            System.out.println("\n MEILLEUR ALGORITHME : " + bestAlgorithm + 
                              " (" + String.format("%.2f", bestAccuracy) + "%)");
        }
        
        return results;
    }
    
    /**
     * Sauvegarde un modèle entraîné
     */
    public void saveModel(Classifier classifier, String filePath) throws Exception {
        // Créer le dossier models s'il n'existe pas
        if (!filePath.contains("/") && !filePath.contains("\\")) {
            filePath = "models/" + filePath;
        }
        
        // Créer le dossier parent si nécessaire
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        if (lastSlash != -1) {
            String dir = filePath.substring(0, lastSlash);
            new File(dir).mkdirs();
        }
        
        SerializationHelper.write(filePath, classifier);
        System.out.println(" Modele sauvegarde : " + filePath);
    }
    
    /**
     * Charge un modèle sauvegardé
     */
    public Classifier loadModel(String filePath) throws Exception {
        // Essayer différents chemins
        File file = new File(filePath);
        if (!file.exists()) {
            if (new File("models/" + filePath).exists()) {
                filePath = "models/" + filePath;
            }
        }
        
        Classifier classifier = (Classifier) SerializationHelper.read(filePath);
        System.out.println(" Modele charge : " + filePath);
        System.out.println("   Type : " + classifier.getClass().getSimpleName());
        
        return classifier;
    }
    
    /**
     * Affiche les détails d'une évaluation
     */
    public void printEvaluationDetails(Evaluation eval, Instances data) {
        System.out.println("\n DETAILS DE L'EVALUATION :");
        System.out.println("=".repeat(50));
        
        System.out.println("Precision globale : " + 
                          String.format("%.2f", eval.pctCorrect()) + "%");
        System.out.println("Erreur absolue moyenne : " + 
                          String.format("%.3f", eval.meanAbsoluteError()));
        System.out.println("Kappa : " + 
                          String.format("%.3f", eval.kappa()));
        System.out.println("AUC moyen : " + 
                          String.format("%.3f", eval.weightedAreaUnderROC()));
        
        // Matrice de confusion
        System.out.println("\n MATRICE DE CONFUSION :");
        double[][] matrix = eval.confusionMatrix();
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("  ");
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%5.0f ", matrix[i][j]);
            }
            System.out.println();
        }
        
        // Détails par classe
        System.out.println("\n PERFORMANCE PAR CLASSE :");
        System.out.println("-".repeat(60));
        System.out.printf("%-20s %-10s %-10s %-10s\n", 
            "Classe", "Precision", "Rappel", "F-Mesure");
        System.out.println("-".repeat(60));
        
        for (int i = 0; i < data.classAttribute().numValues(); i++) {
            String className = data.classAttribute().value(i);
            double precision = eval.precision(i);
            double recall = eval.recall(i);
            double fMeasure = eval.fMeasure(i);
            
            System.out.printf("%-20s %-9.3f %-9.3f %-9.3f\n",
                className, precision, recall, fMeasure);
        }
    }
}