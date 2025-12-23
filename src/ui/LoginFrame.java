package ui;

import java.awt.*;
import javax.swing.*;
import dao.UserDAO;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("Job Scraper App");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 2));
        add(mainPanel);

        // ===== PANEL GAUCHE (dégradé) =====
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(41, 128, 185);
                Color color2 = new Color(52, 152, 219);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel appName = new JLabel("JOB SCRAPER");
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("Arial", Font.BOLD, 28));
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Analyse des offres d'emploi");
        subtitle.setForeground(Color.WHITE);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(appName);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(subtitle);
        leftPanel.add(Box.createVerticalGlue());

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // ===== PANEL DROIT (formulaire) =====
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel loginTitle = new JLabel("Connexion");
        loginTitle.setFont(new Font("Arial", Font.BOLD, 24));
        loginTitle.setForeground(new Color(41, 128, 185));

        JTextField username = new JTextField(15);
        username.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(41, 128, 185)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JPasswordField password = new JPasswordField(15);
        password.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(41, 128, 185)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton loginBtn = new JButton("Se connecter");
        loginBtn.setBackground(new Color(41, 128, 185));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel message = new JLabel("");
        message.setForeground(Color.RED);

        // ===== Placement =====
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        rightPanel.add(loginTitle, gbc);

        gbc.gridy++; gbc.gridwidth = 1; gbc.gridx = 0;
        rightPanel.add(new JLabel("Nom d'utilisateur :"), gbc);
        gbc.gridx = 1;
        rightPanel.add(username, gbc);

        gbc.gridy++; gbc.gridx = 0;
        rightPanel.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1;
        rightPanel.add(password, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(loginBtn, gbc);

        JButton registerBtn = new JButton("S'inscrire");
        registerBtn.setBackground(new Color(52, 152, 219));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setFont(new Font("Arial", Font.BOLD, 14));
        registerBtn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridy++;
        rightPanel.add(registerBtn, gbc);

        gbc.gridy++;
        rightPanel.add(message, gbc);

        // ===== Actions =====
        loginBtn.addActionListener(e -> {
            String user = username.getText();
            String pass = new String(password.getPassword());

            boolean ok = UserDAO.login(user, pass);

            if (ok) {
                new HomeFrame(user); // ouvrir accueil
                dispose();           // fermer login
            } else {
                message.setForeground(Color.RED);
                message.setText("Identifiants incorrects");
            }
        });


        registerBtn.addActionListener(e -> {
            new RegisterFrame();
            dispose();
        });

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}