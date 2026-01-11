package ml;

import weka.classifiers.*;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;
import java.util.*;

public class PredictionService {
    private Classifier classifier;
    private Instances dataStructure;

    /**
     * Charge un modèle sauvegardé
     */
    public void loadModel(String modelPath, String dataStructurePath) throws Exception {
        classifier = (Classifier) SerializationHelper.read(modelPath);
        dataStructure = new DataSource(dataStructurePath).getDataSet();
        dataStructure.setClassIndex(dataStructure.numAttributes() - 1);

        System.out.println("Modèle chargé : " + modelPath);
        System.out.println("   Type : " + classifier.getClass().getSimpleName());
    }

    /**
     * Prédit la catégorie d'une nouvelle offre
     */
    public String predictJobCategory(Map<String, Object> jobFeatures) throws Exception {
        // Créer une nouvelle instance
        Instance newInstance = createInstanceFromFeatures(jobFeatures);

        // Faire la prédiction
        double prediction = classifier.classifyInstance(newInstance);
        String predictedCategory = dataStructure.classAttribute().value((int) prediction);

        // Obtenir la distribution de probabilité
        double[] distribution = classifier.distributionForInstance(newInstance);

        System.out.println("\n🔮 PRÉDICTION :");
        System.out.println("─".repeat(30));
        System.out.println("Catégorie prédite : " + predictedCategory);
        System.out.println("\n DISTRIBUTION DE PROBABILITÉ :");

        // Afficher les 3 meilleures prédictions
        Map<Double, String> probabilityMap = new TreeMap<>(Collections.reverseOrder());
        for (int i = 0; i < distribution.length; i++) {
            probabilityMap.put(distribution[i],
                    dataStructure.classAttribute().value(i));
        }

        int count = 0;
        for (Map.Entry<Double, String> entry : probabilityMap.entrySet()) {
            if (count < 3) {
                System.out.printf("  %-25s : %.1f%%\n",
                        entry.getValue(), entry.getKey() * 100);
                count++;
            } else {
                break;
            }
        }

        return predictedCategory;
    }

    /**
     * Prédit pour plusieurs offres
     */
    public List<Map<String, Object>> batchPredict(List<Map<String, Object>> jobFeaturesList)
            throws Exception {
        List<Map<String, Object>> predictions = new ArrayList<>();

        for (int i = 0; i < jobFeaturesList.size(); i++) {
            Map<String, Object> features = jobFeaturesList.get(i);

            try {
                Instance instance = createInstanceFromFeatures(features);
                double prediction = classifier.classifyInstance(instance);
                String predictedCategory = dataStructure.classAttribute().value((int) prediction);
                double[] distribution = classifier.distributionForInstance(instance);

                Map<String, Object> result = new HashMap<>();
                result.put("job_id", i);
                result.put("features", features);
                result.put("predicted_category", predictedCategory);
                result.put("confidence", Arrays.stream(distribution).max().orElse(0));
                result.put("distribution", distribution);

                predictions.add(result);

            } catch (Exception e) {
                System.err.println(" Erreur de prédiction pour l'offre " + i + ": " +
                        e.getMessage());
            }
        }

        return predictions;
    }

    /**
     * Crée une instance Weka à partir des features
     */
    private Instance createInstanceFromFeatures(Map<String, Object> features) {
        // Créer une nouvelle instance avec la même structure que les données
        // d'entraînement
        Instance instance = new DenseInstance(dataStructure.numAttributes());
        instance.setDataset(dataStructure);

        // Remplir les valeurs
        for (int i = 0; i < dataStructure.numAttributes(); i++) {
            String attrName = dataStructure.attribute(i).name();

            if (features.containsKey(attrName)) {
                Object value = features.get(attrName);

                if (dataStructure.attribute(i).isNumeric()) {
                    instance.setValue(i, Double.parseDouble(value.toString()));
                } else if (dataStructure.attribute(i).isNominal()) {
                    // Vérifier si la valeur existe dans les valeurs nominales
                    String strValue = value.toString();
                    if (dataStructure.attribute(i).indexOfValue(strValue) != -1) {
                        instance.setValue(i, strValue);
                    } else {
                        // Valeur manquante si non trouvée
                        instance.setMissing(i);
                    }
                }
            } else {
                // Valeur manquante
                instance.setMissing(i);
            }
        }

        return instance;
    }

    /**
     * Évalue la performance sur un ensemble de test
     */
    public void evaluateOnTestSet(String testDataPath) throws Exception {
        Instances testData = new DataSource(testDataPath).getDataSet();
        testData.setClassIndex(testData.numAttributes() - 1);

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(classifier, testData);

        System.out.println("\n ÉVALUATION SUR NOUVELLES DONNÉES :");
        System.out.println("─".repeat(40));
        System.out.println("Précision : " +
                String.format("%.2f", eval.pctCorrect()) + "%");
        System.out.println("Nombre d'instances : " + testData.numInstances());

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
     * Génère des recommandations basées sur la prédiction
     */
    public Map<String, Object> generateRecommendations(String predictedCategory) {
        Map<String, Object> recommendations = new HashMap<>();

        // Recommandations par catégorie (exemple)
        Map<String, List<String>> categoryAdvice = new HashMap<>();

        categoryAdvice.put("IT_DEVELOPPEMENT", Arrays.asList(
                "Mettez à jour vos compétences en frameworks modernes",
                "Participez à des projets open source",
                "Obtenez des certifications cloud (AWS, Azure)",
                "Développez votre portfolio GitHub"));

        categoryAdvice.put("COMMERCIAL_VENTE", Arrays.asList(
                "Améliorez vos techniques de négociation",
                "Développez votre réseau professionnel",
                "Suivez une formation en CRM",
                "Participez à des salons professionnels"));

        categoryAdvice.put("RESSOURCES_HUMAINES", Arrays.asList(
                "Formez-vous aux nouvelles lois du travail",
                "Développez vos compétences en psychologie",
                "Maîtrisez les outils de recrutement digitaux",
                "Participez à des conférences RH"));

        // Ajouter d'autres catégories...

        if (categoryAdvice.containsKey(predictedCategory)) {
            recommendations.put("conseils", categoryAdvice.get(predictedCategory));
            recommendations.put("formations_suggerees", suggestTrainings(predictedCategory));
            recommendations.put("competences_cles", suggestSkills(predictedCategory));
        } else {
            recommendations.put("conseils", Arrays.asList(
                    "Consultez les offres similaires sur les plateformes d'emploi",
                    "Adaptez votre CV aux exigences du marché",
                    "Développez des compétences transversales"));
        }

        return recommendations;
    }

    private List<String> suggestTrainings(String category) {
        // Suggestions de formations par catégorie
        Map<String, List<String>> trainings = new HashMap<>();

        trainings.put("IT_DEVELOPPEMENT", Arrays.asList(
                "Formation Full Stack Development",
                "Certification AWS Solutions Architect",
                "Cours de Machine Learning",
                "Formation DevOps"));

        trainings.put("DATA_IA", Arrays.asList(
                "Formation Data Science",
                "Certification TensorFlow",
                "Cours de Big Data",
                "Formation en statistiques avancées"));

        return trainings.getOrDefault(category, Arrays.asList(
                "Formation continue dans votre domaine",
                "Développement des compétences transversales"));
    }

    private List<String> suggestSkills(String category) {
        // Compétences clés par catégorie
        Map<String, List<String>> skills = new HashMap<>();

        skills.put("IT_DEVELOPPEMENT", Arrays.asList(
                "Programmation (Java, Python, JavaScript)",
                "Frameworks (Spring, React, Angular)",
                "Bases de données (SQL, NoSQL)",
                "DevOps et outils cloud"));

        skills.put("LOGISTIQUE_TRANSPORT", Arrays.asList(
                "Gestion de la supply chain",
                "Planification et optimisation",
                "Gestion des stocks",
                "Logistique internationale"));

        return skills.getOrDefault(category, Arrays.asList(
                "Compétences analytiques",
                "Communication efficace",
                "Résolution de problèmes",
                "Adaptabilité"));
    }
}