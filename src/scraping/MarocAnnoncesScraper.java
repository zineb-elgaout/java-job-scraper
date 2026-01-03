package scraping;

import model.JobOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class MarocAnnoncesScraper {

    private static final String BASE_URL =
            "https://www.marocannonces.com/categorie/309/Emploi/Offres-emploi.html";

    public List<JobOffer> scrape(int maxPages) {

        List<JobOffer> offers = new ArrayList<>();
        Set<String> visitedLinks = new HashSet<>();

        try {
            for (int page = 1; page <= maxPages; page++) {
                
                // Vérifier si le thread a été interrompu
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Scraping interrompu");
                    return offers;
                }

                String url = page == 1
                        ? BASE_URL
                        : BASE_URL.replace(".html", "/" + page + ".html");

                System.out.println("Scraping page MarocAnnonces: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000) // Réduit à 10 secondes
                        .get();

                // Récupérer les liens des annonces
                Elements jobLinks = doc.select("a[href*='/Offres-emploi/annonce/']");

                if (jobLinks.isEmpty()) {
                    System.out.println("Page vide → arrêt");
                    break;
                }

                for (Element linkEl : jobLinks) {
                    
                    // Vérifier à nouveau si interrompu
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("Scraping interrompu pendant le traitement");
                        return offers;
                    }

                    String link = "https://www.marocannonces.com/" + linkEl.attr("href");

                    if (visitedLinks.contains(link)) continue;
                    visitedLinks.add(link);

                    JobOffer offer = scrapeJobDetails(link);
                    if (offer != null) {
                        offers.add(offer);
                    }

                    // Gérer l'interruption pendant le sleep
                    try {
                        Thread.sleep(500); // Réduit à 0.5 seconde
                    } catch (InterruptedException e) {
                        System.out.println("Pause interrompue");
                        Thread.currentThread().interrupt();
                        return offers;
                    }
                }
                
                // Petite pause entre les pages
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Pause entre pages interrompue");
                    Thread.currentThread().interrupt();
                    return offers;
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du scraping MarocAnnonces: " + e.getMessage());
        }

        return offers;
    }

    // Scraping de la page détail
    private JobOffer scrapeJobDetails(String url) {

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000) // Réduit à 10 secondes
                    .get();

            String title = doc.select("div.description.desccatemploi h1").text();
            String description = doc.select("div.block").text();

            String location = doc.select("ul.info-holder li:first-child a").text();
            String company = doc.select("ul.extraQuestionName li:nth-child(4) a").text();

            if (title.isEmpty()) return null;

            return new JobOffer(
                    title,
                    company.isEmpty() ? "Non spécifié" : company,
                    location.isEmpty() ? "Non spécifié" : location,
                    description,
                    url,
                    "MarocAnnonces"
            );

        } catch (Exception e) {
            System.out.println("Erreur annonce MarocAnnonces: " + url);
            return null;
        }
    }
}