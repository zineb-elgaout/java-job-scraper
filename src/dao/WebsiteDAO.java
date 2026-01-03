package dao;

import model.Website;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WebsiteDAO {

    public static List<Website> getAllWebsites() {

        List<Website> list = new ArrayList<>();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM websites");

            while (rs.next()) {
                Website w = new Website(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("url")
                );
                list.add(w);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
