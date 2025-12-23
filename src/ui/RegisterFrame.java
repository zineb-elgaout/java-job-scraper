package ui;

import java.awt.*;
import javax.swing.*;
import dao.UserDAO;

public class RegisterFrame extends JFrame {

    public RegisterFrame() {
        setTitle("Inscription - Job Scraper App");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Créer un compte");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(41, 128, 185));

        JTextField username = new JTextField(15);
        JPasswordField password = new JPasswordField(15);
        JPasswordField confirmPassword = new JPasswordField(15);

        JButton registerBtn = new JButton("S'inscrire");
        JLabel message = new JLabel("");
        message.setForeground(Color.RED);

        // Placement
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(title, gbc);

        gbc.gridy++; gbc.gridwidth = 1; gbc.gridx = 0;
        add(new JLabel("Nom d'utilisateur :"), gbc);
        gbc.gridx = 1;
        add(username, gbc);

        gbc.gridy++; gbc.gridx = 0;
        add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1;
        add(password, gbc);

        gbc.gridy++; gbc.gridx = 0;
        add(new JLabel("Confirmer mot de passe :"), gbc);
        gbc.gridx = 1;
        add(confirmPassword, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(registerBtn, gbc);

        gbc.gridy++;
        add(message, gbc);

        // Action
        registerBtn.addActionListener(e -> {
            String user = username.getText();
            String pass = new String(password.getPassword());
            String confirm = new String(confirmPassword.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                message.setForeground(Color.RED);
                message.setText("Veuillez remplir tous les champs");
                return;
            }

            if (!pass.equals(confirm)) {
                message.setForeground(Color.RED);
                message.setText("Les mots de passe ne correspondent pas");
                return;
            }

            boolean success = UserDAO.register(user, pass);
            if (success) {
                message.setForeground(new Color(39, 174, 96));
                message.setText("Inscription réussie ! Vous pouvez vous connecter.");
            } else {
                message.setForeground(Color.RED);
                message.setText("Erreur : Nom d'utilisateur déjà utilisé");
            }
        });

        setVisible(true);
    }
}