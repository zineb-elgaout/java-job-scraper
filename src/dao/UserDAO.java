package dao;

import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    // =========================
    // REGISTER
    // =========================
    public static boolean register(String username, String password) {

        String checkSql = "SELECT id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users(username, password) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            if (conn == null) return false;

            // 1️⃣ Vérifier si username existe déjà
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false; // username déjà utilisé
            }

            // 2️⃣ Insérer l'utilisateur
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); // simple (sans hash pour l'instant)

            insertStmt.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // LOGIN
    // =========================
    public static boolean login(String username, String password) {

        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection()) {

            if (conn == null) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true si trouvé

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
