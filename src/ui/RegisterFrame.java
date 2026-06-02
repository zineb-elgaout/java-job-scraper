package ui;

import dao.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class RegisterFrame extends JFrame {

    // ===== REGEX PATTERNS =====
   
    
    // Mot de passe: au moins 8 caractères, 1 chiffre, 1 lettre
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$"
    );

    public RegisterFrame() {

        Color PRIMARY = new Color(52, 152, 219);
        Color BORDER  = new Color(41, 128, 185);
        Color HOVER = new Color(31, 118, 175); // Couleur de survol
        Color SUCCESS = new Color(46, 204, 113); // Vert pour succès

        setTitle("Inscription - Job Scraper App");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(false);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(BORDER,2));
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
        leftPanel.setPreferredSize(new Dimension(280,0));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel app = new JLabel("JOB SCRAPER");
        app.setFont(new Font("Arial", Font.BOLD,28));
        app.setForeground(Color.WHITE);
        app.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Créer un compte");
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
        gbc.insets=new Insets(10,10,10,10);
        gbc.fill=GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Inscription");
        title.setFont(new Font("Arial", Font.BOLD,24));
        title.setForeground(BORDER);

        // ===== Champs initialisés (vides) =====
        JTextField username = new JTextField(15);
        JPasswordField password = new JPasswordField(15);
        JPasswordField confirm = new JPasswordField(15);

        JTextField[] fields = { username, password, confirm };
        for(JTextField f : fields){
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,2,0,BORDER),
                BorderFactory.createEmptyBorder(4,8,4,8)
            ));
        }

        // ===== Bouton Créer le compte - NOUVEAU STYLE =====
        JButton registerBtn = createStyledButton("Créer le compte", PRIMARY, HOVER);
        
        // ===== Phrase + lien sur la même ligne =====
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));
        loginPanel.setBackground(Color.WHITE);

        JLabel questionLabel = new JLabel("Vous avez déjà un compte ?");
        questionLabel.setForeground(Color.BLACK);
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel loginLink = new JLabel("Se connecter");
        loginLink.setForeground(BORDER);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.setFont(new Font("Arial", Font.PLAIN, 13));

        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginLink.setText("<html><u>Se connecter</u></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginLink.setText("Se connecter");
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SessionData.username = "";
                SessionData.password = "";
                new LoginFrame();
                dispose();
            }
        });

        loginPanel.add(questionLabel);
        loginPanel.add(loginLink);

        JLabel message = new JLabel("");
        message.setForeground(Color.RED);

        // ===== Placement =====
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2;
        rightPanel.add(title, gbc);

        gbc.gridy++; gbc.gridwidth=1;
        rightPanel.add(new JLabel("UserName :"), gbc);
        gbc.gridx=1;
        rightPanel.add(username, gbc);

        gbc.gridy++; gbc.gridx=0;
        rightPanel.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx=1;
        rightPanel.add(password, gbc);

        gbc.gridy++; gbc.gridx=0;
        rightPanel.add(new JLabel("Confirmer MDP :"), gbc);
        gbc.gridx=1;
        rightPanel.add(confirm, gbc);

        gbc.gridy++; gbc.gridx=0; gbc.gridwidth=2; gbc.anchor=GridBagConstraints.CENTER;
        rightPanel.add(registerBtn, gbc);

        gbc.gridy++;
        rightPanel.add(loginPanel, gbc);

        gbc.gridy++;
        rightPanel.add(message, gbc);

        // ===== Actions =====
        registerBtn.addActionListener(e -> {
            String UserName = username.getText().trim();
            String p = new String(password.getPassword());
            String c = new String(confirm.getPassword());

            // Validation des champs vides
            if(UserName.isEmpty() || p.isEmpty() || c.isEmpty()){
                message.setForeground(Color.RED);
                message.setText("Veuillez remplir tous les champs");
                return;
            }
            
            
            
            // Validation du mot de passe
            if(!PASSWORD_PATTERN.matcher(p).matches()) {
                message.setForeground(Color.RED);
                message.setText("Mot de passe: min 8 caractères, 1 chiffre, 1 lettre");
                return;
            }
            
            // Vérification de la correspondance des mots de passe
            if(!p.equals(c)){
                message.setForeground(Color.RED);
                message.setText("Les mots de passe ne correspondent pas");
                return;
            }

            // Tentative d'inscription
            if(UserDAO.register(UserName, p)){
                // Message de succès en vert
                message.setForeground(SUCCESS);
                message.setText("✓ Compte créé avec succès !");
                
                // Redirection vers LoginFrame après un délai
                Timer timer = new Timer(1500, ev -> {
                    SessionData.username = "";
                    SessionData.password = "";
                    new LoginFrame();
                    dispose();
                });
                timer.setRepeats(false);
                timer.start();
                
            } else {
                message.setForeground(Color.RED);
                message.setText("UserName déjà utilisé");
            }
        });

        mainPanel.add(rightPanel, BorderLayout.CENTER);
        setVisible(true);
    }
    
    // Méthode pour créer un bouton stylisé (même style que les autres)
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
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 35, 12, 35));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 45));
        
        return button;
    }
    
    // ===== MÉTHODES DE VALIDATION (optionnelles, pour réutilisation) =====
    
    
    public static boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}