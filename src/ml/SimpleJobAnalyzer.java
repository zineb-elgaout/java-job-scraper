package ml;

import java.io.*;
import java.util.*;

public class SimpleJobAnalyzer {
    
    public static void main(String[] args) throws IOException {
        String csvPath = "ml_dataset.csv";
        
        System.out.println("=== ANALYSE SIMPLIFIÉE DES OFFRES D'EMPLOI ===\n");
        
        // 1. Lire et analyser le CSV
        System.out.println("1. Analyse du fichier CSV...");
        List<JobOffer> offers = readCSV(csvPath);
        
        if (offers.isEmpty()) {
            System.out.println("ERREUR: Aucune offre trouvée");
            return;
        }
        
        System.out.println("Offres chargées: " + offers.size());
        
        // 2. Analyse par localisation
        System.out.println("\n2. Distribution par ville:");
        Map<String, Integer> locationCount = new HashMap<>();
        for (JobOffer offer : offers) {
            String loc = offer.location.isEmpty() ? "Non spécifié" : offer.location;
            locationCount.put(loc, locationCount.getOrDefault(loc, 0) + 1);
        }
        
        locationCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .forEach(entry -> 
                System.out.printf("  %-15s: %d offres (%.1f%%)\n", 
                    entry.getKey(), entry.getValue(), 
                    (entry.getValue() * 100.0) / offers.size()));
        
        // 3. Extraction des mots-clés
        System.out.println("\n3. Mots-clés fréquents:");
        Map<String, Integer> wordFreq = extractKeywords(offers);
        
        wordFreq.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(20)
            .forEach(entry -> 
                System.out.printf("  %-20s: %d fois\n", entry.getKey(), entry.getValue()));
        
        // 4. Catégorisation simple basée sur les mots-clés
        System.out.println("\n4. Catégorisation automatique:");
        categorizeOffers(offers);
        
        // 5. Sauvegarde des données analysées
        saveAnalyzedData(offers, "offres_analysees.csv");
    }
    
    private static List<JobOffer> readCSV(String csvPath) throws IOException {
        List<JobOffer> offers = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(csvPath));
        String line = reader.readLine(); // En-têtes
        
        if (line == null) {
            reader.close();
            return offers;
        }
        
        String[] headers = line.split(",");
        int titleIdx = -1, descIdx = -1, locIdx = -1;
        
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].replace("\"", "").trim();
            switch (header) {
                case "title": titleIdx = i; break;
                case "description": descIdx = i; break;
                case "location": locIdx = i; break;
            }
        }
        
        int count = 0;
        while ((line = reader.readLine()) != null && count < 1000) { // Limite pour tests
            count++;
            String[] values = parseCSVLineSimple(line);
            
            JobOffer offer = new JobOffer();
            if (titleIdx != -1 && titleIdx < values.length) {
                offer.title = values[titleIdx].replace("\"", "").trim();
            }
            if (descIdx != -1 && descIdx < values.length) {
                offer.description = values[descIdx].replace("\"", "").trim();
            }
            if (locIdx != -1 && locIdx < values.length) {
                offer.location = values[locIdx].replace("\"", "").trim();
            }
            
            if (!offer.title.isEmpty() || !offer.description.isEmpty()) {
                offers.add(offer);
            }
        }
        
        reader.close();
        return offers;
    }
    
    private static String[] parseCSVLineSimple(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }
    
    private static Map<String, Integer> extractKeywords(List<JobOffer> offers) {
        Map<String, Integer> freq = new HashMap<>();
        String[] stopWords = {"de", "et", "à", "en", "pour", "avec", "dans", "sur", "par", "le", "la", "les", "des", "un", "une", "du"};
        Set<String> stopSet = new HashSet<>(Arrays.asList(stopWords));
        
        for (JobOffer offer : offers) {
            String text = (offer.title + " " + offer.description).toLowerCase();
            String[] words = text.split("[\\s\\p{Punct}]+");
            
            for (String word : words) {
                word = word.trim();
                if (word.length() > 2 && !stopSet.contains(word) && !word.matches(".*\\d.*")) {
                    freq.put(word, freq.getOrDefault(word, 0) + 1);
                }
            }
        }
        
        return freq;
    }
    
    private static void categorizeOffers(List<JobOffer> offers) {
        // Définir des catégories basées sur les mots-clés
        Map<String, String[]> categoryKeywords = new HashMap<>();
        categoryKeywords.put("IT/Développement", 
            new String[]{"développeur", "programmeur", "java", "python", "web", "mobile", "software", "backend", "frontend"});
        categoryKeywords.put("Commercial/Vente", 
            new String[]{"commercial", "vente", "client", "chiffre", "affaires", "account", "sales"});
        categoryKeywords.put("Administratif", 
            new String[]{"administratif", "secrétariat", "gestion", "bureau", "assistant", "accueil"});
        categoryKeywords.put("Comptabilité/Finance", 
            new String[]{"comptable", "finance", "comptabilité", "trésorerie", "audit"});
        categoryKeywords.put("Logistique/Transport", 
            new String[]{"logistique", "transport", "livraison", "stock", "entrepôt"});
        
        Map<String, Integer> categoryCount = new HashMap<>();
        
        for (JobOffer offer : offers) {
            String text = (offer.title + " " + offer.description).toLowerCase();
            String category = "Autre";
            int maxMatches = 0;
            
            for (Map.Entry<String, String[]> entry : categoryKeywords.entrySet()) {
                int matches = 0;
                for (String keyword : entry.getValue()) {
                    if (text.contains(keyword)) {
                        matches++;
                    }
                }
                if (matches > maxMatches) {
                    maxMatches = matches;
                    category = entry.getKey();
                }
            }
            
            offer.category = category;
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }
        
        // Afficher les résultats
        categoryCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .forEach(entry -> 
                System.out.printf("  %-25s: %d offres\n", entry.getKey(), entry.getValue()));
    }
    
    private static void saveAnalyzedData(List<JobOffer> offers, String filename) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(filename));
        writer.println("Titre,Description,Localisation,Catégorie");
        
        for (JobOffer offer : offers) {
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                offer.title.replace("\"", "'"),
                offer.description.replace("\"", "'").replace("\n", " ").substring(0, Math.min(200, offer.description.length())),
                offer.location,
                offer.category);
        }
        
        writer.close();
        System.out.println("\nDonnées analysées sauvegardées: " + filename);
    }
    
    static class JobOffer {
        String title = "";
        String description = "";
        String location = "";
        String category = "Non catégorisé";
    }
}