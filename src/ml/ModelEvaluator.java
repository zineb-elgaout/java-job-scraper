package ml;

import weka.classifiers.*;
import weka.classifiers.evaluation.Evaluation;
import weka.core.*;
import java.util.*;
import java.io.FileWriter;
import java.text.DecimalFormat;

public class ModelEvaluator {

    /**
     * Évalue un modèle de classification
     */
    public void evaluateClassificationModel(Classifier classifier,
            Instances trainData,
            Instances testData) throws Exception {
        System.out.println("\n ÉVALUATION DÉTAILLÉE DU MODÈLE");
        System.out.println("═".repeat(70));

        // Évaluation sur l'entraînement
        Evaluation trainEval = new Evaluation(trainData);
        trainEval.evaluateModel(classifier, trainData);

        // Évaluation sur le test
        Evaluation testEval = new Evaluation(testData);
        testEval.evaluateModel(classifier, testData);

        // Afficher les métriques
        printMetrics("ENTRAÎNEMENT", trainEval);
        printMetrics("TEST", testEval);

        // Comparer les performances
        System.out.println("\n COMPARAISON ENTRAINEMENT vs TEST");
        System.out.println("─".repeat(50));
        System.out.printf("%-20s %-15s %-15s\n", "Métrique", "Entraînement", "Test");
        System.out.println("-".repeat(50));
        System.out.printf("%-20s %-14.2f%% %-14.2f%%\n", "Précision",
                trainEval.pctCorrect(), testEval.pctCorrect());
        System.out.printf("%-20s %-14.3f %-14.3f\n", "Précision Moyenne",
                trainEval.weightedPrecision(), testEval.weightedPrecision());
        System.out.printf("%-20s %-14.3f %-14.3f\n", "Rappel Moyen",
                trainEval.weightedRecall(), testEval.weightedRecall());
        System.out.printf("%-20s %-14.3f %-14.3f\n", "F-Mesure Moyenne",
                trainEval.weightedFMeasure(), testEval.weightedFMeasure());
        System.out.printf("%-20s %-14.3f %-14.3f\n", "AUC Moyen",
                trainEval.weightedAreaUnderROC(), testEval.weightedAreaUnderROC());

        // Vérifier le sur-apprentissage
        double overfitting = trainEval.pctCorrect() - testEval.pctCorrect();
        System.out.println("\n  SUR-APPRENTISSAGE : " +
                String.format("%.2f", overfitting) + "%");
        if (overfitting > 10) {
            System.out.println("    Attention : risque de sur-apprentissage élevé !");
        } else if (overfitting > 5) {
            System.out.println("     Sur-apprentissage modéré");
        } else {
            System.out.println("   Bonne généralisation");
        }
    }

    /**
     * Affiche les métriques détaillées
     */
    private void printMetrics(String datasetName, Evaluation eval) {
        System.out.println("\n " + datasetName + " - MÉTRIQUES DÉTAILLÉES");
        System.out.println("─".repeat(50));

        DecimalFormat df = new DecimalFormat("0.000");

        System.out.println("Précision globale : " +
                String.format("%.2f", eval.pctCorrect()) + "%");
        System.out.println("Erreur absolue moyenne : " +
                df.format(eval.meanAbsoluteError()));
        System.out.println("Erreur quadratique moyenne : " +
                df.format(eval.rootMeanSquaredError()));
        System.out.println("Précision moyenne : " +
                df.format(eval.weightedPrecision()));
        System.out.println("Rappel moyen : " +
                df.format(eval.weightedRecall()));
        System.out.println("F-Mesure moyenne : " +
                df.format(eval.weightedFMeasure()));
        System.out.println("AUC moyen : " +
                df.format(eval.weightedAreaUnderROC()));
        System.out.println("Kappa : " +
                df.format(eval.kappa()));

        // Matrice de confusion
        System.out.println("\n MATRICE DE CONFUSION :");
        double[][] matrix = eval.confusionMatrix();
        for (double[] row : matrix) {
            System.out.print("  ");
            for (double val : row) {
                System.out.printf("%5.0f ", val);
            }
            System.out.println();
        }
    }

    /**
     * Génère un rapport d'évaluation
     */
    public Map<String, Object> generateEvaluationReport(Classifier classifier,
            Instances testData) throws Exception {
        Map<String, Object> report = new HashMap<>();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(classifier, testData);

        // Métriques globales
        report.put("accuracy", eval.pctCorrect());
        report.put("precision", eval.weightedPrecision());
        report.put("recall", eval.weightedRecall());
        report.put("fmeasure", eval.weightedFMeasure());
        report.put("auc", eval.weightedAreaUnderROC());
        report.put("kappa", eval.kappa());
        report.put("mae", eval.meanAbsoluteError());
        report.put("rmse", eval.rootMeanSquaredError());

        // Métriques par classe
        Map<String, Map<String, Double>> perClassMetrics = new HashMap<>();
        for (int i = 0; i < testData.classAttribute().numValues(); i++) {
            String className = testData.classAttribute().value(i);
            Map<String, Double> classMetrics = new HashMap<>();
            classMetrics.put("precision", eval.precision(i));
            classMetrics.put("recall", eval.recall(i));
            classMetrics.put("fmeasure", eval.fMeasure(i));
            classMetrics.put("auc", eval.areaUnderROC(i));

            perClassMetrics.put(className, classMetrics);
        }
        report.put("per_class_metrics", perClassMetrics);

        // Matrice de confusion
        report.put("confusion_matrix", eval.confusionMatrix());

        // Prédictions détaillées
        List<Map<String, Object>> predictions = new ArrayList<>();
        for (int i = 0; i < testData.numInstances(); i++) {
            Instance instance = testData.instance(i);
            double actual = instance.classValue();
            double predicted = eval.evaluateModelOnce(classifier, instance);

            Map<String, Object> pred = new HashMap<>();
            pred.put("instance_id", i);
            pred.put("actual", testData.classAttribute().value((int) actual));
            pred.put("predicted", testData.classAttribute().value((int) predicted));
            pred.put("correct", actual == predicted);
            pred.put("distribution", classifier.distributionForInstance(instance));

            predictions.add(pred);
        }
        report.put("predictions", predictions);

        return report;
    }

    /**
     * Exporte le rapport au format CSV
     */
    public void exportReportToCSV(Map<String, Object> report, String filePath) throws Exception {
        try (FileWriter writer = new FileWriter(filePath)) {
            // En-tête
            writer.write("metric,value\n");

            // Métriques globales
            writer.write(String.format("accuracy,%.4f\n", (double) report.get("accuracy")));
            writer.write(String.format("precision,%.4f\n", (double) report.get("precision")));
            writer.write(String.format("recall,%.4f\n", (double) report.get("recall")));
            writer.write(String.format("fmeasure,%.4f\n", (double) report.get("fmeasure")));
            writer.write(String.format("auc,%.4f\n", (double) report.get("auc")));
            writer.write(String.format("kappa,%.4f\n", (double) report.get("kappa")));
            writer.write(String.format("mae,%.4f\n", (double) report.get("mae")));
            writer.write(String.format("rmse,%.4f\n", (double) report.get("rmse")));

            System.out.println(" Rapport exporté : " + filePath);
        }
    }

    /**
     * Affiche les erreurs de prédiction
     */
    public void printPredictionErrors(Classifier classifier, Instances testData) throws Exception {
        System.out.println("\n ERREURS DE PRÉDICTION");
        System.out.println("─".repeat(50));

        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < testData.numInstances(); i++) {
            Instance instance = testData.instance(i);
            double actual = instance.classValue();
            double predicted = classifier.classifyInstance(instance);

            if (actual != predicted) {
                errorCount++;
                String actualClass = testData.classAttribute().value((int) actual);
                String predictedClass = testData.classAttribute().value((int) predicted);

                errors.add(String.format("Instance %d: %s → %s",
                        i, actualClass, predictedClass));

                // Limiter l'affichage aux 10 premières erreurs
                if (errors.size() <= 10) {
                    System.out.printf("  %-40s\n", errors.get(errors.size() - 1));
                }
            }
        }

        double errorRate = (errorCount * 100.0) / testData.numInstances();
        System.out.println("\n STATISTIQUES DES ERREURS :");
        System.out.println("─".repeat(30));
        System.out.println("Nombre total d'erreurs : " + errorCount);
        System.out.println("Taux d'erreur : " + String.format("%.2f", errorRate) + "%");

        if (errors.size() > 10) {
            System.out.println("  ... et " + (errors.size() - 10) + " erreurs supplémentaires");
        }
    }
}