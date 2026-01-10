package ml;

import weka.clusterers.SimpleKMeans;
import weka.core.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

/**
 * ═══════════════════════════════════════════════════════════════════
 * MACHINE LEARNING AMÉLIORÉ : Clustering basé sur le TEXTE
 * Utilise TF-IDF sur title + description pour un clustering pertinent
 * ═══════════════════════════════════════════════════════════════════
 */
public class ImprovedJobClustering {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3307/job_scraper";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    
    // Stop words français + mots d'interface web
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        // Articles, prépositions
        "le", "la", "les", "un", "une", "des", "du", "de", "et", "ou",
        "mais", "donc", "pour", "dans", "sur", "avec", "sans", "sous",
        "ce", "cette", "ces", "son", "sa", "ses", "leur", "nous", "vous",
        "qui", "que", "quoi", "dont", "où", "tout", "tous", "très",
        "plus", "moins", "beaucoup", "peu", "encore", "toujours", "jamais",
        "aux", "par", "chez", "vers", "entre", "depuis", "pendant",
        
        // Verbes courants
        "être", "avoir", "faire", "dire", "aller", "voir", "savoir",
        "pouvoir", "falloir", "vouloir", "devoir", "est", "sont", "sera",
        "être", "assurer", "garantir", "permettre", "donner",
        
        // Mots génériques RH
        "recherche", "offre", "poste", "profil", "mission", "missions",
        "recherchons", "candidat", "entreprise", "société", "principales",
        "recrutons", "recrute", "sommes", "votre", "notre", "nos",
        
        // MOTS D'INTERFACE WEB (le vrai problème)
        "contact", "favoris", "faq", "mes", "inscription", "connexion",
        "mon", "compte", "espace", "déposer", "postuler", "enregistrer",
        "accueil", "rechercher", "filtrer", "trier", "page", "suivant",
        "précédent", "retour", "menu", "navigation", "liens", "partenaires",
        
        // Mots trop génériques
        "cadre", "secteur", "domaine", "activités", "service", "services",
        "équipe", "client", "clients", "maroc", "casablanca", "rabat"
    ));
    
    private List<JobData> jobs;
    private Map<String, Integer> vocabulary;
    private double[][] tfidfMatrix;
    private Instances wekaData;
    private int[] clusterAssignments;
    
    
    public static void main(String[] args) {
        try {
            ImprovedJobClustering clustering = new ImprovedJobClustering();
            
            // Pipeline
            clustering.loadJobs();
            clustering.buildVocabulary();
            clustering.computeTfIdf();
            clustering.convertToWeka();
            clustering.elbowMethod(2, 12);
            
            Scanner scanner = new Scanner(System.in);
            System.out.print("\n🎯 Choisissez K (nombre de clusters) [6] : ");
            String input = scanner.nextLine();
            int k = input.isEmpty() ? 6 : Integer.parseInt(input);
            
            clustering.performClustering(k);
            clustering.analyzeAndLabelClusters();
            clustering.saveToDatabase();
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * ÉTAPE 1 : Charger les offres avec leur texte
     * ═══════════════════════════════════════════════════════════════
     */
    private void loadJobs() throws SQLException {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("📂 CHARGEMENT DES OFFRES");
        System.out.println("═".repeat(70));
        
        jobs = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query = """
                SELECT id, title, description, company, location
                FROM ml_ready_data
                WHERE title IS NOT NULL AND description IS NOT NULL
                LIMIT 1000;
                """;
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                while (rs.next()) {
                    JobData job = new JobData();
                    job.id = rs.getInt("id");
                    job.title = rs.getString("title");
                    job.description = rs.getString("description");
                    job.company = rs.getString("company");
                    job.location = rs.getString("location");
                    
                    // Combiner title + description
                    job.fullText = cleanText(job.title + " " + job.description);
                    job.tokens = tokenize(job.fullText);
                    
                    jobs.add(job);
                }
            }
        }
        
        System.out.println("✅ " + jobs.size() + " offres chargées");
    }
    
    
    /**
     * Nettoie le texte de manière agressive
     */
    private String cleanText(String text) {
        if (text == null) return "";
        
        // Minuscules
        text = text.toLowerCase();
        
        // Supprimer HTML et scripts
        text = text.replaceAll("<script[^>]*>.*?</script>", " ");
        text = text.replaceAll("<style[^>]*>.*?</style>", " ");
        text = text.replaceAll("<[^>]+>", " ");
        text = text.replaceAll("&[a-z]+;", " ");
        
        // Supprimer URLs
        text = text.replaceAll("https?://[^\\s]+", " ");
        text = text.replaceAll("www\\.[^\\s]+", " ");
        
        // Supprimer emails
        text = text.replaceAll("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}", " ");
        
        // Supprimer caractères spéciaux (garder lettres accentuées)
        text = text.replaceAll("[^a-zàâäéèêëïîôùûüÿç0-9\\s]", " ");
        
        // Supprimer nombres isolés
        text = text.replaceAll("\\b\\d+\\b", " ");
        
        // Espaces multiples
        text = text.replaceAll("\\s+", " ").trim();
        
        return text;
    }
    
    
    /**
     * Tokenise et filtre les stop words
     */
    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        
        for (String word : text.split("\\s+")) {
            // Garder mots >= 3 caractères
            // Pas de stop words
            // Pas de mots purement numériques
            if (word.length() >= 3 && 
                !STOP_WORDS.contains(word) &&
                !word.matches("\\d+")) {
                tokens.add(word);
            }
        }
        
        return tokens;
    }
    
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * ÉTAPE 2 : Construire le vocabulaire (top mots)
     * ═══════════════════════════════════════════════════════════════
     */
    private void buildVocabulary() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("📚 CONSTRUCTION DU VOCABULAIRE");
        System.out.println("═".repeat(70));
        
        // Compter les mots dans tous les documents
        Map<String, Integer> wordCounts = new HashMap<>();
        
        for (JobData job : jobs) {
            Set<String> uniqueWords = new HashSet<>(job.tokens);
            for (String word : uniqueWords) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
        
        // Filtrer : au moins 2 docs, max 80% des docs
        int minDf = 2;
        int maxDf = (int) (jobs.size() * 0.8);
        
        List<Map.Entry<String, Integer>> filteredWords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            int df = entry.getValue();
            if (df >= minDf && df <= maxDf) {
                filteredWords.add(entry);
            }
        }
        
        // Garder top 200 mots
        filteredWords.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        vocabulary = new LinkedHashMap<>();
        int idx = 0;
        for (int i = 0; i < Math.min(200, filteredWords.size()); i++) {
            vocabulary.put(filteredWords.get(i).getKey(), idx++);
        }
        
        System.out.println("✅ Vocabulaire : " + vocabulary.size() + " mots");
        
        // Afficher top 20 mots
        System.out.println("\n🔝 Top 20 mots les plus fréquents :");
        int count = 0;
        for (Map.Entry<String, Integer> entry : filteredWords) {
            if (count >= 20) break;
            if (vocabulary.containsKey(entry.getKey())) {
                System.out.printf("   %2d. %-20s (%d docs)\n", 
                    count + 1, entry.getKey(), entry.getValue());
                count++;
            }
        }
    }
    
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * ÉTAPE 3 : Calculer TF-IDF
     * ═══════════════════════════════════════════════════════════════
     */
    private void computeTfIdf() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("🔢 CALCUL TF-IDF");
        System.out.println("═".repeat(70));
        
        int numDocs = jobs.size();
        int vocabSize = vocabulary.size();
        
        tfidfMatrix = new double[numDocs][vocabSize];
        
        // Document Frequency pour IDF
        Map<String, Integer> df = new HashMap<>();
        for (JobData job : jobs) {
            Set<String> uniqueWords = new HashSet<>(job.tokens);
            for (String word : uniqueWords) {
                if (vocabulary.containsKey(word)) {
                    df.put(word, df.getOrDefault(word, 0) + 1);
                }
            }
        }
        
        // Calculer TF-IDF pour chaque document
        for (int i = 0; i < numDocs; i++) {
            JobData job = jobs.get(i);
            
            // Term Frequency
            Map<String, Integer> tf = new HashMap<>();
            for (String token : job.tokens) {
                if (vocabulary.containsKey(token)) {
                    tf.put(token, tf.getOrDefault(token, 0) + 1);
                }
            }
            
            // TF-IDF
            for (Map.Entry<String, Integer> entry : tf.entrySet()) {
                String word = entry.getKey();
                int termFreq = entry.getValue();
                int docFreq = df.get(word);
                
                int wordIdx = vocabulary.get(word);
                
                // TF-IDF = TF * log(N / DF)
                double tfidf = termFreq * Math.log((double) numDocs / docFreq);
                tfidfMatrix[i][wordIdx] = tfidf;
            }
            
            // Normalisation L2
            double norm = 0.0;
            for (double val : tfidfMatrix[i]) {
                norm += val * val;
            }
            norm = Math.sqrt(norm);
            
            if (norm > 0) {
                for (int j = 0; j < vocabSize; j++) {
                    tfidfMatrix[i][j] /= norm;
                }
            }
        }
        
        System.out.println("✅ Matrice TF-IDF : " + numDocs + " × " + vocabSize);
    }
    
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * ÉTAPE 4 : Convertir en format Weka
     * ═══════════════════════════════════════════════════════════════
     */
    private void convertToWeka() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("🔄 CONVERSION EN FORMAT WEKA");
        System.out.println("═".repeat(70));
        
        // Créer attributs
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (String word : vocabulary.keySet()) {
            attributes.add(new Attribute(word));
        }
        
        wekaData = new Instances("JobVectors", attributes, jobs.size());
        
        // Ajouter instances
        for (int i = 0; i < jobs.size(); i++) {
            wekaData.add(new DenseInstance(1.0, tfidfMatrix[i]));
        }
        
        System.out.println("✅ " + wekaData.numInstances() + " instances créées");
    }
    
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * ÉTAPE 5 : Méthode du coude
     * ═══════════════════════════════════════════════════════════════
     */
    private void elbowMethod(int minK, int maxK) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("📈 MÉTHODE DU COUDE");
        System.out.println("═".repeat(70));
        System.out.println("\nTest de K = " + minK + " à " + maxK + "...\n");
        
        for (int k = minK; k <= maxK; k++) {
            try {
                SimpleKMeans kmeans = new SimpleKMeans();
                kmeans.setNumClusters(k);
                kmeans.setSeed(42);
                kmeans.setMaxIterations(100);
                kmeans.buildClusterer(wekaData);
                
                double sse = kmeans.getSquaredError();
                
                System.out.printf("   K = %2d : SSE = %10.2f", k, sse);
                
                int barLength = Math.max(0, Math.min(40, (int)(40 - (sse / 20))));
                if (barLength > 0) {
                    System.out.print("  [" + "█".repeat(barLength) + "]");
                }
                System.out.println();
                
            } catch (Exception e) {
                System.err.println("   K = " + k + " : Erreur");
            }
        }
        
        System.out.println("\n💡 Le coude = K optimal (où la courbe ralentit)");
    }
    
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * ÉTAPE 6 : K-Means Clustering
     * ═══════════════════════════════════════════════════════════════
     */
    private void performClustering(int k) throws Exception {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("🤖 K-MEANS CLUSTERING (K = " + k + ")");
        System.out.println("═".repeat(70));
        
        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setNumClusters(k);
        kmeans.setSeed(42);
        kmeans.setMaxIterations(300);
        
        System.out.println("\n⚙️  Entraînement du modèle...");
        kmeans.buildClusterer(wekaData);
        
        // Assigner clusters
        clusterAssignments = new int[jobs.size()];
        for (int i = 0; i < jobs.size(); i++) {
            clusterAssignments[i] = kmeans.clusterInstance(wekaData.instance(i));
            jobs.get(i).clusterId = clusterAssignments[i];
        }
        
        System.out.println("✅ Clustering terminé");
        
        // Distribution
        System.out.println("\n📊 Distribution des clusters :");
        Map<Integer, Integer> distribution = new HashMap<>();
        for (int cluster : clusterAssignments) {
            distribution.put(cluster, distribution.getOrDefault(cluster, 0) + 1);
        }
        
        for (int i = 0; i < k; i++) {
            int count = distribution.getOrDefault(i, 0);
            double percentage = (count * 100.0) / jobs.size();
            String bar = "█".repeat((int) (percentage / 2));
            System.out.printf("   Cluster %d : %4d offres (%5.1f%%) %s\n", 
                i, count, percentage, bar);
        }
    }
    
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * ÉTAPE 7 : Analyser et labelliser les clusters
     * ═══════════════════════════════════════════════════════════════
     */
    private void analyzeAndLabelClusters() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("🔍 ANALYSE DES CLUSTERS");
        System.out.println("═".repeat(70));
        
        int numClusters = Collections.max(Arrays.asList(
            Arrays.stream(clusterAssignments).boxed().toArray(Integer[]::new)
        )) + 1;
        
        for (int clusterId = 0; clusterId < numClusters; clusterId++) {
            System.out.println("\n" + "═".repeat(70));
            System.out.println("CLUSTER " + clusterId);
            System.out.println("═".repeat(70));
            
            // Filtrer jobs du cluster
            List<JobData> clusterJobs = new ArrayList<>();
            for (JobData job : jobs) {
                if (job.clusterId == clusterId) {
                    clusterJobs.add(job);
                }
            }
            
            System.out.println("\n📊 Nombre d'offres : " + clusterJobs.size());
            
            // Mots les plus fréquents du cluster
            Map<String, Integer> wordFreq = new HashMap<>();
            for (JobData job : clusterJobs) {
                for (String token : job.tokens) {
                    if (vocabulary.containsKey(token)) {
                        wordFreq.put(token, wordFreq.getOrDefault(token, 0) + 1);
                    }
                }
            }
            
            // Top mots
            List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordFreq.entrySet());
            sortedWords.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            System.out.println("\n🔝 Top 15 mots caractéristiques :");
            for (int i = 0; i < Math.min(15, sortedWords.size()); i++) {
                Map.Entry<String, Integer> entry = sortedWords.get(i);
                double percentage = (entry.getValue() * 100.0) / clusterJobs.size();
                System.out.printf("   %2d. %-20s : %3d offres (%.1f%%)\n",
                    i + 1, entry.getKey(), entry.getValue(), percentage);
            }
            
            // Exemples d'offres
            System.out.println("\n📋 Exemples d'offres :");
            for (int i = 0; i < Math.min(5, clusterJobs.size()); i++) {
                JobData job = clusterJobs.get(i);
                System.out.printf("   %4d. %-50s - %s\n",
                    job.id,
                    truncate(job.title, 50),
                    truncate(job.company, 25));
            }
            
            // Suggérer un label basé sur les mots dominants
            String suggestedLabel = suggestLabel(sortedWords);
            System.out.println("\n💡 Label suggéré : " + suggestedLabel);
        }
        
        // Labellisation manuelle
        labelClustersManually(numClusters);
    }
    
    
    /**
     * Suggère un label basé sur les mots dominants
     */
    private String suggestLabel(List<Map.Entry<String, Integer>> topWords) {
        if (topWords.isEmpty()) return "Inconnu";
        
        Set<String> words = new HashSet<>();
        for (int i = 0; i < Math.min(10, topWords.size()); i++) {
            words.add(topWords.get(i).getKey());
        }
        
        // Patterns de détection
        if (containsAny(words, "developpeur", "java", "python", "web", "programmeur", 
                        "software", "angular", "react", "php")) {
            return "IT - Développement";
        }
        if (containsAny(words, "comptable", "finance", "audit", "comptabilite", 
                        "tresorerie", "fiscal")) {
            return "Finance / Comptabilité";
        }
        if (containsAny(words, "commercial", "vente", "vendeur", "business", 
                        "client", "prospection")) {
            return "Commercial / Vente";
        }
        if (containsAny(words, "marketing", "communication", "digital", "community", 
                        "content", "media")) {
            return "Marketing / Communication";
        }
        if (containsAny(words, "ressources", "humaines", "recrutement", "formation", 
                        "paie", "talent")) {
            return "Ressources Humaines";
        }
        if (containsAny(words, "logistique", "supply", "chain", "stock", 
                        "transport", "approvisionnement")) {
            return "Logistique / Supply Chain";
        }
        if (containsAny(words, "ingenieur", "production", "qualite", "maintenance", 
                        "industriel", "technique")) {
            return "Ingénierie / Production";
        }
        
        return "Autre - " + topWords.get(0).getKey();
    }
    
    
    private boolean containsAny(Set<String> words, String... keywords) {
        for (String keyword : keywords) {
            if (words.contains(keyword)) return true;
        }
        return false;
    }
    
    
    /**
     * Labellisation manuelle
     */
    private void labelClustersManually(int numClusters) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("🏷️  LABELLISATION FINALE");
        System.out.println("═".repeat(70));
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n💡 Pour chaque cluster, validez ou modifiez le label");
        System.out.println("   (Appuyez sur Entrée pour garder le label suggéré)\n");
        
        for (int i = 0; i < numClusters; i++) {
            final int clusterId = i; // Variable effectively final pour lambda
            
            // Récupérer le label suggéré
            String suggested = jobs.stream()
                .filter(j -> j.clusterId == clusterId)
                .findFirst()
                .map(j -> j.category)
                .orElse("Cluster " + clusterId);
            
            System.out.print("➜ Cluster " + clusterId + " [" + suggested + "] : ");
            String input = scanner.nextLine().trim();
            
            String finalLabel = input.isEmpty() ? suggested : input;
            
            // Assigner à tous les jobs du cluster
            for (JobData job : jobs) {
                if (job.clusterId == clusterId) {
                    job.category = finalLabel;
                }
            }
            
            System.out.println("   ✅ '" + finalLabel + "'");
        }
    }
    
    
    /**
     * ═══════════════════════════════════════════════════════════════
     * ÉTAPE 8 : Sauvegarder en base de données
     * ═══════════════════════════════════════════════════════════════
     */
    private void saveToDatabase() throws SQLException {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("💾 SAUVEGARDE DANS LA BASE DE DONNÉES");
        System.out.println("═".repeat(70));
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            
            // Ajouter colonnes si nécessaire
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.execute("ALTER TABLE ml_ready_data ADD COLUMN cluster_id INT");
                    System.out.println("✅ Colonne 'cluster_id' ajoutée");
                } catch (SQLException e) {}
                
                try {
                    stmt.execute("ALTER TABLE ml_ready_data ADD COLUMN category VARCHAR(100)");
                    System.out.println("✅ Colonne 'category' ajoutée");
                } catch (SQLException e) {}
            }
            
            // Mettre à jour
            String updateQuery = """
                UPDATE ml_ready_data 
                SET cluster_id = ?, category = ? 
                WHERE id = ?
                """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                int updated = 0;
                
                for (JobData job : jobs) {
                    pstmt.setInt(1, job.clusterId);
                    pstmt.setString(2, job.category);
                    pstmt.setInt(3, job.id);
                    pstmt.executeUpdate();
                    updated++;
                }
                
                System.out.println("\n✅ " + updated + " offres mises à jour");
            }
            
            // Statistiques
            System.out.println("\n📊 Statistiques finales :");
            String statsQuery = """
                SELECT category, COUNT(*) as count
                FROM ml_ready_data
                WHERE category IS NOT NULL
                GROUP BY category
                ORDER BY count DESC;
                """;
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(statsQuery)) {
                
                while (rs.next()) {
                    String category = rs.getString("category");
                    int count = rs.getInt("count");
                    double percentage = (count * 100.0) / jobs.size();
                    System.out.printf("   • %-35s : %4d (%5.1f%%)\n",
                        category, count, percentage);
                }
            }
        }
        
        System.out.println("\n✅ CLUSTERING TERMINÉ AVEC SUCCÈS !");
    }
    
    
    /**
     * Utilitaires
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    
    
    /**
     * Classe pour stocker les données d'une offre
     */
    private static class JobData {
        int id;
        String title;
        String description;
        String company;
        String location;
        String fullText;
        List<String> tokens;
        int clusterId;
        String category;
    }
}