package dao;

import model.JobOffer;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JobOfferDAO {

    private static final String INSERT_SQL =
            "INSERT INTO jobs (title, company, location, description, link, source) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String CHECK_EXISTS_SQL =
            "SELECT id FROM jobs WHERE link = ?";

    // 🔍 Vérifier si l'offre existe déjà
    public boolean existsByLink(String link) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(CHECK_EXISTS_SQL)) {

            ps.setString(1, link);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 💾 Insertion
    public void insert(JobOffer offer) {

        if (existsByLink(offer.getLink())) {
            System.out.println("⏭️ Offre déjà existante : " + offer.getLink());
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, offer.getTitle());
            ps.setString(2, offer.getCompany());
            ps.setString(3, offer.getLocation());
            ps.setString(4, offer.getDescription());
            ps.setString(5, offer.getLink());
            ps.setString(6, offer.getSource());

            ps.executeUpdate();
            System.out.println("✅ Offre insérée : " + offer.getTitle());

        } catch (SQLException e) {
            System.out.println("⚠️ Erreur insertion offre");
            e.printStackTrace();
        }
    }
}
