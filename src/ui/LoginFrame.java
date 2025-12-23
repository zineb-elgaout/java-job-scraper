package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import dao.UserDAO;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("Job Scraper App");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setLayout(new BorderLayout());

        // Panel principal avec bordure subtile
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        add(mainPanel);

        // ===== PANEL GAUCHE (Image/Branding minimaliste) =====
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
        
        JLabel subtitle = new JLabel("ANALYSE DES OFFRES");
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

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // ===== PANEL DROIT (Formulaire minimaliste) =====
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBorder(new EmptyBorder(60, 80, 60, 80));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 25, 0);
        gbc.anchor = GridBagConstraints.WEST;

        // Titre épuré
        JLabel loginTitle = new JLabel("CONNEXION");
        loginTitle.setFont(new Font("Helvetica Neue", Font.BOLD, 26));
        loginTitle.setForeground(new Color(18, 18, 18));
        loginTitle.setBorder(new EmptyBorder(0, 0, 40, 0));

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

        // Labels minimalistes
        JLabel userLabel = new JLabel("NOM D'UTILISATEUR");
        userLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
        userLabel.setForeground(new Color(120, 120, 120));
        userLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JLabel passLabel = new JLabel("MOT DE PASSE");
        passLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 10));
        passLabel.setForeground(new Color(120, 120, 120));
        passLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        // Bouton style Zara (noir, rectangulaire, net)
        JButton loginBtn = new JButton("SE CONNECTER");
        loginBtn.setBackground(new Color(18, 18, 18));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Helvetica Neue", Font.PLAIN, 11));
        loginBtn.setBorder(new EmptyBorder(15, 40, 15, 40));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setOpaque(true);
        loginBtn.setBorderPainted(false);

        // Effet hover
        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(50, 50, 50));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(18, 18, 18));
            }
        });

        // Bouton inscription (style outline)
        JButton registerBtn = new JButton("S'INSCRIRE");
        registerBtn.setBackground(Color.WHITE);
        registerBtn.setForeground(new Color(18, 18, 18));
        registerBtn.setFocusPainted(false);
        registerBtn.setFont(new Font("Helvetica Neue", Font.PLAIN, 11));
        registerBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(18, 18, 18), 1),
                new EmptyBorder(14, 40, 14, 40)
        ));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setOpaque(true);

        // Effet hover inscription
        registerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(new Color(18, 18, 18));
                registerBtn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(Color.WHITE);
                registerBtn.setForeground(new Color(18, 18, 18));
            }
        });

        JLabel message = new JLabel("");
        message.setForeground(new Color(180, 0, 0));
        message.setFont(new Font("Helvetica Neue", Font.PLAIN, 11));
        message.setBorder(new EmptyBorder(10, 0, 0, 0));

        // ===== Placement des composants =====
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        rightPanel.add(loginTitle, gbc);

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
        gbc.insets = new Insets(0, 0, 35, 0);
        rightPanel.add(password, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 15, 0);
        rightPanel.add(loginBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(registerBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        rightPanel.add(message, gbc);

        // ===== Actions =====
        loginBtn.addActionListener(e -> {
            String user = username.getText().trim();
            String pass = new String(password.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                message.setForeground(new Color(180, 0, 0));
                message.setText("Veuillez remplir tous les champs");
                return;
            }

            boolean ok = UserDAO.login(user, pass);

            if (ok) {
                new HomeFrame(user);
                dispose();
            } else {
                message.setForeground(new Color(180, 0, 0));
                message.setText("Identifiants incorrects");
            }
        });

        registerBtn.addActionListener(e -> {
            new RegisterFrame();
            dispose();
        });

        // Action Enter sur les champs
        password.addActionListener(e -> loginBtn.doClick());
        username.addActionListener(e -> password.requestFocus());

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}