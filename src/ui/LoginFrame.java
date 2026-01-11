package ui;

import dao.UserDAO;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    
    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color HOVER = new Color(79, 82, 221);
    private static final Color TEXT = new Color(55, 65, 81);
    private static final Color TEXT_LIGHT = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);

    public LoginFrame() {
        setTitle("Job Scraper - Connexion");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.add(createLeftPanel(), BorderLayout.WEST);
        content.add(createFormPanel(), BorderLayout.CENTER);
        
        add(content);
        setVisible(true);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(249, 250, 251));
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(249, 250, 251));
        textPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Job Scraper");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Analyse intelligente des offres d'emploi");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(TEXT_LIGHT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));

        JTextArea description = new JTextArea(
            "• Scraping automatique des offres\n\n" +
            "• Analyse ML et classification\n\n" +
            "• Visualisations interactives\n\n" +
            "• Export de données et rapports"
        );
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setForeground(TEXT_LIGHT);
        description.setBackground(new Color(249, 250, 251));
        description.setEditable(false);
        description.setFocusable(false);
        description.setBorder(null);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(title);
        textPanel.add(subtitle);
        textPanel.add(description);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(textPanel, gbc);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel heading = new JLabel("Connexion");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(TEXT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(heading, gbc);

        JLabel subheading = new JLabel("Connectez-vous à votre compte");
        subheading.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subheading.setForeground(TEXT_LIGHT);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 35, 0);
        panel.add(subheading, gbc);

        JLabel userLabel = new JLabel("Nom d'utilisateur");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(TEXT);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(userLabel, gbc);

        JTextField userField = createTextField();
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(userField, gbc);

        JLabel passLabel = new JLabel("Mot de passe");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLabel.setForeground(TEXT);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(passLabel, gbc);

        JPasswordField passField = createPasswordField();
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 25, 0);
        panel.add(passField, gbc);

        JLabel message = new JLabel("");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        message.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(message, gbc);

        JButton loginBtn = createButton("Se connecter");
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 25, 0);
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                message.setForeground(new Color(239, 68, 68));
                message.setText("Veuillez remplir tous les champs");
                return;
            }
            
            if (UserDAO.login(username, password)) {
                message.setForeground(new Color(34, 197, 94));
                message.setText("Connexion réussie");
                Timer timer = new Timer(400, ev -> {
                    new HomeFrame(username);
                    dispose();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                message.setForeground(new Color(239, 68, 68));
                message.setText("✗ Identifiants incorrects");
            }
        });

        // Touche Entrée pour se connecter
        java.awt.event.KeyAdapter enterListener = new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    loginBtn.doClick();
                }
            }
        };
        userField.addKeyListener(enterListener);
        passField.addKeyListener(enterListener);

        JPanel register = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        register.setBackground(Color.WHITE);
        
        JLabel text = new JLabel("Pas encore de compte ?");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(TEXT_LIGHT);
        
        JLabel link = new JLabel("S'inscrire");
        link.setFont(new Font("Segoe UI", Font.BOLD, 13));
        link.setForeground(PRIMARY);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                link.setText("<html><u>S'inscrire</u></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                link.setText("S'inscrire");
            }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new RegisterFrame();
                dispose();
            }
        });

        register.add(text);
        register.add(link);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(register, gbc);

        return panel;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(22);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(11, 14, 11, 14)
        ));
        
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 2, true),
                    BorderFactory.createEmptyBorder(10, 13, 10, 13)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(11, 14, 11, 14)
                ));
            }
        });
        
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(22);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(11, 14, 11, 14)
        ));
        
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 2, true),
                    BorderFactory.createEmptyBorder(10, 13, 10, 13)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(11, 14, 11, 14)
                ));
            }
        });
        
        return field;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(HOVER);
                } else {
                    g2.setColor(PRIMARY);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        return btn;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame();
        });
    }
}