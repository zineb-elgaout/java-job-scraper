package dao;

import model.JobOffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;

public class JobDAO {

    /**
     * ═══════════════════════════════════════════════════════════
     * SAUVEGARDER UNE OFFRE (VERSION SIMPLE)
     * ═══════════════════════════════════════════════════════════
     */
    public static boolean saveJob(JobOffer job) {
        String sql = "INSERT INTO jobs (title, company, location, description, link, source) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, job.getTitle());
            pstmt.setString(2, job.getCompany());
            pstmt.setString(3, job.getLocation());
            pstmt.setString(4, job.getDescription());
            pstmt.setString(5, job.getLink());
            pstmt.setString(6, job.getSource());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde: " + e.getMessage());
            return false;
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════
     * SAUVEGARDER PLUSIEURS OFFRES (AVEC DÉTECTION DOUBLONS)
     * ═══════════════════════════════════════════════════════════
     */
    public static int saveJobs(List<JobOffer> jobs) {
        int count = 0;
        int duplicates = 0;

        for (JobOffer job : jobs) {
            // Vérifier si l'offre existe déjà
            if (!jobExists(job.getTitle(), job.getCompany())) {
                if (saveJob(job)) {
                    count++;
                }
            } else {
                duplicates++;
            }
        }

        if (duplicates > 0) {
            System.out.println("   ℹ️  " + duplicates + " doublons ignorés");
        }

        return count;
    }

    /**
     * ═══════════════════════════════════════════════════════════
     * RÉCUPÉRER TOUTES LES OFFRES
     * ═══════════════════════════════════════════════════════════
     */
    public static List<JobOffer> getAllJobs() {
        List<JobOffer> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs ORDER BY date_added DESC";

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JobOffer job = new JobOffer(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("company"),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getString("link"),
                        rs.getString("source"),
                        rs.getString("date_added"));
                jobs.add(job);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération: " + e.getMessage());
        }

        return jobs;
    }

    /**
     * ═══════════════════════════════════════════════════════════
     * RECHERCHER DES OFFRES
     * ═══════════════════════════════════════════════════════════
     */
    public static List<JobOffer> searchJobs(String keyword) {
        List<JobOffer> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs WHERE title LIKE ? OR company LIKE ? OR description LIKE ? ORDER BY date_added DESC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JobOffer job = new JobOffer(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("company"),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getString("link"),
                        rs.getString("source"),
                        rs.getString("date_added"));
                jobs.add(job);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
        }

        return jobs;
    }

    /**
     * ═══════════════════════════════════════════════════════════
     * SUPPRIMER UNE OFFRE
     * ═══════════════════════════════════════════════════════════
     */
    public static boolean deleteJob(int jobId) {
        String sql = "DELETE FROM jobs WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jobId);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════
     * VÉRIFIER SI UNE OFFRE EXISTE (ÉVITER DOUBLONS)
     * ═══════════════════════════════════════════════════════════
     */
    public static boolean jobExists(String title, String company) {
        String sql = "SELECT COUNT(*) FROM jobs WHERE title = ? AND company = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, company);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification: " + e.getMessage());
            return false;
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════
     * STATISTIQUES SIMPLES
     * ═══════════════════════════════════════════════════════════
     */
    public static void printStatistics() {
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            System.out.println("\n STATISTIQUES BASE DE DONNÉES:");
            System.out.println("═".repeat(60));

            // Total offres
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM jobs");
            if (rs.next()) {
                System.out.println(" Total offres : " + rs.getInt(1));
            }

            // Par source
            rs = stmt.executeQuery(
                    "SELECT source, COUNT(*) as count FROM jobs " +
                            "GROUP BY source");

            System.out.println("\n🌐 Par source :");
            while (rs.next()) {
                System.out.println("   • " + rs.getString("source") +
                        " : " + rs.getInt("count") + " offres");
            }

            // Par entreprise (top 10)
            rs = stmt.executeQuery(
                    "SELECT company, COUNT(*) as count FROM jobs " +
                            "GROUP BY company " +
                            "ORDER BY count DESC " +
                            "LIMIT 10");

            System.out.println("\n🏢 Top 10 entreprises :");
            int rank = 1;
            while (rs.next()) {
                System.out.println("   " + rank + ". " + rs.getString("company") +
                        " (" + rs.getInt("count") + " offres)");
                rank++;
            }

            // Par ville (top 10)
            rs = stmt.executeQuery(
                    "SELECT location, COUNT(*) as count FROM jobs " +
                            "GROUP BY location " +
                            "ORDER BY count DESC " +
                            "LIMIT 10");

            System.out.println("\n📍 Top 10 villes :");
            rank = 1;
            while (rs.next()) {
                System.out.println("   " + rank + ". " + rs.getString("location") +
                        " (" + rs.getInt("count") + " offres)");
                rank++;
            }

            // Offres récentes (dernières 24h si date_added est renseignée)
            rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM jobs " +
                            "WHERE date_added >= datetime('now', '-1 day')");

            if (rs.next()) {
                int recentCount = rs.getInt(1);
                if (recentCount > 0) {
                    System.out.println("\n🆕 Offres récentes (24h) : " + recentCount);
                }
            }

            System.out.println("═".repeat(60));

        } catch (SQLException e) {
            System.err.println("Erreur statistiques: " + e.getMessage());
        }
    }
}