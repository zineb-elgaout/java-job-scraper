package scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import model.JobOffer;
import java.util.ArrayList;
import java.util.List;

public class RekruteScraper {
    
    /**
     * Scrape une seule page de Rekrute.com
     */
    public List<JobOffer> scrapeOnePage() {
        List<JobOffer> jobs = new ArrayList<>();
        
        try {
            String url = "https://www.rekrute.com/offres.html?s=1&p=1&o=1";
            
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            Elements jobElements = doc.select("li.post-id");
            
            for (Element job : jobElements) {
                try {
                    // Titre
                    String titre = job.select("h2 a.titreJob").text().trim();
                    if (titre.isEmpty()) continue;
                    
                    // Entreprise
                    String entreprise = "";
                    Elements img = job.select("img.photo");
                    if (!img.isEmpty()) {
                        entreprise = img.first().attr("alt").trim();
                    }
                    if (entreprise.isEmpty()) entreprise = "Non spécifié";
                    
                    // Lieu
                    String lieu = "Maroc";
                    if (titre.contains("|")) {
                        String[] parts = titre.split("\\|");
                        if (parts.length > 1) {
                            lieu = parts[1].trim();
                            titre = parts[0].trim();
                        }
                    }
                    
                    // Lien
                    String lien = job.select("h2 a.titreJob").attr("abs:href");
                    if (lien.isEmpty()) lien = url;
                    
                    // Description
                    StringBuilder description = new StringBuilder();
                    
                    // Catégorie
                    String category = extractCategorySafe(job);
                    if (category != null) {
                        description.append("Catégorie: ").append(category).append("\n");
                    }
                    
                    // Type de contrat
                    String contractType = extractContractTypeSafe(job);
                    if (contractType != null) {
                        description.append("Type de contrat: ").append(contractType).append("\n");
                    }
                    
                    // Description courte
                    Elements descElements = job.select("div.info span");
                    if (!descElements.isEmpty()) {
                        String shortDesc = descElements.text().trim();
                        if (!shortDesc.isEmpty()) {
                            description.append(shortDesc);
                        }
                    }
                    
                    // Créer l'offre
                    JobOffer offer = new JobOffer(titre, entreprise, lieu, description.toString(), lien, "Rekrute");
                    jobs.add(offer);
                    
                } catch (Exception e) {
                    // Continuer avec l'offre suivante
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur Rekrute: " + e.getMessage());
        }
        
        return jobs;
    }
    
    /**
     * Méthode principale compatible avec l'interface
     */
    public List<JobOffer> scrapeAll(String keyword, String location) {
        System.out.println("Début du scraping Rekrute...");
        
        List<JobOffer> allJobs = new ArrayList<>();
        
        try {
            // Scraper seulement 3 pages pour éviter les problèmes
            for (int page = 1; page <= 500; page++) {
                System.out.println("Page " + page + "...");
                
                try {
                    String url = "https://www.rekrute.com/offres.html?s=1&p=" + page + "&o=1";
                    
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(10000)
                            .get();
                    
                    Elements jobElements = doc.select("li.post-id");
                    
                    for (Element job : jobElements) {
                        try {
                            // Titre
                            String titre = job.select("h2 a.titreJob").text().trim();
                            if (titre.isEmpty()) continue;
                            
                            // Entreprise
                            String entreprise = "";
                            Elements img = job.select("img.photo");
                            if (!img.isEmpty()) {
                                entreprise = img.first().attr("alt").trim();
                            }
                            if (entreprise.isEmpty()) entreprise = "Non spécifié";
                            
                            // Lieu
                            String lieu = "Maroc";
                            if (titre.contains("|")) {
                                String[] parts = titre.split("\\|");
                                if (parts.length > 1) {
                                    lieu = parts[1].trim();
                                    titre = parts[0].trim();
                                }
                            }
                            
                            // Lien
                            String lien = job.select("h2 a.titreJob").attr("abs:href");
                            if (lien.isEmpty()) lien = url;
                            
                            // Description
                            StringBuilder description = new StringBuilder();
                            
                            // Catégorie
                            String category = extractCategorySafe(job);
                            if (category != null) {
                                description.append("Catégorie: ").append(category).append("\n");
                            }
                            
                            // Description courte
                            Elements descElements = job.select("div.info span");
                            if (!descElements.isEmpty()) {
                                String shortDesc = descElements.text().trim();
                                if (!shortDesc.isEmpty()) {
                                    description.append(shortDesc);
                                }
                            }
                            
                            // Créer l'offre
                            JobOffer offer = new JobOffer(titre, entreprise, lieu, description.toString(), lien, "Rekrute");
                            allJobs.add(offer);
                            
                        } catch (Exception e) {
                            // Continuer
                        }
                    }
                    
                    // Petite pause entre les pages
                    Thread.sleep(1000);
                    
                } catch (InterruptedException e) {
                    System.out.println("Scraping interrompu");
                    break;
                } catch (Exception e) {
                    System.err.println("Erreur page " + page + ": " + e.getMessage());
                }
            }
            
            System.out.println("Scraping Rekrute terminé. " + allJobs.size() + " offres récupérées.");
            
        } catch (Exception e) {
            System.err.println("Erreur générale Rekrute: " + e.getMessage());
        }
        
        return allJobs;
    }
    
    // Méthodes d'extraction sécurisées
    private static String extractCategorySafe(Element job) {
        try {
            Elements allLi = job.select("li");
            for (Element li : allLi) {
                String text = li.text();
                if (text.contains("Secteur d'activité") || text.contains("Fonction")) {
                    Elements links = li.select("a");
                    if (!links.isEmpty()) {
                        return links.first().text().trim();
                    }
                }
            }
        } catch (Exception e) {
            // Ignorer
        }
        return null;
    }
    
    private static String extractContractTypeSafe(Element job) {
        try {
            Elements allLi = job.select("li");
            for (Element li : allLi) {
                String text = li.text();
                if (text.contains("Type de contrat proposé")) {
                    Elements links = li.select("a");
                    if (!links.isEmpty()) {
                        return links.first().text().trim();
                    }
                }
            }
        } catch (Exception e) {
            // Ignorer
        }
        return null;
    }
}