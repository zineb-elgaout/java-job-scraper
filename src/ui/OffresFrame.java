package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import dao.JobOfferDAOExtended;
import model.JobOffer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OffresFrame extends JFrame {

    private final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 252);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private final Color TEXT_SECONDARY = new Color(127, 140, 141);
    private final Color BORDER_COLOR = new Color(230, 233, 238);
    private final Color HOVER_COLOR = new Color(245, 250, 255);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color ERROR_COLOR = new Color(231, 76, 60);

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel countLabel;
    private JButton exportButton;
    private List<JobOffer> jobOffers;
    
    private File selectedFile;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public OffresFrame() {
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("JobScraper • Offres d'emploi");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Panel principal avec BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        setContentPane(mainPanel);

        // ========== HEADER ==========
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(20, 40, 20, 40)
        ));

        // Titre à gauche
        JLabel titleLabel = new JLabel("Offres d'emploi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);

        // Bouton retour à droite
        JLabel backLabel = createBackLabel();

        header.add(titleLabel, BorderLayout.WEST);
        header.add(backLabel, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        // ========== PANEL PRINCIPAL CONTENANT TOUT ==========
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        // ========== PANEL D'INFORMATIONS ==========
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setBorder(new EmptyBorder(15, 40, 10, 40));

        // Compteur d'offres seulement
        countLabel = new JLabel("Chargement des données...");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        countLabel.setForeground(TEXT_SECONDARY);

        infoPanel.add(countLabel, BorderLayout.WEST);
        contentPanel.add(infoPanel, BorderLayout.NORTH);

        // ========== TABLEAU DES OFFRES ==========
        JPanel tablePanel = new JPanel(new BorderLayout(0, 0));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new EmptyBorder(0, 40, 0, 40));

        // Modèle de table
        String[] columns = {"ID", "Titre", "Entreprise", "Lieu", "Source", "Date", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(new Color(225, 245, 254));
        table.setSelectionForeground(PRIMARY_COLOR);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // En-tête de table
        JTableHeader headerTable = table.getTableHeader();
        headerTable.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerTable.setBackground(new Color(240, 242, 245));
        headerTable.setForeground(TEXT_PRIMARY);
        headerTable.setReorderingAllowed(false);

        // Configuration manuelle des largeurs de colonnes
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);   // ID - réduit
        columnModel.getColumn(1).setPreferredWidth(250);  // Titre
        columnModel.getColumn(2).setPreferredWidth(150);  // Entreprise
        columnModel.getColumn(3).setPreferredWidth(100);  // Lieu
        columnModel.getColumn(4).setPreferredWidth(100);  // Source
        columnModel.getColumn(5).setPreferredWidth(100);  // Date
        columnModel.getColumn(6).setPreferredWidth(400);  // Description - augmenté

        // Scroll pane avec style
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Ajouter un effet de survol aux lignes
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                }
            }
        });

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        // ========== PANEL DE DETAILS POUR LE LIEN ==========
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(10, 40, 10, 40)
        ));
        detailsPanel.setPreferredSize(new Dimension(0, 80));

        JLabel detailsTitle = new JLabel("Lien de l'offre");
        detailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        detailsTitle.setForeground(PRIMARY_COLOR);

        JTextField linkField = new JTextField();
        linkField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        linkField.setForeground(TEXT_PRIMARY);
        linkField.setBackground(new Color(250, 251, 252));
        linkField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        linkField.setEditable(false);

        // Lorsqu'une ligne est sélectionnée, afficher le lien
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int selectedRow = table.getSelectedRow();
                if (jobOffers != null && selectedRow < jobOffers.size()) {
                    JobOffer offer = jobOffers.get(selectedRow);
                    linkField.setText(offer.getLink());
                }
            }
        });

        JPanel detailsContainer = new JPanel(new BorderLayout(5, 5));
        detailsContainer.setBackground(Color.WHITE);
        detailsContainer.add(detailsTitle, BorderLayout.NORTH);
        detailsContainer.add(linkField, BorderLayout.CENTER);

        detailsPanel.add(detailsContainer, BorderLayout.CENTER);
        contentPanel.add(detailsPanel, BorderLayout.SOUTH);

        // Ajouter le contentPanel au mainPanel
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // ========== PANEL BOUTON EXPORT EN BAS ==========
        JPanel exportPanel = new JPanel(new BorderLayout());
        exportPanel.setBackground(BACKGROUND_COLOR);
        exportPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(15, 40, 15, 40)
        ));
        exportPanel.setPreferredSize(new Dimension(0, 70));
        
        exportButton = createExportButton();
        JPanel buttonContainer = new JPanel(new GridBagLayout()); // Utiliser GridBagLayout pour centrer
        buttonContainer.setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonContainer.add(exportButton, gbc);
        
        exportPanel.add(buttonContainer, BorderLayout.CENTER);
        mainPanel.add(exportPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JLabel createBackLabel() {
        JLabel backLabel = new JLabel("Retour");
        backLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backLabel.setForeground(PRIMARY_COLOR);
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backLabel.setText("<html><u>Retour</u></html>");
                backLabel.setForeground(new Color(31, 118, 175));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backLabel.setText("Retour");
                backLabel.setForeground(PRIMARY_COLOR);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });

        return backLabel;
    }

    private JButton createExportButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(new Color(200, 200, 200));
                } else if (getModel().isPressed()) {
                    g2.setColor(SUCCESS_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(SUCCESS_COLOR.brighter());
                } else {
                    g2.setColor(SUCCESS_COLOR);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setText(" Télécharger Excel");
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.CENTER); // Centrer le texte
        button.setVerticalAlignment(SwingConstants.CENTER);   // Centrer le texte verticalement
        button.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40)); // Padding égal des deux côtés
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setEnabled(false);
        button.setPreferredSize(new Dimension(220, 45));

        button.addActionListener(e -> exportToExcelCSV());

        return button;
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                JobOfferDAOExtended dao = new JobOfferDAOExtended();
                jobOffers = dao.getAllJobOffers();
                return null;
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    
                    if (jobOffers != null && !jobOffers.isEmpty()) {
                        int id = 1;
                        for (JobOffer offer : jobOffers) {
                            Object[] row = {
                                id++,
                                shortenText(offer.getTitle(), 60),
                                shortenText(offer.getCompany(), 25),
                                shortenText(offer.getLocation(), 20),
                                offer.getSource(),
                                getFormattedDate(offer),
                                shortenText(offer.getDescription(), 120) // Description plus longue
                            };
                            tableModel.addRow(row);
                        }
                        
                        countLabel.setText(String.format("📊 %d offres trouvées", jobOffers.size()));
                        exportButton.setEnabled(true);
                    } else {
                        countLabel.setText("📭 Aucune offre trouvée dans la base de données");
                    }
                } catch (Exception e) {
                    countLabel.setText("❌ Erreur lors du chargement des données");
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private String getFormattedDate(JobOffer offer) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    private String shortenText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private void exportToExcelCSV() {
        if (jobOffers == null || jobOffers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucune donnée à exporter.",
                "Export Excel",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le fichier Excel");
        fileChooser.setSelectedFile(new File("offres_emploi.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
            }

            SwingWorker<Void, Void> exportWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try (PrintWriter writer = new PrintWriter(selectedFile, "UTF-8")) {
                        writer.write('\ufeff');
                        writer.println("ID,Titre,Entreprise,Lieu,Source,Date,Description,Lien");
                        
                        int id = 1;
                        for (JobOffer offer : jobOffers) {
                            String row = String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                                id++,
                                escapeCsv(offer.getTitle()),
                                escapeCsv(offer.getCompany()),
                                escapeCsv(offer.getLocation()),
                                escapeCsv(offer.getSource()),
                                escapeCsv(getFormattedDate(offer)),
                                escapeCsv(offer.getDescription()),
                                escapeCsv(offer.getLink())
                            );
                            writer.println(row);
                        }
                    }
                    return null;
                }
                
                private String escapeCsv(String value) {
                    if (value == null) return "";
                    value = value.replace("\"", "\"\"");
                    value = value.replace("\n", " ").replace("\r", " ");
                    return value;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        
                        String message = String.format(
                            "<html>"
                            + "<div style='font-size:11pt; font-weight:bold; color:#27ae60;'>"
                            + "✅ Téléchargement terminé"
                            + "</div>"
                            + "<div style='font-size:10pt; margin-top:8px;'>"
                            + "Fichier enregistré :<br>"
                            + "<span style='color:#7b8a8b;'>%s</span>"
                            + "</div>"
                            + "<div style='font-size:9pt; color:#3498db; margin-top:8px;'>"
                            + "%d offres exportées"
                            + "</div>"
                            + "</html>",
                            selectedFile.getName(),
                            jobOffers.size()
                        );
                        
                        JOptionPane.showMessageDialog(OffresFrame.this,
                            message,
                            "Téléchargement",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(OffresFrame.this,
                            "<html><div style='color:#e74c3c; font-weight:bold;'>"
                            + "❌ Erreur lors du téléchargement"
                            + "</div></html>",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            exportWorker.execute();
        }
    }
}