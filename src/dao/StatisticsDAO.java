package dao;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatisticsDAO {

    private Connection conn;

    public StatisticsDAO(Connection conn) {
        this.conn = conn;
    }

    // 📊 TOP 10 catégories
    public Map<String, Integer> countByFinalCategory() {

        Map<String, Integer> data = new LinkedHashMap<>();

        String sql =
            "SELECT LEFT(final_category, 30) AS category, COUNT(*) AS total " +
            "FROM jobs_advanced_categories " +
            "WHERE final_category IS NOT NULL AND final_category != '' " +
            "GROUP BY category " +
            "ORDER BY total DESC " +
            "LIMIT 10";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                data.put(rs.getString("category"), rs.getInt("total"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    // 🏢 Répartition par secteur
    public Map<String, Integer> countBySector() {

        Map<String, Integer> data = new LinkedHashMap<>();

        String sql =
            "SELECT activity_sector, COUNT(*) AS total " +
            "FROM jobs_advanced_categories " +
            "WHERE activity_sector IS NOT NULL AND activity_sector != '' " +
            "GROUP BY activity_sector " +
            "ORDER BY total DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                data.put(rs.getString("activity_sector"), rs.getInt("total"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    // 🌍 Nombre de villes par site (corrigé)
    public Map<String, Integer> countCitiesBySource() {

        Map<String, Integer> data = new LinkedHashMap<>();

        String sql =
            "SELECT source, COUNT(DISTINCT location_clean) AS total " +
            "FROM jobs_advanced_categories " +
            "WHERE location_clean IS NOT NULL AND location_clean != '' " +
            "GROUP BY source " +
            "ORDER BY total DESC";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String source = rs.getString("source");
                int total = rs.getInt("total");
                if (source != null && !source.isEmpty()) {
                    data.put(source, total);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }
}