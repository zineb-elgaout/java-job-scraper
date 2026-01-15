package ml;

import weka.classifiers.*;
import weka.classifiers.evaluation.Evaluation;
import weka.core.*;
import java.util.*;
import java.text.DecimalFormat;

public class ModelEvaluator {

    /**
     * Évalue un modèle de classification de manière simple
     */
    public void evaluateClassificationModel(Classifier classifier,
            Instances trainData,
            Instances testData) throws Exception {
        System.out.println("\n ÉVALUATION DU MODÈLE");
        System.out.println("-".repeat(50));

        // Évaluation sur le test
        Evaluation testEval = new Evaluation(testData);
        testEval.evaluateModel(classifier, testData);

        // Afficher les métriques principales
        printSimpleMetrics(testEval);

        // Vérifier le sur-apprentissage
        Evaluation trainEval = new Evaluation(trainData);
        trainEval.evaluateModel(classifier, trainData);
        checkOverfitting(trainEval, testEval);
    }

    /**
     * Affiche les métriques principales
     */
    private void printSimpleMetrics(Evaluation eval) {
        DecimalFormat df = new DecimalFormat("0.000");

        System.out.println("RÉSULTATS PRINCIPAUX :");
        System.out.println("Précision globale : " +
                String.format("%.2f", eval.pctCorrect()) + "%");
        System.out.println("Kappa : " + df.format(eval.kappa()));
        System.out.println("AUC moyen : " + df.format(eval.weightedAreaUnderROC()));

        // Affichage simplifié de la matrice de confusion (seulement pour les petites
        // matrices)
        double[][] matrix = eval.confusionMatrix();
        if (matrix.length <= 5) { // Afficher seulement si 5 classes ou moins
            System.out.println("\nMATRICE DE CONFUSION (simplifiée) :");
            for (double[] row : matrix) {
                System.out.print("  ");
                for (double val : row) {
                    System.out.printf("%5.0f ", val);
                }
                System.out.println();
            }
        }
    }

    /**
     * Vérifie le sur-apprentissage de manière simple
     */
    private void checkOverfitting(Evaluation trainEval, Evaluation testEval) {
        double overfitting = trainEval.pctCorrect() - testEval.pctCorrect();

        System.out.println("\nVÉRIFICATION DU SUR-APPRENTISSAGE :");
        System.out.printf("Différence (train - test) : %.2f%%\n", overfitting);

        if (overfitting > 10) {
            System.out.println("  Risque de sur-apprentissage élevé !");
        } else if (overfitting > 5) {
            System.out.println("  Sur-apprentissage modéré");
        } else {
            System.out.println(" Bonne généralisation");
        }
    }

    /**
     * Évalue rapidement un modèle et retourne les métriques principales
     */
    public Map<String, Object> quickEvaluate(Classifier classifier, Instances testData) throws Exception {
        Map<String, Object> results = new HashMap<>();

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(classifier, testData);

        results.put("accuracy", eval.pctCorrect());
        results.put("kappa", eval.kappa());
        results.put("auc", eval.weightedAreaUnderROC());
        results.put("precision", eval.weightedPrecision());
        results.put("recall", eval.weightedRecall());

        return results;
    }

    /**
     * Affiche une comparaison simple entre plusieurs algorithmes
     */
    public void compareAlgorithmsSimple(Map<String, Classifier> classifiers,
            Instances trainData,
            Instances testData) throws Exception {
        System.out.println("\n COMPARAISON SIMPLE DES ALGORITHMES");
        System.out.println("-".repeat(60));

        System.out.printf("%-20s %-10s %-10s %-10s\n",
                "Algorithme", "Précision", "Kappa", "Temps(ms)");
        System.out.println("-".repeat(60));

        for (Map.Entry<String, Classifier> entry : classifiers.entrySet()) {
            String name = entry.getKey();
            Classifier classifier = entry.getValue();

            try {
                long startTime = System.currentTimeMillis();
                classifier.buildClassifier(trainData);
                long endTime = System.currentTimeMillis();

                Evaluation eval = new Evaluation(testData);
                eval.evaluateModel(classifier, testData);

                System.out.printf("%-20s %-9.2f%% %-9.3f %-9d\n",
                        name, eval.pctCorrect(), eval.kappa(), (endTime - startTime));

            } catch (Exception e) {
                System.out.printf("%-20s %-20s\n", name, "ERREUR");
            }
        }
    }

    /**
     * Affiche un résumé très simple des performances
     */
    public void printSimpleSummary(Classifier classifier, Instances testData) throws Exception {
        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(classifier, testData);

        System.out.println("\n RÉSUMÉ DES PERFORMANCES :");
        System.out.println("-".repeat(40));
        System.out.printf("Précision : %.2f%%\n", eval.pctCorrect());
        System.out.printf("Kappa    : %.3f\n", eval.kappa());
        System.out.printf("AUC      : %.3f\n", eval.weightedAreaUnderROC());

        // Afficher seulement le nombre d'erreurs
        int correct = (int) (eval.correct());
        int total = testData.numInstances();
        System.out.printf("Correct  : %d/%d\n", correct, total);
    }
}