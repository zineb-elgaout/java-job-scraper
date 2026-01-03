package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import scraping.EmploiMaScraper;
import scraping.MarocAnnoncesScraper;
import scraping.RekruteScraper;
import model.JobOffer;
import dao.JobOfferDAO;

import javax.swing.SwingWorker;
import java.util.List;

public class SiteSelectionFrame extends JDialog {
    
    // Mêmes couleurs que HomeFrame pour conserver le même style
    private final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 252);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private final Color TEXT_SECONDARY = new Color(127, 140, 141);
    private final Color BORDER_COLOR = new Color(230, 233, 238);
    private final Color HOVER_COLOR = new Color(245, 250, 255);
    private final Color DIALOG_BG = new Color(255, 255, 255);
    private final Color DIALOG_HEADER = new Color(41, 128, 185);
    
    private String selectedSite = null;
    private ButtonGroup siteGroup = new ButtonGroup();
    private JButton startButton;
    
    public SiteSelectionFrame(JFrame parent) {
        super(parent, "Sélection du site", true);
        initUI();
    }
    
    private void initUI() {
        setTitle("JobScraper • Sélection du site");
        setSize(900, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Panel principal avec le même fond que HomeFrame
        JPanel mainPanel = new JPanel(new BorderLayout());
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
        JLabel titleLabel = new JLabel("Sélectionnez un site à scraper");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        // Bouton fermer à droite (style cohérent)
        JLabel closeLabel = createCloseLabel();
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(closeLabel, BorderLayout.EAST);
        
        // ========== CONTENU PRINCIPAL ==========
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(30, 60, 30, 60));
        
        // Sites marocains d'emploi
        String[] siteNames = {"Rekrute", "Emploi.ma", "MarocAnnonces"};
        String[] siteDescriptions = {
            "Site d'emploi leader au Maroc avec offres qualitatives et ciblées",
            "Plateforme générale d'emploi pour le marché marocain, toutes industries",
            "Portail d'annonces générales incluant des offres d'emploi marocaines"
        };
        String[] iconNames = {"rekrute.png", "emploi.png", "maroc_annonces.png"};
        
        for (int i = 0; i < siteNames.length; i++) {
            JPanel siteCard = createSiteCard(siteNames[i], siteDescriptions[i], iconNames[i]);
            contentPanel.add(siteCard);
            if (i < siteNames.length - 1) {
                contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }
        
        // Ajouter un espace flexible pour centrer les cartes
        contentPanel.add(Box.createVerticalGlue());
        
        // ========== PANEL BOUTONS ==========
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(15, 40, 15, 40)
        ));
        
        // Bouton Annuler
        JButton cancelButton = createStyledButton("Annuler", TEXT_SECONDARY);
        cancelButton.addActionListener(e -> dispose());
        
        // Bouton Démarrer (initialement désactivé)
        startButton = createStyledButton("Démarrer le scraping", PRIMARY_COLOR);
        startButton.setEnabled(false);
        
        startButton.addActionListener(e -> {
            if (selectedSite != null) {
                dispose();
                showScrapingProgress(selectedSite);
            }
        });
        
        buttonPanel.setLayout(new BorderLayout());
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonContainer.setBackground(BACKGROUND_COLOR);
        buttonContainer.add(cancelButton);
        buttonContainer.add(startButton);
        buttonPanel.add(buttonContainer, BorderLayout.CENTER);
        
        // Assemblage final
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private JPanel createSiteCard(String siteName, String description, String iconName) {
        // Carte dans le même style que HomeFrame
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(CARD_COLOR);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Bordure avec padding
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Taille fixe avec hauteur de 120px
        card.setPreferredSize(new Dimension(700, 120));
        card.setMaximumSize(new Dimension(700, 120));
        card.setMinimumSize(new Dimension(700, 120));
        
        // Radio button pour la sélection
        JRadioButton radioButton = new JRadioButton();
        radioButton.setBackground(CARD_COLOR);
        radioButton.setFocusPainted(false);
        siteGroup.add(radioButton);
        
        // Icône du site - version carrée
        JLabel iconLabel = createSimpleIcon(iconName, siteName);
        
        // Panel pour le contenu textuel avec BoxLayout
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(CARD_COLOR);
        textPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        
        // Nom du site en bleu (comme HomeFrame)
        JLabel nameLabel = new JLabel(siteName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(PRIMARY_COLOR);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Description
        JLabel descLabel = new JLabel("<html><div style='width: 480px; color: #7b8a8b; font-size: 13px; line-height: 1.4;'>" 
            + description + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(nameLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        textPanel.add(descLabel);
        
        // Indicateur de sélection (flèche)
        JLabel arrowLabel = new JLabel("→");
        arrowLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        arrowLabel.setForeground(PRIMARY_COLOR);
        arrowLabel.setVisible(false);
        
        // Stocker la flèche comme propriété client de la carte
        card.putClientProperty("arrowLabel", arrowLabel);
        
        // Gestionnaire d'événements pour la carte
        MouseAdapter cardListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radioButton.setSelected(true);
                selectedSite = siteName;
                updateStartButton();
                highlightCard(card, arrowLabel, true);
                
                // Désélectionner les autres cartes
                Container parent = card.getParent();
                if (parent != null) {
                    for (Component comp : parent.getComponents()) {
                        if (comp != card && comp instanceof JPanel) {
                            JPanel otherCard = (JPanel) comp;
                            JLabel otherArrow = (JLabel) otherCard.getClientProperty("arrowLabel");
                            if (otherArrow != null) {
                                otherArrow.setVisible(false);
                            }
                            resetCardStyle(otherCard);
                        }
                    }
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!radioButton.isSelected()) {
                    card.setBackground(HOVER_COLOR);
                    textPanel.setBackground(HOVER_COLOR);
                    radioButton.setBackground(HOVER_COLOR);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!radioButton.isSelected()) {
                    card.setBackground(CARD_COLOR);
                    textPanel.setBackground(CARD_COLOR);
                    radioButton.setBackground(CARD_COLOR);
                }
            }
        };
        
        // Ajouter le listener à la carte et au radio button
        card.addMouseListener(cardListener);
        radioButton.addActionListener(e -> {
            if (radioButton.isSelected()) {
                selectedSite = siteName;
                updateStartButton();
                highlightCard(card, arrowLabel, true);
                
                // Désélectionner les autres cartes
                Container parent = card.getParent();
                if (parent != null) {
                    for (Component comp : parent.getComponents()) {
                        if (comp != card && comp instanceof JPanel) {
                            JPanel otherCard = (JPanel) comp;
                            JLabel otherArrow = (JLabel) otherCard.getClientProperty("arrowLabel");
                            if (otherArrow != null) {
                                otherArrow.setVisible(false);
                            }
                            resetCardStyle(otherCard);
                        }
                    }
                }
            }
        });
        
        // Assemblage de la carte
        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setBackground(CARD_COLOR);
        
        // Conteneur pour centrer l'icône carrée
        JPanel iconContainer = new JPanel(new GridBagLayout());
        iconContainer.setBackground(CARD_COLOR);
        iconContainer.setPreferredSize(new Dimension(70, 0));
        iconContainer.add(iconLabel);
        
        leftPanel.add(radioButton, BorderLayout.WEST);
        leftPanel.add(iconContainer, BorderLayout.CENTER);
        
        card.add(leftPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.add(arrowLabel, BorderLayout.EAST);
        
        return card;
    }
    
    private JLabel createSimpleIcon(String iconName, String siteName) {
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(50, 50)); // Carré 50x50
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Essayer de charger l'icône depuis plusieurs emplacements
        ImageIcon icon = tryLoadIcon(iconName);
        
        if (icon != null) {
            // Si l'image est trouvée, l'afficher en carré
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(scaledImg));
        } else {
            // Icône carrée simple avec lettre
            String firstLetter = siteName.length() > 0 ? 
                String.valueOf(siteName.charAt(0)).toUpperCase() : "M";
            iconLabel.setText(firstLetter);
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            iconLabel.setOpaque(true);
            iconLabel.setBackground(new Color(235, 245, 255));
            iconLabel.setForeground(PRIMARY_COLOR);
            iconLabel.setBorder(BorderFactory.createLineBorder(
                new Color(200, 225, 255), 2));
        }
        
        return iconLabel;
    }
    
    private ImageIcon tryLoadIcon(String filename) {
        // Essayer plusieurs emplacements (même logique que HomeFrame)
        java.net.URL url = getClass().getResource("icons/" + filename);
        if (url != null) return new ImageIcon(url);
        
        url = getClass().getResource(filename);
        if (url != null) return new ImageIcon(url);
        
        url = getClass().getClassLoader().getResource("icons/" + filename);
        if (url != null) return new ImageIcon(url);
        
        String projectPath = System.getProperty("user.dir");
        String[] possiblePaths = {
            projectPath + "/icons/" + filename,
            projectPath + "/src/icons/" + filename,
            projectPath + "/src/ui/icons/" + filename,
            "icons/" + filename,
            "src/icons/" + filename
        };
        
        for (String path : possiblePaths) {
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                return new ImageIcon(path);
            }
        }
        
        return null;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        // Créer un bouton dans le même style que HomeFrame
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (!isEnabled()) {
                    g2.setColor(new Color(200, 200, 200));
                } else if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 40));
        
        return button;
    }
    
    private JLabel createCloseLabel() {
        JLabel closeLabel = new JLabel("Fermer");
        closeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeLabel.setForeground(PRIMARY_COLOR);
        closeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeLabel.setText("<html><u>Fermer</u></html>");
                closeLabel.setForeground(new Color(31, 118, 175));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                closeLabel.setText("Fermer");
                closeLabel.setForeground(PRIMARY_COLOR);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });
        
        return closeLabel;
    }
    
    private void updateStartButton() {
        // Activer le bouton Démarrer si un site est sélectionné
        if (startButton != null) {
            startButton.setEnabled(selectedSite != null);
        }
    }
    
    private void highlightCard(JPanel card, JLabel arrowLabel, boolean highlight) {
        if (highlight) {
            card.setBackground(HOVER_COLOR);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                new EmptyBorder(19, 19, 19, 19)
            ));
            arrowLabel.setVisible(true);
        } else {
            card.setBackground(CARD_COLOR);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
            ));
            arrowLabel.setVisible(false);
        }
    }
    
    private void resetCardStyle(JPanel card) {
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Réinitialiser aussi les panels enfants
        for (Component comp : card.getComponents()) {
            if (comp instanceof JPanel) {
                ((JPanel) comp).setBackground(CARD_COLOR);
            }
        }
    }
    
    private void showScrapingProgress(String site) {
        // Créer une boîte de dialogue de progression
        JDialog progressDialog = new JDialog(this, "Scraping en cours", true);
        progressDialog.setSize(500, 350);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setUndecorated(true);
        progressDialog.setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(DIALOG_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DIALOG_HEADER);
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel titleLabel = new JLabel("Scraping en cours");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Contenu
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(DIALOG_BG);
        contentPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        
        JLabel siteLabel = new JLabel("Site: " + site);
        siteLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        siteLabel.setForeground(TEXT_PRIMARY);
        siteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoLabel = new JLabel("Collecte des données en cours...");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Bouton Annuler
        JButton cancelButton = createDialogButton("Annuler", 
            new Color(149, 165, 166), new Color(127, 140, 141));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(DIALOG_BG);
        buttonPanel.add(cancelButton);
        
        contentPanel.add(siteLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(progressBar);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(infoLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(buttonPanel);
        
        // Assemblage
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        progressDialog.add(mainPanel);
        
        // ⭐⭐ VRAI SCRAPING DANS UN THREAD SÉPARÉ ⭐⭐
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            private int offersCount = 0;
            private String errorMessage = null;
            private boolean interrupted = false;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Exécuter le scraper selon le site sélectionné
                    switch(site.toLowerCase()) {
                        case "rekrute":
                            // Utiliser RekruteScraper
                            RekruteScraper scraper1 = new RekruteScraper();
                            List<JobOffer> offers1 = scraper1.scrapeAll("", "");
                            offersCount = offers1.size();
                            
                            // Insérer dans la base de données
                            JobOfferDAO dao = new JobOfferDAO();
                            for (JobOffer offer : offers1) {
                                if (isCancelled()) {
                                    interrupted = true;
                                    return null;
                                }
                                dao.insert(offer);
                            }
                            break;
                            
                        case "emploi.ma":
                            EmploiMaScraper scraper2 = new EmploiMaScraper();
                            List<JobOffer> offers2 = scraper2.scrape();
                            offersCount = offers2.size();
                            
                            JobOfferDAO dao2 = new JobOfferDAO();
                            for (JobOffer offer : offers2) {
                                if (isCancelled()) {
                                    interrupted = true;
                                    return null;
                                }
                                dao2.insert(offer);
                            }
                            break;
                            
                        case "marocannonces":
                            // Utiliser MarocAnnoncesScraper avec seulement 5 pages pour éviter les problèmes
                            MarocAnnoncesScraper scraper3 = new MarocAnnoncesScraper();
                            List<JobOffer> offers3 = scraper3.scrape(500);
                            offersCount = offers3.size();
                            
                            JobOfferDAO dao3 = new JobOfferDAO();
                            for (JobOffer offer : offers3) {
                                if (isCancelled()) {
                                    interrupted = true;
                                    return null;
                                }
                                dao3.insert(offer);
                            }
                            break;
                    }
                } catch (Exception e) {
                    // Vérifier si c'est une interruption
                    if (e instanceof InterruptedException || Thread.currentThread().isInterrupted()) {
                        interrupted = true;
                    } else {
                        errorMessage = e.getMessage();
                        e.printStackTrace();
                    }
                }
                return null;
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                
                // Afficher le résultat
                if (interrupted) {
                    JOptionPane.showMessageDialog(SiteSelectionFrame.this,
                        "⏹️  Scraping interrompu par l'utilisateur.\n" +
                        "Les offres collectées ont été sauvegardées.",
                        "Scraping interrompu",
                        JOptionPane.INFORMATION_MESSAGE);
                } else if (errorMessage != null) {
                    JOptionPane.showMessageDialog(SiteSelectionFrame.this,
                        "Erreur lors du scraping :\n" + errorMessage,
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                } else if (offersCount > 0) {
                    JOptionPane.showMessageDialog(SiteSelectionFrame.this,
                        String.format(
                            "✅ Scraping terminé avec succès !\n\n" +
                            "📊 %d offres ont été collectées et sauvegardées.\n" +
                            "Consultez-les dans l'onglet \"Voir les offres\".",
                            offersCount
                        ),
                        "Scraping terminé",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(SiteSelectionFrame.this,
                        String.format(
                            "⚠️ Scraping terminé sur %s.\n\n" +
                            "Aucune nouvelle offre n'a été trouvée.\n" +
                            "Veuillez réessayer plus tard.",
                            site
                        ),
                        "Aucune offre trouvée",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        
        // Action pour le bouton Annuler
        cancelButton.addActionListener(e -> {
            worker.cancel(true);
            progressDialog.dispose();
        });
        
        // Démarrer le scraping
        worker.execute();
        
        progressDialog.setVisible(true);
    }
    
    private JButton createDialogButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(hoverColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(bgColor);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        
        return button;
    }
}