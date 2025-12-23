package ui;

import javax.swing.*;
import java.awt.*;

public class HomeFrame extends JFrame {

    public HomeFrame(String username) {

        setTitle("Accueil - Job Scraper App");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Header =====
        JLabel welcome = new JLabel("Bienvenue " + username, SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 26));
        welcome.setForeground(new Color(41, 128, 185));
        welcome.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        add(welcome, BorderLayout.NORTH);

        // ===== Contenu principal =====
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 2, 20, 20));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JButton scrapeBtn = new JButton("🔍 Lancer le Scraping");
        JButton jobsBtn = new JButton("📄 Voir les offres");
        JButton profileBtn = new JButton("👤 Profil");
        JButton logoutBtn = new JButton("🚪 Déconnexion");

        Font btnFont = new Font("Arial", Font.BOLD, 14);
        for (JButton btn : new JButton[]{scrapeBtn, jobsBtn, profileBtn, logoutBtn}) {
            btn.setFont(btnFont);
            btn.setBackground(new Color(52, 152, 219));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
        }

        centerPanel.add(scrapeBtn);
        centerPanel.add(jobsBtn);
        centerPanel.add(profileBtn);
        centerPanel.add(logoutBtn);

        add(centerPanel, BorderLayout.CENTER);

        // ===== Action logout =====
        logoutBtn.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        setVisible(true);
    }
}
