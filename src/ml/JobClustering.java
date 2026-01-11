package ml;

import weka.clusterers.*;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.EuclideanDistance;
import java.util.*;

public class JobClustering {
    
    /**
     * Applique K-Means clustering sur des données sans attribut de classe
     */
    public Clusterer applyKMeans(Instances data, int k) throws Exception {
        System.out.println("\n K-MEANS CLUSTERING (k=" + k + ")");
        System.out.println("-".repeat(40));
        
        // Créer une copie des données sans l'attribut de classe
        Instances dataForClustering = new Instances(data);
        if (dataForClustering.classIndex() >= 0) {
            dataForClustering.setClassIndex(-1); // Pas d'attribut de classe
        }
        
        SimpleKMeans clusterer = new SimpleKMeans();
        clusterer.setNumClusters(k);
        clusterer.setSeed(42);
        clusterer.setPreserveInstancesOrder(true);
        
        // Utiliser la distance euclidienne
        EuclideanDistance dist = new EuclideanDistance(dataForClustering);
        clusterer.setDistanceFunction(dist);
        
        clusterer.buildClusterer(dataForClustering);
        
        System.out.println(" Clustering K-Means termine");
        System.out.println("   Nombre de clusters : " + clusterer.getNumClusters());
        System.out.println("   SSE (somme des erreurs carrees) : " + 
                          String.format("%.2f", clusterer.getSquaredError()));
        
        return clusterer;
    }
    
    /**
     * Applique EM clustering sur des données sans attribut de classe
     */
    public Clusterer applyEM(Instances data, int maxClusters) throws Exception {
        System.out.println("\n EM CLUSTERING");
        System.out.println("-".repeat(40));
        
        // Créer une copie des données sans l'attribut de classe
        Instances dataForClustering = new Instances(data);
        if (dataForClustering.classIndex() >= 0) {
            dataForClustering.setClassIndex(-1);
        }
        
        EM clusterer = new EM();
        clusterer.setNumClusters(maxClusters);
        clusterer.setSeed(42);
        
        clusterer.buildClusterer(dataForClustering);
        
        System.out.println(" Clustering EM termine");
        System.out.println("   Nombre de clusters trouves : " + clusterer.numberOfClusters());
        
        return clusterer;
    }
    
    /**
     * Applique Hierarchical Clustering sur des données sans attribut de classe
     */
    public Clusterer applyHierarchicalClustering(Instances data, int numClusters) throws Exception {
        System.out.println("\n HIERARCHICAL CLUSTERING");
        System.out.println("-".repeat(40));
        
        // Créer une copie des données sans l'attribut de classe
        Instances dataForClustering = new Instances(data);
        if (dataForClustering.classIndex() >= 0) {
            dataForClustering.setClassIndex(-1);
        }
        
        HierarchicalClusterer clusterer = new HierarchicalClusterer();
        clusterer.setNumClusters(numClusters);
        clusterer.setDistanceFunction(new weka.core.EuclideanDistance());
        clusterer.setLinkType(new SelectedTag(1, HierarchicalClusterer.TAGS_LINK_TYPE));
        
        clusterer.buildClusterer(dataForClustering);
        
        System.out.println(" Clustering hierarchique termine");
        System.out.println("   Nombre de clusters : " + numClusters);
        
        return clusterer;
    }
    
    /**
     * Applique XMeans clustering (alternative à DBSCAN)
     */
    public Clusterer applyXMeans(Instances data) throws Exception {
        System.out.println("\n X-MEANS CLUSTERING");
        System.out.println("-".repeat(40));
        
        // Créer une copie des données sans l'attribut de classe
        Instances dataForClustering = new Instances(data);
        if (dataForClustering.classIndex() >= 0) {
            dataForClustering.setClassIndex(-1);
        }
        
        // Utilisons SimpleKMeans avec recherche du k optimal
        System.out.println("  XMeans non disponible, utilisation de K-Means avec recherche du k optimal");
        
        Map<Integer, Double> elbowResults = findOptimalK(dataForClustering, 10);
        int optimalK = findElbowPoint(elbowResults);
        
        return applyKMeans(data, optimalK);
    }
    
    /**
     * Trouve le point de coude (elbow point)
     */
    public int findElbowPoint(Map<Integer, Double> sseValues) {
        int optimalK = 2;
        double maxDrop = 0;
        
        List<Integer> ks = new ArrayList<>(sseValues.keySet());
        Collections.sort(ks);
        
        for (int i = 1; i < ks.size() - 1; i++) {
            int k = ks.get(i);
            double prevDrop = sseValues.get(k-1) - sseValues.get(k);
            double nextDrop = sseValues.get(k) - sseValues.get(k+1);
            double dropRatio = prevDrop / nextDrop;
            
            if (dropRatio > maxDrop) {
                maxDrop = dropRatio;
                optimalK = k;
            }
        }
        
        return optimalK;
    }
    
    /**
     * Analyse les clusters
     */
    public void analyzeClusters(Clusterer clusterer, Instances data) throws Exception {
        System.out.println("\n ANALYSE DES CLUSTERS");
        System.out.println("-".repeat(40));
        
        // Créer une copie des données sans l'attribut de classe
        Instances dataForClustering = new Instances(data);
        if (dataForClustering.classIndex() >= 0) {
            dataForClustering.setClassIndex(-1);
        }
        
        // Distribution des instances dans les clusters
        Map<Integer, Integer> clusterDistribution = new HashMap<>();
        for (int i = 0; i < dataForClustering.numInstances(); i++) {
            int cluster = clusterer.clusterInstance(dataForClustering.instance(i));
            clusterDistribution.put(cluster, 
                clusterDistribution.getOrDefault(cluster, 0) + 1);
        }
        
        System.out.println(" DISTRIBUTION DES CLUSTERS :");
        System.out.println("-".repeat(30));
        for (Map.Entry<Integer, Integer> entry : clusterDistribution.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / dataForClustering.numInstances();
            System.out.printf("  Cluster %-2d : %4d instances (%.1f%%)\n", 
                entry.getKey(), entry.getValue(), percentage);
        }
        
        // Centres des clusters (pour K-Means)
        if (clusterer instanceof SimpleKMeans) {
            SimpleKMeans kmeans = (SimpleKMeans) clusterer;
            System.out.println("\n CENTRES DES CLUSTERS :");
            
            Instances centroids = kmeans.getClusterCentroids();
            for (int i = 0; i < centroids.numInstances(); i++) {
                System.out.println("\n  Cluster " + i + " :");
                for (int j = 0; j < centroids.numAttributes(); j++) {
                    System.out.printf("    %-20s : %.3f\n", 
                        centroids.attribute(j).name(),
                        centroids.instance(i).value(j));
                }
            }
        }
        
        // Silhouette Score (approximation)
        System.out.println("\n QUALITE DU CLUSTERING :");
        System.out.println("-".repeat(30));
        
        if (clusterer instanceof SimpleKMeans) {
            SimpleKMeans kmeans = (SimpleKMeans) clusterer;
            double sse = kmeans.getSquaredError();
            System.out.println("  SSE (somme des erreurs carrees) : " + 
                              String.format("%.2f", sse));
        }
    }
    
    /**
     * Trouve le nombre optimal de clusters avec la méthode du coude
     */
    public Map<Integer, Double> findOptimalK(Instances data, int maxK) throws Exception {
        System.out.println("\n RECHERCHE DU NOMBRE OPTIMAL DE CLUSTERS");
        System.out.println("-".repeat(50));
        
        // Créer une copie des données sans l'attribut de classe
        Instances dataForClustering = new Instances(data);
        if (dataForClustering.classIndex() >= 0) {
            dataForClustering.setClassIndex(-1);
        }
        
        Map<Integer, Double> sseValues = new TreeMap<>();
        
        for (int k = 2; k <= maxK; k++) {
            SimpleKMeans kmeans = new SimpleKMeans();
            kmeans.setNumClusters(k);
            kmeans.setSeed(42);
            kmeans.buildClusterer(dataForClustering);
            
            double sse = kmeans.getSquaredError();
            sseValues.put(k, sse);
            
            System.out.printf("  k=%-2d : SSE = %.2f\n", k, sse);
        }
        
        // Trouver le point de coude
        int optimalK = findElbowPoint(sseValues);
        
        System.out.println("\n K OPTIMAL SUGGERE : " + optimalK);
        
        return sseValues;
    }
    
    /**
     * Visualise les clusters (statistiques)
     */
    public void visualizeClusters(Clusterer clusterer, Instances data) throws Exception {
        System.out.println("\n VISUALISATION DES CLUSTERS");
        System.out.println("=".repeat(70));
        
        // Créer une copie des données sans l'attribut de classe
        Instances dataForClustering = new Instances(data);
        if (dataForClustering.classIndex() >= 0) {
            dataForClustering.setClassIndex(-1);
        }
        
        // Pour K-Means, analyser les caractéristiques des centroïdes
        if (clusterer instanceof SimpleKMeans) {
            SimpleKMeans kmeans = (SimpleKMeans) clusterer;
            int numClusters = kmeans.getNumClusters();
            Instances centroids = kmeans.getClusterCentroids();
            
            for (int cluster = 0; cluster < numClusters; cluster++) {
                System.out.println("\n CLUSTER " + cluster + " :");
                System.out.println("-".repeat(30));
                
                // Caractéristiques principales du cluster
                List<Map.Entry<String, Double>> features = new ArrayList<>();
                for (int attr = 0; attr < centroids.numAttributes(); attr++) {
                    double value = centroids.instance(cluster).value(attr);
                    features.add(new AbstractMap.SimpleEntry<>(
                        centroids.attribute(attr).name(), value));
                }
                
                // Trier par valeur absolue décroissante
                features.sort((a, b) -> Double.compare(
                    Math.abs(b.getValue()), Math.abs(a.getValue())));
                
                // Afficher les 5 principales caractéristiques
                for (int i = 0; i < Math.min(5, features.size()); i++) {
                    Map.Entry<String, Double> feature = features.get(i);
                    System.out.printf("  %-25s : %.3f\n", 
                        feature.getKey(), feature.getValue());
                }
            }
        } else {
            System.out.println("  Visualisation detaillee disponible uniquement pour K-Means");
        }
    }
    
    /**
     * Applique plusieurs algorithmes de clustering et compare
     */
    public Map<String, Clusterer> compareClusteringAlgorithms(Instances data) throws Exception {
        System.out.println("\n COMPARAISON DES ALGORITHMES DE CLUSTERING");
        System.out.println("=".repeat(60));
        
        Map<String, Clusterer> results = new HashMap<>();
        
        try {
            // Créer une copie des données sans l'attribut de classe
            Instances dataForClustering = new Instances(data);
            if (dataForClustering.classIndex() >= 0) {
                dataForClustering.setClassIndex(-1);
            }
            
            // 1. K-Means avec k optimal
            System.out.println("\n 1. K-MEANS :");
            Map<Integer, Double> elbowResults = findOptimalK(dataForClustering, 8);
            int optimalK = findElbowPoint(elbowResults);
            Clusterer kmeans = applyKMeans(data, optimalK);
            results.put("KMeans", kmeans);
            
            // 2. EM
            System.out.println("\n 2. EM :");
            Clusterer em = applyEM(data, optimalK);
            results.put("EM", em);
            
            // 3. Hierarchical
            System.out.println("\n 3. HIERARCHICAL :");
            Clusterer hierarchical = applyHierarchicalClustering(data, optimalK);
            results.put("Hierarchical", hierarchical);
            
            System.out.println("\n Comparaison terminee");
            
        } catch (Exception e) {
            System.out.println("  Erreur lors de la comparaison : " + e.getMessage());
        }
        
        return results;
    }
}