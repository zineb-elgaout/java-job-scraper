package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.Map;

import ml.*;
import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.clusterers.Clusterer;

public class MLFrame extends JFrame {
    
    // Palette de couleurs moderne
    private final Color PRIMARY = new Color(99, 102, 241);
    private final Color PRIMARY_HOVER = new Color(79, 82, 221);
    private final Color BACKGROUND = new Color(249, 250, 251);
    private final Color SURFACE = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private final Color BORDER = new Color(229, 231, 235);
    private final Color SUCCESS = new Color(34, 197, 94);
    private final Color WARNING = new Color(251, 146, 60);
    private final Color ERROR = new Color(239, 68, 68);
    
    private JTextArea outputArea;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    
    // Composants ML disponibles
    private JobDataLoader dataLoader;
    private JobClassifier classifier;
    private JobClustering clustering;
    
    // Données chargées
    private Instances dataset;
    private Instances trainData;
    private Instances testData;
    private Classifier currentClassifier;
    
    public MLFrame() {
        dataLoader = new JobDataLoader();
        classifier = new JobClassifier();
        clustering = new JobClustering();
        initUI();
    }
    
    private void initUI() {
        setTitle("JobScraper ML");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);
        setContentPane(mainPanel);
        
        add(createHeader(), BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(280);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        splitPane.setBackground(BACKGROUND);
        
        splitPane.setLeftComponent(createSidebar());
        splitPane.setRightComponent(createContentPanel());
        
        add(splitPane, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(20, 30, 20, 30)
        ));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setBackground(SURFACE);
        
        JLabel iconLabel = new JLabel("⚡");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        
        JLabel titleLabel = new JLabel("Machine Learning");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        
        JButton closeButton = createModernButton("Fermer", false);
        closeButton.addActionListener(e -> dispose());
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SURFACE);
        sidebar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER),
            new EmptyBorder(25, 20, 20, 20)
        ));
        
        JLabel navLabel = new JLabel("NAVIGATION");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        navLabel.setForeground(TEXT_SECONDARY);
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(navLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        
        String[] menuItems = {
            "Charger données",
            "Classification",
            "Clustering",
            "Gestion modèles",
            "Tests complets"
        };
        
        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createSidebarButton(menuItems[i], i);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createInfoPanel());
        
        return sidebar;
    }
    
    private JButton createSidebarButton(String text, int index) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(SURFACE);
        button.setBorder(new EmptyBorder(12, 16, 12, 16));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(240, 44));
        button.setFocusPainted(false);
        
        button.addActionListener(e -> handleMenuAction(index));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(243, 244, 246));
                button.setForeground(PRIMARY);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SURFACE);
                button.setForeground(TEXT_PRIMARY);
            }
        });
        
        return button;
    }
    
    private void handleMenuAction(int index) {
        switch(index) {
            case 0: loadData(); break;
            case 1: showClassificationMenu(); break;
            case 2: showClusteringMenu(); break;
            case 3: showModels(); break;
            case 4: runAllTests(); break;
        }
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(239, 246, 255));
        panel.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(219, 234, 254), 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel infoLabel = new JLabel("À propos");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoLabel.setForeground(PRIMARY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea infoText = new JTextArea(
            "Analyse ML des offres d'emploi :\n" +
            "• Classification automatique\n" +
            "• Clustering des compétences\n" +
            "• Modèles prédictifs"
        );
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoText.setForeground(TEXT_SECONDARY);
        infoText.setBackground(new Color(239, 246, 255));
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);
        infoText.setEditable(false);
        infoText.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        panel.add(infoLabel);
        panel.add(infoText);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        outputArea = new JTextArea();
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setBackground(SURFACE);
        outputArea.setForeground(TEXT_PRIMARY);
        outputArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        
        showWelcomeMessage();
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(SURFACE);
        statusBar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
            new EmptyBorder(12, 30, 12, 30)
        ));
        
        statusLabel = new JLabel("Prêt");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(200, 6));
        progressBar.setVisible(false);
        progressBar.setForeground(PRIMARY);
        progressBar.setBorderPainted(false);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(progressBar, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private JButton createModernButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setBorder(new EmptyBorder(10, 24, 10, 24));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        
        if (isPrimary) {
            button.setBackground(PRIMARY);
            button.setForeground(Color.WHITE);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(PRIMARY_HOVER);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(PRIMARY);
                }
            });
        } else {
            button.setBackground(SURFACE);
            button.setForeground(TEXT_SECONDARY);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(9, 23, 9, 23)
            ));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(249, 250, 251));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(SURFACE);
                }
            });
        }
        
        return button;
    }
    
    private void updateStatus(String message, String type) {
        statusLabel.setText(message);
        
        switch(type) {
            case "success": statusLabel.setForeground(SUCCESS); break;
            case "warning": statusLabel.setForeground(WARNING); break;
            case "error": statusLabel.setForeground(ERROR); break;
            default: statusLabel.setForeground(TEXT_SECONDARY);
        }
    }
    
    private void appendOutput(String text) {
        outputArea.append(text + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
    
    private void clearOutput() {
        outputArea.setText("");
    }
    
    private void showWelcomeMessage() {
        clearOutput();
        appendOutput("╔═══════════════════════════════════════════════════════════╗");
        appendOutput("║           SYSTÈME MACHINE LEARNING                        ║");
        appendOutput("║           Analyse des Offres d'Emploi                     ║");
        appendOutput("╚═══════════════════════════════════════════════════════════╝");
        appendOutput("");
        appendOutput("Bienvenue ! Commencez par charger les données.");
        appendOutput("");
    }
    
    // ========== MÉTHODES ML RÉELLES ==========
    
    private void loadData() {
        clearOutput();
        appendOutput("═══ CHARGEMENT DES DONNÉES ═══\n");
        
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("→ Vérification du fichier de données...");
                    dataLoader.checkDataFile();
                    
                    publish("→ Chargement du dataset...");
                    dataset = dataLoader.loadDataset();
                    
                    publish("\n✓ DONNÉES CHARGÉES AVEC SUCCÈS !\n");
                    
                    // Afficher les statistiques RÉELLES
                    dataLoader.printDataStatistics(dataset);
                    
                    publish("\n→ Division des données (80% entraînement, 20% test)...");
                    Map<String, Instances> split = dataLoader.splitTrainTest(dataset, 80.0);
                    trainData = split.get("train");
                    testData = split.get("test");
                    
                    publish("\n✓ Données prêtes pour l'analyse ML !");
                    
                } catch (Exception e) {
                    publish("\n✗ ERREUR : " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    appendOutput(msg);
                }
            }
            
            @Override
            protected void done() {
                updateStatus("Données chargées", "success");
                progressBar.setVisible(false);
            }
        };
        
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        updateStatus("Chargement en cours...", "warning");
        worker.execute();
    }
    
    private void showClassificationMenu() {
        if (trainData == null) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez d'abord charger les données !", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String[] options = {
            "J48 (Arbre de décision)",
            "Naive Bayes",
            "Random Forest",
            "k-NN",
            "Réseau de neurones",
            "Vote (Ensemble)",
            "Comparer tous les algorithmes"
        };
        
        String choice = (String) JOptionPane.showInputDialog(
            this,
            "Choisissez un algorithme de classification :",
            "Classification",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice != null) {
            runClassification(choice);
        }
    }
    
    private void runClassification(String algorithmName) {
        clearOutput();
        appendOutput("═══ CLASSIFICATION : " + algorithmName + " ═══\n");
        
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Classifier clf = null;
                    
                    switch(algorithmName) {
                        case "J48 (Arbre de décision)":
                            clf = classifier.trainJ48(trainData);
                            break;
                        case "Naive Bayes":
                            clf = classifier.trainNaiveBayes(trainData);
                            break;
                        case "Random Forest":
                            clf = classifier.trainRandomForest(trainData);
                            break;
                        case "k-NN":
                            clf = classifier.trainKNN(trainData);
                            break;
                        case "Réseau de neurones":
                            clf = classifier.trainNeuralNetwork(trainData);
                            break;
                        case "Vote (Ensemble)":
                            clf = classifier.trainVotingClassifier(trainData);
                            break;
                        case "Comparer tous les algorithmes":
                            publish("\n→ Comparaison de tous les algorithmes...\n");
                            classifier.compareAlgorithms(trainData, testData);
                            return null;
                    }
                    
                    if (clf != null) {
                        currentClassifier = clf;
                        
                        publish("\n→ Évaluation du modèle...\n");
                        Evaluation eval = classifier.evaluateClassifier(clf, trainData, testData);
                        
                        publish("\n→ Détails de l'évaluation...\n");
                        classifier.printEvaluationDetails(eval, testData);
                        
                        // Validation croisée
                        publish("\n→ Validation croisée (10 folds)...\n");
                        classifier.crossValidate(clf, dataset, 10);
                        
                        publish("\n✓ Classification terminée !");
                        
                        // Proposer de sauvegarder le modèle
                        int save = JOptionPane.showConfirmDialog(MLFrame.this,
                            "Voulez-vous sauvegarder ce modèle ?",
                            "Sauvegarder",
                            JOptionPane.YES_NO_OPTION);
                            
                        if (save == JOptionPane.YES_OPTION) {
                            String modelName = JOptionPane.showInputDialog(MLFrame.this,
                                "Nom du modèle :",
                                algorithmName.replaceAll("[^a-zA-Z0-9]", "_") + ".model");
                            if (modelName != null && !modelName.isEmpty()) {
                                classifier.saveModel(clf, modelName);
                                publish("✓ Modèle sauvegardé : " + modelName);
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    publish("\n✗ ERREUR : " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    appendOutput(msg);
                }
            }
            
            @Override
            protected void done() {
                updateStatus("Classification terminée", "success");
                progressBar.setVisible(false);
            }
        };
        
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        updateStatus("Classification en cours...", "warning");
        worker.execute();
    }
    
    private void showClusteringMenu() {
        if (dataset == null) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez d'abord charger les données !", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String[] options = {
            "K-Means (k=3)",
            "K-Means (k=5)",
            "EM Clustering",
            "Clustering Hiérarchique",
            "Trouver k optimal",
            "Comparer les algorithmes"
        };
        
        String choice = (String) JOptionPane.showInputDialog(
            this,
            "Choisissez une méthode de clustering :",
            "Clustering",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        if (choice != null) {
            runClustering(choice);
        }
    }
    
    private void runClustering(String method) {
        clearOutput();
        appendOutput("═══ CLUSTERING : " + method + " ═══\n");
        
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Clusterer clust = null;
                    
                    switch(method) {
                        case "K-Means (k=3)":
                            clust = clustering.applyKMeans(dataset, 3);
                            clustering.analyzeClusters(clust, dataset);
                            clustering.visualizeClusters(clust, dataset);
                            break;
                        case "K-Means (k=5)":
                            clust = clustering.applyKMeans(dataset, 5);
                            clustering.analyzeClusters(clust, dataset);
                            clustering.visualizeClusters(clust, dataset);
                            break;
                        case "EM Clustering":
                            clust = clustering.applyEM(dataset, 5);
                            clustering.analyzeClusters(clust, dataset);
                            break;
                        case "Clustering Hiérarchique":
                            clust = clustering.applyHierarchicalClustering(dataset, 4);
                            clustering.analyzeClusters(clust, dataset);
                            break;
                        case "Trouver k optimal":
                            clustering.findOptimalK(dataset, 10);
                            break;
                        case "Comparer les algorithmes":
                            clustering.compareClusteringAlgorithms(dataset);
                            break;
                    }
                    
                    publish("\n✓ Clustering terminé !");
                    
                } catch (Exception e) {
                    publish("\n✗ ERREUR : " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    appendOutput(msg);
                }
            }
            
            @Override
            protected void done() {
                updateStatus("Clustering terminé", "success");
                progressBar.setVisible(false);
            }
        };
        
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        updateStatus("Clustering en cours...", "warning");
        worker.execute();
    }
    
    private void showModels() {
        clearOutput();
        appendOutput("═══ GESTION DES MODÈLES ═══\n");
        
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                File dir = new File("models");
                
                if (dir.exists() && dir.listFiles() != null) {
                    File[] files = dir.listFiles((d, name) -> name.endsWith(".model"));
                    
                    if (files != null && files.length > 0) {
                        publish("📁 MODÈLES DISPONIBLES (" + files.length + ") :\n");
                        publish("═".repeat(70));
                        
                        java.util.Arrays.sort(files, 
                            (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                        
                        for (File f : files) {
                            double kb = f.length() / 1024.0;
                            String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                                .format(new java.util.Date(f.lastModified()));
                            publish(String.format("%-40s  %8.1f KB  %s", 
                                f.getName(), kb, date));
                        }
                        
                        publish("\n💡 Pour utiliser un modèle, chargez-le depuis le code.");
                    } else {
                        publish("Aucun modèle disponible dans le dossier 'models'.");
                        publish("\nLes modèles entraînés peuvent être sauvegardés après la classification.");
                    }
                } else {
                    publish("Dossier 'models' introuvable.");
                    publish("Créez le dossier 'models' à la racine du projet.");
                    publish("\nLes modèles entraînés y seront sauvegardés automatiquement.");
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    appendOutput(msg);
                }
            }
        };
        
        worker.execute();
        updateStatus("Liste des modèles", "success");
    }
    
    private void runAllTests() {
        if (dataset == null) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez d'abord charger les données !", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        clearOutput();
        appendOutput("═══ TESTS COMPLETS DU SYSTÈME ML ═══\n");
        
        int response = JOptionPane.showConfirmDialog(this,
            "Exécuter tous les tests ML ?\nCette opération peut prendre plusieurs minutes.",
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (response != JOptionPane.YES_OPTION) return;
        
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("═".repeat(70));
                    publish("               SUITE DE TESTS COMPLÈTE");
                    publish("═".repeat(70));
                    
                    // Test 1: Classification
                    publish("\n[1/2] TESTS DE CLASSIFICATION...\n");
                    publish("-".repeat(70));
                    classifier.compareAlgorithms(trainData, testData);
                    
                    // Test 2: Clustering
                    publish("\n[2/2] TESTS DE CLUSTERING...\n");
                    publish("-".repeat(70));
                    clustering.findOptimalK(dataset, 8);
                    Clusterer kmeans = clustering.applyKMeans(dataset, 3);
                    clustering.analyzeClusters(kmeans, dataset);
                    
                    publish("\n" + "═".repeat(70));
                    publish("✓ TOUS LES TESTS TERMINÉS AVEC SUCCÈS !");
                    publish("═".repeat(70));
                    
                } catch (Exception e) {
                    publish("\n✗ Erreur pendant les tests : " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    appendOutput(msg);
                }
            }
            
            @Override
            protected void done() {
                updateStatus("Tests terminés", "success");
                progressBar.setVisible(false);
            }
        };
        
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        updateStatus("Tests en cours...", "warning");
        worker.execute();
    }
}