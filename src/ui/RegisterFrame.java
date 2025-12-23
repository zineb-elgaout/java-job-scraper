package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import dao.UserDAO;

public class RegisterFrame extends JFrame {

    public RegisterFrame() {
        setTitle("Inscription - Job Scraper App");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setLayout(new BorderLayout());

        // Panel principal avec bordure subtile
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        add(mainPanel);

        // ===== PANEL DROIT (Image/Branding minimaliste) =====
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fond noir mat (style Zara)
                g2d.setColor(new Color(18, 18, 18));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Lignes décoratives minimalistes
                g2d.setColor(new Color(60, 60, 60));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawLine(50, 100, getWidth() - 50, 100);
                g2d.drawLine(50, getHeight() - 100, getWidth() - 50, getHeight() - 100);
            }
        };
        leftPanel.setPreferredSize(new Dimension(400, 0));
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcLeft = new GridBagConstraints();

        // Branding minimaliste
        JLabel appName = new JLabel("JOB SCRAPER");
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("Helvetica Neue", Font.PLAIN, 38));
        
        JLabel subtitle = new JLabel("CRÉEZ VOTRE COMPTE");
        subtitle.setForeground(new Color(180, 180, 180));
        subtitle.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
        subtitle.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel brandingPanel = new JPanel();
        brandingPanel.setLayout(new BoxLayout(brandingPanel, BoxLayout.Y_AXIS));
        brandingPanel.setOpaque(false);
        brandingPanel.add(appName);
        brandingPanel.add(subtitle);

        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        leftPanel.add(brandingPanel, gbcLeft);

        mainPanel.add(leftPanel, BorderLayout.EAST);

        // ===== PANEL GAUCHE (Formulaire minimaliste) =====
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBorder(new EmptyBorder(60, 80, 60, 80));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 25, 0);
        gbc.anchor = GridBagConstraints.WEST;

        // Titre épuré
        JLabel registerTitle = new JLabel("INSCRIPTION");
        registerTitle.setFont(new Font("Helvetica Neue", Font.BOLD, 26));
        registerTitle.setForeground(new Color(18, 18, 18));
        registerTitle.setBorder(new EmptyBorder(0, 0, 40, 0));

        // Champs de texte style Zara (ligne en dessous uniquement)
        JTextField username = new JTextField(20);
        username.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        username.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(10, 2, 10, 2)
        ));
        username.setForeground(new Color(18, 18, 18));
        username.setCaretColor(new Color(18, 18, 18));

        JPasswordField password = new JPasswordField(20);
        password.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        password.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(10, 2, 10, 2)
        ));
        password.setForeground(new Color(18, 18, 18));
        password.setCaretColor(new Color(18, 18, 18));

        JPasswordField confirmPassword = new JPasswordField(20);
        confirmPassword.setFont(new Font("Helvetica Neue", Font.PLAIN, 14));
        confirmPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(10, 2, 10, 2)
        ));
        confirmPassword.setForeground(new Color(18, 18, 18));
        confirmPassword.setCaretColor(new Color(18, 18, 18));

        // Labels minimalistes
        JLabel userLabel = new JLabel("NOM D'UTILISATEUR");
        userLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
        userLabel.setForeground(new Color(120, 120, 120));
        userLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel passLabel = new JLabel("MOT DE PASSE");
        passLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
        passLabel.setForeground(new Color(120, 120, 120));
        passLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel confirmLabel = new JLabel("CONFIRMER LE MOT DE PASSE");
        confirmLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
        confirmLabel.setForeground(new Color(120, 120, 120));
        confirmLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        // Bouton style Zara (noir, rectangulaire, net)
        JButton registerBtn = new JButton("S'INSCRIRE");
        registerBtn.setBackground(new Color(18, 18, 18));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setFont(new Font("Helvetica Neue", Font.PLAIN, 11));
        registerBtn.setBorder(new EmptyBorder(15, 40, 15, 40));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setOpaque(true);
        registerBtn.setBorderPainted(false);

        // Effet hover
        registerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(new Color(50, 50, 50));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(new Color(18, 18, 18));
            }
        });

        // Bouton retour connexion (style outline)
        JButton backBtn = new JButton("RETOUR À LA CONNEXION");
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(18, 18, 18));
        backBtn.setFocusPainted(false);
        backBtn.setFont(new Font("Helvetica Neue", Font.PLAIN, 11));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(18, 18, 18), 1),
                new EmptyBorder(14, 40, 14, 40)
        ));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setOpaque(true);

        // Effet hover bouton retour
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backBtn.setBackground(new Color(18, 18, 18));
                backBtn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backBtn.setBackground(Color.WHITE);
                backBtn.setForeground(new Color(18, 18, 18));
            }
        });

        JLabel message = new JLabel("");
        message.setFont(new Font("Helvetica Neue", Font.PLAIN, 11));
        message.setBorder(new EmptyBorder(10, 0, 0, 0));

        // ===== Placement des composants =====
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        rightPanel.add(registerTitle, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 2, 0);
        rightPanel.add(userLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 25, 0);
        rightPanel.add(username, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 2, 0);
        rightPanel.add(passLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 25, 0);
        rightPanel.add(password, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 2, 0);
        rightPanel.add(confirmLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 35, 0);
        rightPanel.add(confirmPassword, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 15, 0);
        rightPanel.add(registerBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(backBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        rightPanel.add(message, gbc);

        // ===== Actions =====
        registerBtn.addActionListener(e -> {
            String user = username.getText().trim();
            String pass = new String(password.getPassword());
            String confirm = new String(confirmPassword.getPassword());

            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                message.setForeground(new Color(180, 0, 0));
                message.setText("Veuillez remplir tous les champs");
                return;
            }

            if (!pass.equals(confirm)) {
                message.setForeground(new Color(180, 0, 0));
                message.setText("Les mots de passe ne correspondent pas");
                return;
            }

            boolean success = UserDAO.register(user, pass);
            if (success) {
                message.setForeground(new Color(0, 120, 0));
                message.setText("Inscription réussie ! Vous pouvez vous connecter.");
                
                // Retour automatique à la connexion après 2 secondes
                Timer timer = new Timer(2000, evt -> {
                    new LoginFrame();
                    dispose();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                message.setForeground(new Color(180, 0, 0));
                message.setText("Erreur : Nom d'utilisateur déjà utilisé");
            }
        });

        backBtn.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        // Action Enter sur les champs
        confirmPassword.addActionListener(e -> registerBtn.doClick());
        password.addActionListener(e -> confirmPassword.requestFocus());
        username.addActionListener(e -> password.requestFocus());

        mainPanel.add(rightPanel, BorderLayout.WEST);

        setVisible(true);
    }
}