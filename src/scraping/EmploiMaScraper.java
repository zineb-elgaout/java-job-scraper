package scraping;

import model.JobOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class EmploiMaScraper {

    private static final String BASE_URL = "https://www.emploi.ma";

    public List<JobOffer> scrape() {

        List<JobOffer> offers = new ArrayList<>();
        Set<String> existingLinks = new HashSet<>();

        int MAX_PAGES = 500; // Réduit à 5 pages pour éviter les problèmes

        try {
            for (int page = 0; page < MAX_PAGES; page++) {
                
                // Vérifier interruption
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Scraping Emploi.ma interrompu");
                    return offers;
                }

                String url = BASE_URL + "/recherche-jobs-maroc?page=" + page;
                System.out.println("Scraping page Emploi.ma: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000) // Réduit à 10 secondes
                        .get();

                Elements jobs = doc.select("div.card.card-job");

                if (jobs.isEmpty()) {
                    System.out.println("Aucune offre trouvée, arrêt pagination.");
                    break;
                }

                for (Element job : jobs) {

                    // Titre + lien
                    Element titleEl = job.selectFirst("h3 a");
                    if (titleEl == null) continue;

                    String fullTitle = titleEl.text().trim();
                    String link = BASE_URL + titleEl.attr("href");

                    // Éviter doublons
                    if (existingLinks.contains(link)) continue;
                    existingLinks.add(link);

                    // Titre + lieu
                    String title = fullTitle;
                    String location = "Non spécifié";

                    if (fullTitle.contains(" - ")) {
                        int lastDash = fullTitle.lastIndexOf(" - ");
                        title = fullTitle.substring(0, lastDash).trim();
                        location = fullTitle.substring(lastDash + 3).trim();
                    }

                    // Entreprise
                    String company = "Non spécifié";
                    Element companyEl = job.selectFirst(".company-name");
                    if (companyEl != null) {
                        company = companyEl.text().trim();
                    }

                    // Description
                    String description = "";
                    Element descEl = job.selectFirst(".card-job-description p");
                    if (descEl != null) {
                        description = descEl.text().trim();
                    }

                    // Création objet métier
                    JobOffer offer = new JobOffer(
                            title,
                            company,
                            location,
                            description,
                            link,
                            "Emploi.ma"
                    );

                    offers.add(offer);
                }
                
                // Petite pause entre les pages
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Scraping Emploi.ma interrompu");
                    Thread.currentThread().interrupt();
                    return offers;
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur scraping Emploi.ma: " + e.getMessage());
        }

        return offers;
    }
}