package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class HomeFrame extends JFrame {

    private final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 252);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private final Color TEXT_SECONDARY = new Color(127, 140, 141);
    private final Color BORDER_COLOR = new Color(230, 233, 238);
    private final Color HOVER_COLOR = new Color(245, 250, 255);
    private final Color DIALOG_BG = new Color(255, 255, 255);
    private final Color DIALOG_HEADER = new Color(41, 128, 185);

    private final String[] TITLES = {
        "Lancer le scraping",
        "Voir les offres", 
        "Statistiques",
        "Filtrer & trier"
    };
    
    // Noms des fichiers d'images
    private final String[] IMAGE_FILES = {
        "scraping.jpg",
        "offers.jpg", 
        "stats.jpg",
        "filter.jpg"
    };

    public HomeFrame(String username) {
        initUI(username);
    }

    private void initUI(String username) {
        setTitle("JobScraper • Dashboard");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        setContentPane(mainPanel);

        // ========== HEADER SIMPLIFIÉ ==========
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(20, 40, 20, 40)
        ));
        
        // "Bonjour, [nom d'utilisateur]" à gauche 
        JLabel welcomeLabel = new JLabel("Bonjour, " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(PRIMARY_COLOR); 
        
        // "Déconnexion" simple à droite  
        JLabel logoutLabel = createLogoutLabel();
        
        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(logoutLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ========== CONTENU PRINCIPAL ==========
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(40, 60, 40, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Créer les 4 cartes avec des images
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                int index = row * 2 + col;
                gbc.gridx = col;
                gbc.gridy = row;
                
                JPanel card = createCardWithImage(index);
                contentPanel.add(card, gbc);
            }
        }

        add(contentPanel, BorderLayout.CENTER);

        // ========== FOOTER ==========
        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(15, 40, 15, 40)
        ));
        
        JLabel footerText = new JLabel("© 2024 JobScraper • Version 1.0");
        footerText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerText.setForeground(TEXT_SECONDARY);
        footer.add(footerText);
        
        add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createCardWithImage(int index) {
        // Carte simple
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Bordure avec padding
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(40, 30, 40, 30)
        ));
        
        // Taille fixe
        card.setPreferredSize(new Dimension(300, 200));
        card.setMinimumSize(new Dimension(300, 200));

        // Charger l'image
        JLabel iconLabel = loadImageFromMultipleLocations(index);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Espacement
        Component verticalStrut = Box.createVerticalStrut(25);
        
        // Titre
        JLabel titleLabel = new JLabel(TITLES[index]);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assemblage
        card.add(iconLabel);
        card.add(verticalStrut);
        card.add(titleLabel);

        // Effet de survol
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(HOVER_COLOR);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                    new EmptyBorder(39, 29, 39, 29)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_COLOR);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(40, 30, 40, 30)
                ));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Ajouter l'action pour chaque carte
                switch(index) {
                    case 0: // "Lancer le scraping"
                        new SiteSelectionFrame(HomeFrame.this);
                        break;
                    case 1: // "Voir les offres"
                        new OffresFrame();
                        break;
                    case 2: // "Statistiques"
                        JOptionPane.showMessageDialog(HomeFrame.this, 
                            "Affichage des statistiques...", 
                            "Information", 
                            JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case 3: // "Filtrer & trier"
                    	JOptionPane.showMessageDialog(HomeFrame.this, 
                                "Affichage des statistiques...", 
                                "Information", 
                                JOptionPane.INFORMATION_MESSAGE);
                        break;
                }
            }
        });

        return card;
    }

    private JLabel loadImageFromMultipleLocations(int index) {
        JLabel label = new JLabel();
        String filename = IMAGE_FILES[index];
        
        // Essayer plusieurs emplacements
        ImageIcon icon = tryLoadImage(filename);
        
        if (icon != null) {
            // Si l'image est trouvée, la redimensionner
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImg));
        }
        // Si l'image n'est pas trouvée, le JLabel reste vide
        
        return label;
    }

    private ImageIcon tryLoadImage(String filename) {
        // Essayer plusieurs emplacements différents
        
        // 1. Dans un dossier "icons" à côté de la classe
        java.net.URL url = getClass().getResource("icons/" + filename);
        if (url != null) {
            return new ImageIcon(url);
        }
        
        // 2. Directement dans le package ui
        url = getClass().getResource(filename);
        if (url != null) {
            return new ImageIcon(url);
        }
        
        // 3. Dans le classpath racine
        url = getClass().getClassLoader().getResource("icons/" + filename);
        if (url != null) {
            return new ImageIcon(url);
        }
        
        // 4. Chemin absolu depuis le dossier du projet
        String projectPath = System.getProperty("user.dir");
        String[] possiblePaths = {
            projectPath + "/icons/" + filename,
            projectPath + "/src/icons/" + filename,
            projectPath + "/src/ui/icons/" + filename,
            "icons/" + filename,
            "src/icons/" + filename
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                return new ImageIcon(path);
            }
        }
        
        return null;
    }

    private JLabel createLogoutLabel() {
        // Crée un simple JLabel pour la déconnexion (pas un bouton)
        JLabel logoutLabel = new JLabel("Déconnexion");
        logoutLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutLabel.setForeground(PRIMARY_COLOR);
        logoutLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Effet au survol : souligné + bleu plus foncé
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutLabel.setText("<html><u>Déconnexion</u></html>");
                logoutLabel.setForeground(new Color(31, 118, 175));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                logoutLabel.setText("Déconnexion");
                logoutLabel.setForeground(PRIMARY_COLOR);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                showStyledLogoutDialog();
            }
        });
        
        return logoutLabel;
    }

    private void showStyledLogoutDialog() {
        // Créer une fenêtre de dialogue personnalisée
        JDialog logoutDialog = new JDialog(this, "Confirmation", true);
        logoutDialog.setSize(400, 250);
        logoutDialog.setLocationRelativeTo(this);
        logoutDialog.setUndecorated(true);
        logoutDialog.setLayout(new BorderLayout());
        
        // Panel principal avec coins arrondis
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
        
        // Header avec icône
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DIALOG_HEADER);
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        headerPanel.setPreferredSize(new Dimension(0, 70));
        
        JLabel titleLabel = new JLabel("Déconnexion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Contenu
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(DIALOG_BG);
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        JLabel questionLabel = new JLabel("Voulez-vous vraiment vous déconnecter ?");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionLabel.setForeground(TEXT_PRIMARY);
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel detailLabel = new JLabel("<html><div style='text-align: center; color: #7b8a8b;'>"
                + "Vous serez redirigé vers la page de connexion.</div></html>");
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        contentPanel.add(questionLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(detailLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(DIALOG_BG);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JButton cancelButton = createDialogButton("Annuler", new Color(149, 165, 166), new Color(127, 140, 141));
        JButton confirmButton = createDialogButton("Se déconnecter", 
            new Color(231, 76, 60), new Color(192, 57, 43));
        
        cancelButton.addActionListener(e -> logoutDialog.dispose());
        confirmButton.addActionListener(e -> {
            logoutDialog.dispose();
            dispose();
            // new LoginFrame(); // À décommenter pour retourner au login
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        
        // Assemblage
        contentPanel.add(buttonPanel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        logoutDialog.add(mainPanel);
        logoutDialog.setVisible(true);
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
        button.setPreferredSize(new Dimension(140, 40));
        
        return button;
    }
}