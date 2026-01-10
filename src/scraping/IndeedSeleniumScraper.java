package scraping;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.interactions.Actions;

import java.sql.*;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class IndeedSeleniumScraper {

    private static final String DB_URL = "jdbc:mysql://localhost:3307/job_scraper";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private Connection conn;
    private WebDriver driver;
    private Set<String> existingUrls = new HashSet<>();
    private Random random = new Random();

    public static void main(String[] args) {
        IndeedSeleniumScraper scraper = new IndeedSeleniumScraper();
        try {
            scraper.init();
            scraper.loadExistingUrls();
            scraper.scrape("developpeur", 3);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scraper.close();
        }
    }

    private void init() throws SQLException {
        System.out.println("🚀 Démarrage Selenium Indeed");

        // DB
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

        // CHROMEDRIVER
        System.setProperty("webdriver.chrome.driver", "C:\\Tools\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        
        // Configuration anti-détection
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--lang=fr-FR");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
        
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        
        // Anti-détection JS
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
    }

    private void loadExistingUrls() throws SQLException {
        String sql = "SELECT link FROM jobs WHERE source='Indeed'";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                existingUrls.add(rs.getString("link"));
            }
        }
        System.out.println("📦 Déjà en base : " + existingUrls.size());
    }

    public void scrape(String keyword,  int pages) throws InterruptedException {
        // Navigation INITIALE vers la première page
        String url = "https://ma.indeed.com/jobs?q=" + keyword.replace(" ", "+") + "&l=";
        System.out.println("🔍 Page 1 : " + url);
        driver.get(url);
        Thread.sleep(4000 + random.nextInt(2000));

        for (int page = 0; page < pages; page++) {
            try {
                System.out.println("\n📄 ========== PAGE " + (page + 1) + " ==========");
                
                // Scroll léger pour activer le chargement
                smoothScroll();
                Thread.sleep(2000);

                // Attendre et trouver les offres
                List<WebElement> jobs = findJobs();

                if (jobs == null || jobs.isEmpty()) {
                    System.out.println("❌ Aucune offre trouvée");
                    break;
                }

                System.out.println("✅ " + jobs.size() + " offres trouvées");

                // Traiter chaque offre
                int savedCount = processJobs(jobs);
                System.out.println("💾 Offres sauvegardées : " + savedCount);

                // Si ce n'est pas la dernière page, cliquer sur "Suivant"
                if (page < pages - 1) {
                    if (!goToNextPage()) {
                        System.out.println("📍 Plus de pages disponibles");
                        break;
                    }
                    
                    // Pause longue entre pages (imiter un humain)
                    System.out.println("⏳ Pause avant la page suivante...");
                    Thread.sleep(8000 + random.nextInt(5000));
                }

            } catch (Exception e) {
                System.out.println("❌ Erreur page " + (page + 1) + " : " + e.getMessage());
                break;
            }
        }
    }

    private List<WebElement> findJobs() {
        // Attente avec timeout court
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.job_seen_beacon, div.cardOutline, td.resultContent")
            ));
        } catch (TimeoutException e) {
            System.out.println("⚠️ Timeout - tentative de récupération...");
        }

        // Essayer plusieurs sélecteurs
        String[] selectors = {
            "div.job_seen_beacon",
            "div.cardOutline",
            "td.resultContent",
            "div.slider_container",
            "li[data-jk]"
        };

        for (String selector : selectors) {
            try {
                List<WebElement> jobs = driver.findElements(By.cssSelector(selector));
                if (!jobs.isEmpty()) {
                    return jobs;
                }
            } catch (Exception e) {
                // Continuer
            }
        }

        return null;
    }

    private int processJobs(List<WebElement> jobs) throws InterruptedException {
        int savedCount = 0;

        for (int i = 0; i < jobs.size(); i++) {
            try {
                WebElement job = jobs.get(i);
                
                // Scroll vers l'offre (comportement humain)
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", 
                    job
                );
                Thread.sleep(600 + random.nextInt(400));

                // Trouver le lien
                WebElement linkElement = findJobLink(job);
                if (linkElement == null) continue;

                String jobUrl = linkElement.getAttribute("href");
                if (jobUrl == null || jobUrl.isEmpty()) continue;

                // Nettoyer l'URL
                jobUrl = cleanUrl(jobUrl);

                if (existingUrls.contains(jobUrl)) {
                    System.out.println("⏩ Offre déjà en base");
                    continue;
                }

                String title = linkElement.getText().trim();
                if (title.isEmpty()) {
                    System.out.println("⚠️ Titre vide, offre ignoré");
                    continue;
                }

                String company = extractText(job, new String[]{
                    "span.companyName",
                    "span[data-testid='company-name']"
                }, "N/A");

                String locationText = extractText(job, new String[]{
                    "div.companyLocation",
                    "div[data-testid='text-location']"
                }, "N/A");

                if (saveJob(title, company, locationText, jobUrl)) {
                    existingUrls.add(jobUrl);
                    savedCount++;
                    System.out.println("✅ " + title);
                }

            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ Élément obsolète");
            } catch (Exception e) {
                System.out.println("⚠️ Erreur : " + e.getMessage());
            }
        }

        return savedCount;
    }

    private boolean goToNextPage() throws InterruptedException {
        try {
            // Scroll vers le bas où se trouve la pagination
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(1500);

            // Chercher le bouton "Suivant" avec plusieurs sélecteurs
            WebElement nextButton = null;
            String[] nextSelectors = {
                "a[data-testid='pagination-page-next']",
                "a[aria-label='Suivant']",
                "a[aria-label='Next']",
                "a.css-13p07ha",
                "nav[role='navigation'] a[aria-label*='Next']",
                "nav[role='navigation'] a[aria-label*='Suivant']"
            };

            for (String selector : nextSelectors) {
                try {
                    List<WebElement> buttons = driver.findElements(By.cssSelector(selector));
                    if (!buttons.isEmpty()) {
                        nextButton = buttons.get(0);
                        System.out.println("✓ Bouton suivant trouvé : " + selector);
                        break;
                    }
                } catch (Exception e) {
                    // Continuer
                }
            }

            // Si pas trouvé avec CSS, essayer XPath
            if (nextButton == null) {
                try {
                    nextButton = driver.findElement(By.xpath("//a[contains(text(), 'Suivant') or contains(text(), 'Next')]"));
                    System.out.println("✓ Bouton suivant trouvé via XPath");
                } catch (NoSuchElementException e) {
                    System.out.println("❌ Bouton 'Suivant' non trouvé");
                    return false;
                }
            }

            if (nextButton == null) {
                return false;
            }

            // Vérifier si le bouton est cliquable
            if (!nextButton.isDisplayed() || !nextButton.isEnabled()) {
                System.out.println("❌ Bouton non cliquable");
                return false;
            }

            // Scroll vers le bouton
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", nextButton);
            Thread.sleep(1000);

            // Simuler un mouvement de souris (plus humain)
            Actions actions = new Actions(driver);
            actions.moveToElement(nextButton).pause(Duration.ofMillis(500)).click().perform();
            
            System.out.println("🔄 Navigation vers la page suivante...");
            Thread.sleep(3000);

            return true;

        } catch (Exception e) {
            System.out.println("❌ Erreur navigation : " + e.getMessage());
            return false;
        }
    }

    private void smoothScroll() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        // Scroll progressif en plusieurs étapes
        for (int i = 0; i < 4; i++) {
            js.executeScript("window.scrollBy({top: 350, behavior: 'smooth'})");
            Thread.sleep(600 + random.nextInt(400));
        }
        
        // Retour en haut
        js.executeScript("window.scrollTo({top: 0, behavior: 'smooth'})");
        Thread.sleep(800);
    }

    private WebElement findJobLink(WebElement job) {
        String[] linkSelectors = {
            "a.jcs-JobTitle",
            "h2.jobTitle a",
            "a[data-jk]",
            "h2 a",
            "a"
        };

        for (String selector : linkSelectors) {
            try {
                List<WebElement> links = job.findElements(By.cssSelector(selector));
                for (WebElement link : links) {
                    String href = link.getAttribute("href");
                    if (href != null && href.contains("indeed.com")) {
                        return link;
                    }
                }
            } catch (Exception e) {
                // Continuer
            }
        }
        return null;
    }

    private String extractText(WebElement parent, String[] selectors, String defaultValue) {
        for (String selector : selectors) {
            try {
                String text = parent.findElement(By.cssSelector(selector)).getText().trim();
                if (!text.isEmpty()) return text;
            } catch (Exception e) {
                // Continuer
            }
        }
        return defaultValue;
    }

    private String cleanUrl(String url) {
        if (url == null) return null;
        if (url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        }
        return url;
    }

    private boolean saveJob(String title, String company, String location, String link) {
        String sql = """
                INSERT INTO jobs (title, company, location, link, source)
                VALUES (?, ?, ?, ?, 'Indeed')
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, company);
            ps.setString(3, location);
            ps.setString(4, link);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("⚠️ Erreur DB : " + e.getMessage());
            return false;
        }
    }

    private void close() {
        try {
            if (driver != null) driver.quit();
            if (conn != null) conn.close();
            System.out.println("🛑 Fermeture propre");
        } catch (Exception ignored) {}
    }
}