package ui;

import dao.UserDAO;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    public LoginFrame() {

        Color PRIMARY = new Color(52, 152, 219);
        Color BORDER  = new Color(41, 128, 185);
        Color HOVER = new Color(31, 118, 175); // Couleur de survol

        setTitle("Job Scraper App");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(BORDER, 2));
        add(mainPanel);

        // ===== PANEL GAUCHE =====
        JPanel leftPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,BORDER,0,getHeight(),PRIMARY));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel app = new JLabel("JOB SCRAPER");
        app.setFont(new Font("Arial", Font.BOLD, 28));
        app.setForeground(Color.WHITE);
        app.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Analyse des offres d'emploi");
        sub.setForeground(Color.WHITE);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(app);
        leftPanel.add(Box.createRigidArea(new Dimension(0,10)));
        leftPanel.add(sub);
        leftPanel.add(Box.createVerticalGlue());
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // ===== PANEL DROIT =====
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Connexion");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(BORDER);

        // ===== Champs texte (initialement vides) =====
        JTextField username = new JTextField(15);
        JPasswordField password = new JPasswordField(15);

        JTextField[] fields = { username, password };
        for (JTextField f : fields) {
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,2,0,BORDER),
                BorderFactory.createEmptyBorder(4,8,4,8)
            ));
        }

        // ===== Bouton Se connecter - NOUVEAU STYLE =====
        JButton loginBtn = createStyledButton("Se connecter", PRIMARY, HOVER);
        
        // ===== Phrase + lien sur la même ligne =====
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));
        registerPanel.setBackground(Color.WHITE);

        JLabel questionLabel = new JLabel("Vous n'avez pas de compte ?");
        questionLabel.setForeground(Color.BLACK);
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel registerLink = new JLabel("Créer un compte");
        registerLink.setForeground(BORDER);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.setFont(new Font("Arial", Font.PLAIN, 13));

        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerLink.setText("<html><u>Créer un compte</u></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerLink.setText("Créer un compte");
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Vide les champs avant d'ouvrir Register
                SessionData.username = "";
                SessionData.password = "";
                new RegisterFrame();
                dispose();
            }
        });

        registerPanel.add(questionLabel);
        registerPanel.add(registerLink);

        JLabel message = new JLabel("");
        message.setForeground(Color.RED);

        // ===== Placement =====
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        rightPanel.add(title, gbc);

        gbc.gridy++; gbc.gridwidth=1;
        rightPanel.add(new JLabel("Nom d'utilisateur :"), gbc);
        gbc.gridx=1;
        rightPanel.add(username, gbc);

        gbc.gridy++; gbc.gridx=0;
        rightPanel.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx=1;
        rightPanel.add(password, gbc);

        gbc.gridy++; gbc.gridx=0; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.CENTER;
        rightPanel.add(loginBtn, gbc);

        gbc.gridy++;
        rightPanel.add(registerPanel, gbc);

        gbc.gridy++;
        rightPanel.add(message, gbc);

        // ===== Actions =====
        loginBtn.addActionListener(e -> {
            if(UserDAO.login(username.getText(), new String(password.getPassword()))) {
                message.setForeground(new Color(39,174,96));
               
                
                // Ouvrir HomeFrame après un court délai
                Timer timer = new Timer(500, ev -> {
                    new HomeFrame(username.getText());
                    dispose();
                });
                timer.setRepeats(false);
                timer.start();
                
            } else {
                message.setForeground(Color.RED);
                message.setText("Identifiants incorrects");
            }
        });

        mainPanel.add(rightPanel, BorderLayout.CENTER);
        setVisible(true);
    }
    
    // Méthode pour créer un bouton stylisé (même style que la fenêtre de déconnexion)
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
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
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Police similaire
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 35, 12, 35)); // Padding similaire
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 45)); // Taille similaire
        
        return button;
    }
}