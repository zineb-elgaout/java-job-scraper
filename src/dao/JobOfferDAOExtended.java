package dao;

import model.JobOffer;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobOfferDAOExtended {
    
    // Récupérer toutes les offres de la base
    public List<JobOffer> getAllJobOffers() {
        List<JobOffer> offers = new ArrayList<>();
        
        String query = "SELECT title, company, location, description, link, source " +
                      "FROM jobs ORDER BY id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                JobOffer offer = new JobOffer(
                    rs.getString("title"),
                    rs.getString("company"),
                    rs.getString("location"),
                    rs.getString("description"),
                    rs.getString("link"),
                    rs.getString("source")
                );
                offers.add(offer);
            }
            
            System.out.println("✅ " + offers.size() + " offres chargées depuis la base");
            
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du chargement des offres");
            e.printStackTrace();
        }
        
        return offers;
    }
}
