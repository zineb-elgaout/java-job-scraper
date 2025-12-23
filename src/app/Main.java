package app;

import ui.LoginFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // Lancer l'interface graphique proprement
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}
