package ml;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.instance.RemovePercentage;
import java.io.*;
import java.util.*;

public class JobDataLoader {
    // Chemin unique vers votre fichier ARFF
    private static final String DATA_FILE = "C:\\Users\\DELL\\Desktop\\projet_java\\java_scrap\\data\\06_weka_ready_fixed.arff";
    
    /**
     * Charge les données depuis le fichier ARFF
     */
    public Instances loadDataset() throws Exception {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("ERREUR: Fichier de donnees non trouve : " + DATA_FILE);
            System.out.println("Chemin absolu : " + file.getAbsolutePath());
            throw new FileNotFoundException("Fichier de donnees non trouve : " + DATA_FILE);
        }
        
        DataSource source = new DataSource(DATA_FILE);
        Instances data = source.getDataSet();
        
        // Définir l'attribut de classe (la dernière colonne par défaut)
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }
        
        System.out.println("Donnees chargees depuis : " + DATA_FILE);
        System.out.println("   Nombre d'instances : " + data.numInstances());
        System.out.println("   Nombre d'attributs : " + data.numAttributes());
        System.out.println("   Attribut de classe : " + data.classAttribute().name());
        
        return data;
    }
    
    /**
     * Vérifie si le fichier de données existe
     */
    public void checkDataFile() {
        File file = new File(DATA_FILE);
        boolean exists = file.exists();
        
        System.out.println("\n=== VERIFICATION DU FICHIER DE DONNEES ===");
        System.out.println("Fichier : " + DATA_FILE);
        
        if (exists) {
            System.out.println("STATUS : TROUVE (" + file.length() + " bytes)");
        } else {
            System.out.println("STATUS : MANQUANT");
            System.out.println("Chemin absolu verifie : " + file.getAbsolutePath());
        }
    }
    
    /**
     * Prépare les données pour la classification
     */
    public Instances prepareForClassification(Instances data) throws Exception {
        // S'assurer qu'un attribut de classe est défini
        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }
        
        System.out.println("Attribut de classe : " + data.classAttribute().name());
        
        // Convertir les attributs numériques en nominaux si nécessaire
        for (int i = 0; i < data.numAttributes(); i++) {
            if (data.attribute(i).isNumeric() && data.attribute(i).numValues() < 10) {
                NumericToNominal convert = new NumericToNominal();
                convert.setAttributeIndices(String.valueOf(i + 1));
                convert.setInputFormat(data);
                data = Filter.useFilter(data, convert);
            }
        }
        
        return data;
    }
    
    /**
     * Divise les données en ensembles d'entraînement et de test
     */
    public Map<String, Instances> splitTrainTest(Instances data, double trainPercentage) throws Exception {
        Map<String, Instances> result = new HashMap<>();
        
        // Randomiser les données
        data.randomize(new Random(42));
        
        // Diviser
        RemovePercentage splitFilter = new RemovePercentage();
        splitFilter.setPercentage(trainPercentage);
        splitFilter.setInvertSelection(true);
        splitFilter.setInputFormat(data);
        Instances trainData = Filter.useFilter(data, splitFilter);
        
        splitFilter.setInvertSelection(false);
        splitFilter.setInputFormat(data);
        Instances testData = Filter.useFilter(data, splitFilter);
        
        result.put("train", trainData);
        result.put("test", testData);
        
        System.out.println("\n=== DIVISION DES DONNEES ===");
        System.out.println("   Entrainement : " + trainData.numInstances() + " instances");
        System.out.println("   Test : " + testData.numInstances() + " instances");
        System.out.println("   Pourcentage entrainement : " + trainPercentage + "%");
        
        return result;
    }
    
    /**
     * Affiche les statistiques des données
     */
    public void printDataStatistics(Instances data) {
        System.out.println("\n=== STATISTIQUES DES DONNEES ===");
        System.out.println("Nombre total d'instances : " + data.numInstances());
        System.out.println("Nombre d'attributs : " + data.numAttributes());
        System.out.println("Attribut de classe : " + data.classAttribute().name());
        
        // Distribution des classes
        System.out.println("\n=== DISTRIBUTION DES CLASSES ===");
        int[] classCounts = data.attributeStats(data.classIndex()).nominalCounts;
        for (int i = 0; i < data.classAttribute().numValues(); i++) {
            String className = data.classAttribute().value(i);
            int count = classCounts[i];
            double percentage = (count * 100.0) / data.numInstances();
            System.out.printf("  %-25s : %4d (%.1f%%)\n", className, count, percentage);
        }
        
        // Statistiques des attributs
        System.out.println("\n=== STATISTIQUES DES ATTRIBUTS ===");
        for (int i = 0; i < data.numAttributes(); i++) {
            if (i != data.classIndex()) {
                System.out.printf("  %-20s : %s\n", 
                    data.attribute(i).name(),
                    data.attribute(i).isNumeric() ? "Numerique" : 
                    data.attribute(i).isNominal() ? "Nominal (" + data.attribute(i).numValues() + " valeurs)" :
                    data.attribute(i).type() == 1 ? "String" : "Inconnu");
            }
        }
    }
}