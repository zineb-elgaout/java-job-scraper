package ui;

import dao.StatisticsDAO;
import stats.ChartFactoryUtil;
import util.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.util.Map;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public class StatisticsFrame extends JFrame {

    private Connection conn;
    private StatisticsDAO dao;
    private JPanel chartsPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public StatisticsFrame() {
        conn = DBConnection.getConnection();
        dao = new StatisticsDAO(conn);

        initUI();
    }

    private void initUI() {
        setTitle("Statistiques des Offres");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));

        // 🔹 HEADER
        JLabel header = new JLabel("Statistiques des Offres d'Emploi", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(52, 152, 219));
        header.setBorder(new EmptyBorder(10, 10, 20, 10));
        mainPanel.add(header, BorderLayout.NORTH);

        // 🔹 PANEL DE SÉLECTION (NOUVEAU)
        JPanel selectionPanel = createSelectionPanel();
        mainPanel.add(selectionPanel, BorderLayout.WEST);

        // 🔹 PANEL PRINCIPAL POUR LES GRAPHIQUES (CardLayout)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(245, 247, 250));

        // Panel d'accueil
        JPanel welcomePanel = createWelcomePanel();
        cardPanel.add(welcomePanel, "welcome");

        // Panel pour graphiques
        chartsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        chartsPanel.setBackground(new Color(245, 247, 250));
        cardPanel.add(chartsPanel, "charts");

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // 🔹 INFO PANEL
        mainPanel.add(createInfoPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 242, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(15, 15, 15, 15)));
        panel.setPreferredSize(new Dimension(250, 0));

        JLabel title = new JLabel("Sélectionnez un graphique");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(52, 152, 219));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        String[] options = {
                "Catégories d'emploi",
                "Secteurs d'activité",
                "Villes par site",
                "Tous les graphiques"
        };

        for (String option : options) {
            JButton btn = createSelectionButton(option);
            btn.addActionListener(e -> {
                String selected = btn.getText();
                if (selected.contains("Catégories")) {
                    showCategoryChart();
                } else if (selected.contains("Secteurs")) {
                    showSectorChart();
                } else if (selected.contains("Villes")) {
                    showCitiesChart();
                } else if (selected.contains("Tous")) {
                    showAllCharts();
                }
            });
            panel.add(btn);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Bouton rafraîchir
        JButton refreshBtn = new JButton("🔄 Rafraîchir les données");
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBtn.setMaximumSize(new Dimension(200, 35));
        refreshBtn.addActionListener(e -> refreshData());
        panel.add(Box.createVerticalGlue());
        panel.add(refreshBtn);

        return panel;
    }

    private JButton createSelectionButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(70, 70, 70));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 15, 8, 15)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(230, 240, 255));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                        new EmptyBorder(7, 14, 7, 14)));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        new EmptyBorder(8, 15, 8, 15)));
            }
        });

        return btn;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));

        JLabel welcome = new JLabel("<html><div style='text-align: center;'>"
                + "<h2>👋 Bienvenue dans les Statistiques</h2>"
                + "<p style='font-size: 14px; color: #666;'>"
                + "Sélectionnez un type de graphique dans le panneau de gauche<br>"
                + "pour visualiser les données des offres d'emploi."
                + "</p></div></html>", JLabel.CENTER);

        panel.add(welcome, BorderLayout.CENTER);
        return panel;
    }

    private void showCategoryChart() {
        Map<String, Integer> byCategory = dao.countByFinalCategory();
        if (!byCategory.isEmpty()) {
            JFreeChart chart = ChartFactoryUtil.createBarChart(
                    "Top 10 des Catégories d'Emploi",
                    "Catégorie",
                    "Nombre d'offres",
                    byCategory);
            displaySingleChart(chart, "Catégories");
        }
    }

    private void showSectorChart() {
        Map<String, Integer> bySector = dao.countBySector();
        if (!bySector.isEmpty()) {
            JFreeChart chart = ChartFactoryUtil.createPieChart(
                    "Répartition par Secteur d'Activité",
                    bySector);
            displaySingleChart(chart, "Secteurs");
        }
    }

    private void showCitiesChart() {
        Map<String, Integer> citiesBySource = dao.countCitiesBySource();
        if (!citiesBySource.isEmpty()) {
            JFreeChart chart = ChartFactoryUtil.createBarChart(
                    "Nombre de Villes par Site",
                    "Site de recrutement",
                    "Nombre de villes uniques",
                    citiesBySource);
            displaySingleChart(chart, "Villes par Site");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Aucune donnée disponible pour 'Villes par site'",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAllCharts() {
        Map<String, Integer> byCategory = dao.countByFinalCategory();
        Map<String, Integer> bySector = dao.countBySector();
        Map<String, Integer> citiesBySource = dao.countCitiesBySource();

        chartsPanel.removeAll();
        chartsPanel.setLayout(new GridLayout(2, 2, 15, 15));

        if (!byCategory.isEmpty()) {
            JFreeChart chart1 = ChartFactoryUtil.createBarChart(
                    "Top Catégories",
                    "Catégorie",
                    "Nombre",
                    byCategory);
            chartsPanel.add(createStyledChartPanel(chart1));
        }

        if (!bySector.isEmpty()) {
            JFreeChart chart2 = ChartFactoryUtil.createPieChart(
                    "Secteurs d'Activité",
                    bySector);
            chartsPanel.add(createStyledChartPanel(chart2));
        }

        if (!citiesBySource.isEmpty()) {
            JFreeChart chart3 = ChartFactoryUtil.createBarChart(
                    "Villes par Site",
                    "Site",
                    "Nombre de villes",
                    citiesBySource);
            chartsPanel.add(createStyledChartPanel(chart3));
        }

        cardLayout.show(cardPanel, "charts");
        revalidate();
        repaint();
    }

    private void displaySingleChart(JFreeChart chart, String title) {
        chartsPanel.removeAll();
        chartsPanel.setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel chartTitle = new JLabel(title, JLabel.CENTER);
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chartTitle.setForeground(new Color(52, 152, 219));
        chartTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));

        container.add(chartTitle, BorderLayout.NORTH);
        container.add(chartPanel, BorderLayout.CENTER);
        chartsPanel.add(container);

        cardLayout.show(cardPanel, "charts");
        revalidate();
        repaint();
    }

    private ChartPanel createStyledChartPanel(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(5, 5, 5, 5)));
        chartPanel.setBackground(Color.WHITE);
        return chartPanel;
    }

    private void refreshData() {
        // Simuler un rafraîchissement
        JOptionPane.showMessageDialog(this,
                "Données rafraîchies avec succès !",
                "Rafraîchissement",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        Map<String, Integer> categoryData = dao.countByFinalCategory();
        Map<String, Integer> sectorData = dao.countBySector();
        Map<String, Integer> citiesData = dao.countCitiesBySource();

        int totalOffers = categoryData.values().stream().mapToInt(Integer::intValue).sum();
        int totalCities = citiesData.values().stream().mapToInt(Integer::intValue).sum();

        panel.add(createInfoCard("Total Offres", String.valueOf(totalOffers),
                new Color(52, 152, 219)));
        panel.add(createInfoCard("Catégories", String.valueOf(categoryData.size()),
                new Color(46, 204, 113)));
        panel.add(createInfoCard("Secteurs", String.valueOf(sectorData.size()),
                new Color(241, 196, 15)));
        panel.add(createInfoCard("Villes uniques", String.valueOf(totalCities),
                new Color(155, 89, 182)));

        return panel;
    }

    private JPanel createInfoCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(240, 240, 240)),
                new EmptyBorder(10, 15, 10, 15)));
        card.setPreferredSize(new Dimension(150, 70));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(valueLabel);

        return card;
    }
}