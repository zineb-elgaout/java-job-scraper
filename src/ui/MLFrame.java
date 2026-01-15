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

public class MLFrame extends JFrame {

    private final Color PRIMARY = new Color(52, 152, 219);
    private final Color BACKGROUND = new Color(248, 249, 252);
    private final Color SURFACE = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private final Color TEXT_SECONDARY = new Color(127, 140, 141);
    private final Color BORDER = new Color(230, 233, 238);
    private final Color SUCCESS = new Color(46, 204, 113);
    private final Color ERROR = new Color(231, 76, 60);

    private JTextArea outputArea;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    // Composants ML
    private JobDataLoader dataLoader;
    private JobClassifier classifier;
    private ModelEvaluator evaluator;
    private RecommendationService RecommendationService;

    // Données
    private Instances dataset;
    private Instances trainData;
    private Instances testData;
    private Classifier currentModel;

    public MLFrame() {
        dataLoader = new JobDataLoader();
        classifier = new JobClassifier();
        evaluator = new ModelEvaluator();
        RecommendationService = new RecommendationService();
        initUI();
    }

    private void initUI() {
        setTitle("JobScraper - Machine Learning");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);
        setContentPane(mainPanel);

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Contenu principal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(1);

        splitPane.setLeftComponent(createSidebar());
        splitPane.setRightComponent(createContentPanel());

        add(splitPane, BorderLayout.CENTER);

        // Status bar
        add(createStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel titleLabel = new JLabel("Machine Learning");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY);

        JButton closeButton = new JButton("Fermer");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.addActionListener(e -> dispose());

        header.add(titleLabel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        String[] menuItems = {
                " Charger données",
                " Classification",
                " Modèles",
                " Recommandations"
        };

        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createSidebarButton(menuItems[i], i);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        sidebar.add(Box.createVerticalGlue());

        // Info simple
        JLabel info = new JLabel("<html><small>ML pour offres d'emploi</small></html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        info.setForeground(TEXT_SECONDARY);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(info);

        return sidebar;
    }

    private JButton createSidebarButton(String text, int index) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(SURFACE);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(180, 40));

        button.addActionListener(e -> handleMenuAction(index));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(240, 245, 255));
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
        switch (index) {
            case 0:
                loadData();
                break;
            case 1:
                showClassification();
                break;
            case 2:
                showModels();
                break;
            case 3:
                showRecommendations();
                break;
        }
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(250, 250, 250));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        showWelcomeMessage();

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(SURFACE);
        statusBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(8, 15, 8, 15)));

        statusLabel = new JLabel("Prêt");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_SECONDARY);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(150, 16));
        progressBar.setVisible(false);

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(progressBar, BorderLayout.EAST);

        return statusBar;
    }

    private void updateStatus(String message, String type) {
        statusLabel.setText(message);
        statusLabel.setForeground(type.equals("error") ? ERROR : type.equals("success") ? SUCCESS : TEXT_SECONDARY);
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
        appendOutput("=== SYSTÈME MACHINE LEARNING ===");
        appendOutput("Analyse des offres d'emploi");
        appendOutput("");
        appendOutput("Options disponibles :");
        appendOutput("1. Charger les données");
        appendOutput("2. Classification");
        appendOutput("3. Gérer les modèles");
        appendOutput("4. Générer des recommandations");
        appendOutput("");
    }

    // ========== MÉTHODES ML ==========

    private void loadData() {
        clearOutput();
        appendOutput("Chargement des données...\n");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Vérification du fichier...");
                    dataLoader.checkDataFile();

                    publish("Chargement du dataset...");
                    dataset = dataLoader.loadDataset();

                    publish("Division des données (80/20)...");
                    Map<String, Instances> split = dataLoader.splitTrainTest(dataset, 80.0);
                    trainData = split.get("train");
                    testData = split.get("test");

                    publish(" DONNÉES CHARGÉES !");
                    publish("Total : " + dataset.numInstances() + " instances");
                    publish("Entraînement : " + trainData.numInstances() + " instances");
                    publish("Test : " + testData.numInstances() + " instances");

                } catch (Exception e) {
                    publish(" Erreur : " + e.getMessage());
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
        updateStatus("Chargement...", "");
        worker.execute();
    }

    private void showClassification() {
        if (trainData == null) {
            JOptionPane.showMessageDialog(this, "Veuillez d'abord charger les données !",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        clearOutput();
        appendOutput("=== CLASSIFICATION ===\n");

        String[] options = { "J48", "Naive Bayes", "Random Forest", "k-NN", "Comparer tous", "Évaluer modèle" };
        String choice = (String) JOptionPane.showInputDialog(this,
                "Choisissez une option :", "Classification",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice != null) {
            if (choice.equals("Évaluer modèle")) {
                evaluateCurrentModel();
            } else {
                runClassification(choice);
            }
        }
    }

    private void runClassification(String algorithm) {
        clearOutput();
        appendOutput("Classification avec " + algorithm + "...\n");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if (algorithm.equals("Comparer tous")) {
                        publish("Comparaison des algorithmes...\n");
                        classifier.compareAlgorithms(trainData, testData);
                    } else {
                        Classifier clf = null;

                        switch (algorithm) {
                            case "J48":
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
                        }

                        if (clf != null) {
                            currentModel = clf;
                            publish("Évaluation du modèle...\n");
                            Evaluation eval = classifier.evaluateClassifier(clf, trainData, testData);

                            publish("\nRÉSULTATS :");
                            publish("Précision : " + String.format("%.2f", eval.pctCorrect()) + "%");
                            publish("Kappa : " + String.format("%.3f", eval.kappa()));
                            publish("AUC moyen : " + String.format("%.3f", eval.weightedAreaUnderROC()));

                            // Affichage simplifié de la matrice de confusion
                            double[][] matrix = eval.confusionMatrix();
                            if (matrix.length <= 5) {
                                publish("\nMatrice de confusion :");
                                for (double[] row : matrix) {
                                    StringBuilder sb = new StringBuilder();
                                    for (double val : row) {
                                        sb.append(String.format("%5.0f ", val));
                                    }
                                    publish(sb.toString());
                                }
                            }

                            // Validation croisée
                            publish("\nValidation croisée (10 folds)...");
                            classifier.crossValidate(clf, dataset, 10);

                            // Sauvegarde automatique
                            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                                    .format(new java.util.Date());
                            String filename = "models/" + algorithm.toLowerCase() + "_" + timestamp + ".model";

                            File modelsDir = new File("models");
                            if (!modelsDir.exists()) {
                                modelsDir.mkdirs();
                            }

                            classifier.saveModel(clf, filename);
                            publish("\n Modèle sauvegardé : " + filename);
                        }
                    }

                    publish("\n Classification terminée !");

                } catch (Exception e) {
                    publish(" Erreur : " + e.getMessage());
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
        updateStatus("Traitement...", "");
        worker.execute();
    }

    private void evaluateCurrentModel() {
        if (currentModel == null) {
            JOptionPane.showMessageDialog(this, "Aucun modèle chargé. Entraînez d'abord un modèle.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        clearOutput();
        appendOutput("Évaluation du modèle courant...\n");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Évaluation sur l'ensemble de test...\n");
                    Evaluation eval = new Evaluation(testData);
                    eval.evaluateModel(currentModel, testData);

                    publish("PERFORMANCES :");
                    publish("Précision : " + String.format("%.2f", eval.pctCorrect()) + "%");
                    publish("Kappa : " + String.format("%.3f", eval.kappa()));
                    publish("AUC moyen : " + String.format("%.3f", eval.weightedAreaUnderROC()));

                    // Évaluation détaillée
                    publish("\n=== ÉVALUATION DÉTAILLÉE ===");
                    evaluator.evaluateClassificationModel(currentModel, trainData, testData);

                } catch (Exception e) {
                    publish(" Erreur : " + e.getMessage());
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
                updateStatus("Évaluation terminée", "success");
                progressBar.setVisible(false);
            }
        };

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        updateStatus("Évaluation...", "");
        worker.execute();
    }

    private void showModels() {
        clearOutput();
        appendOutput("=== GESTION DES MODÈLES ===\n");

        String[] options = { "Lister les modèles", "Charger un modèle", "Sauvegarder modèle courant" };
        String choice = (String) JOptionPane.showInputDialog(this,
                "Choisissez une action :", "Gestion des modèles",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice != null) {
            switch (choice) {
                case "Lister les modèles":
                    listModels();
                    break;
                case "Charger un modèle":
                    loadModel();
                    break;
                case "Sauvegarder modèle courant":
                    saveCurrentModel();
                    break;
            }
        }
    }

    private void listModels() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                File modelsDir = new File("models");

                if (modelsDir.exists() && modelsDir.listFiles() != null) {
                    File[] files = modelsDir.listFiles((dir, name) -> name.endsWith(".model"));

                    if (files != null && files.length > 0) {
                        publish(" MODÈLES DISPONIBLES (" + files.length + ") :\n");

                        // Trier par date
                        java.util.Arrays.sort(files,
                                (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                        for (File f : files) {
                            double kb = f.length() / 1024.0;
                            String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                                    .format(new java.util.Date(f.lastModified()));
                            publish(String.format("• %-35s  %6.1f KB  %s",
                                    f.getName(), kb, date));
                        }
                    } else {
                        publish("Aucun modèle trouvé.");
                        publish("Les modèles seront sauvegardés dans le dossier 'models'.");
                    }
                } else {
                    publish("Dossier 'models' non trouvé.");
                    publish("Créez le dossier pour sauvegarder vos modèles.");
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
        updateStatus("Modèles listés", "success");
    }

    private void loadModel() {
        File modelsDir = new File("models");
        if (!modelsDir.exists() || modelsDir.listFiles() == null) {
            JOptionPane.showMessageDialog(this, "Dossier 'models' non trouvé ou vide.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File[] files = modelsDir.listFiles((dir, name) -> name.endsWith(".model"));
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "Aucun modèle disponible.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] modelNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            modelNames[i] = files[i].getName();
        }

        String selected = (String) JOptionPane.showInputDialog(this,
                "Choisissez un modèle à charger :", "Charger modèle",
                JOptionPane.QUESTION_MESSAGE, null, modelNames, modelNames[0]);

        if (selected != null) {
            clearOutput();
            appendOutput("Chargement du modèle " + selected + "...\n");

            SwingWorker<Void, String> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        String modelPath = "models/" + selected;
                        currentModel = classifier.loadModel(modelPath);
                        publish(" Modèle chargé : " + selected);
                        publish("Type : " + currentModel.getClass().getSimpleName());

                        // Évaluer le modèle si les données sont chargées
                        if (testData != null) {
                            publish("\nÉvaluation sur l'ensemble de test...");
                            Evaluation eval = new Evaluation(testData);
                            eval.evaluateModel(currentModel, testData);
                            publish("Précision : " + String.format("%.2f", eval.pctCorrect()) + "%");
                        }

                    } catch (Exception e) {
                        publish(" Erreur : " + e.getMessage());
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
                    updateStatus("Modèle chargé", "success");
                }
            };

            worker.execute();
        }
    }

    private void saveCurrentModel() {
        if (currentModel == null) {
            JOptionPane.showMessageDialog(this, "Aucun modèle courant à sauvegarder.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = JOptionPane.showInputDialog(this,
                "Nom du modèle (sans extension) :", "model_personnalise");

        if (name != null && !name.trim().isEmpty()) {
            try {
                String filename = "models/" + name + ".model";
                classifier.saveModel(currentModel, filename);
                JOptionPane.showMessageDialog(this,
                        "Modèle sauvegardé : " + filename,
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la sauvegarde : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRecommendations() {
        clearOutput();
        appendOutput("=== GÉNÉRATION DE RECOMMANDATIONS ===\n");

        generateRecommendations();
    }

    private void generateRecommendations() {
        String[] categories = { "IT_DEVELOPPEMENT", "COMMERCIAL_VENTE",
                "RESSOURCES_HUMAINES", "DATA_IA", "MARKETING" };

        String selected = (String) JOptionPane.showInputDialog(this,
                "Choisissez une catégorie :", "Recommandations",
                JOptionPane.QUESTION_MESSAGE, null, categories, categories[0]);

        if (selected != null) {
            clearOutput();
            appendOutput("Génération de recommandations pour : " + selected + "\n");

            SwingWorker<Void, String> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        Map<String, Object> reco = RecommendationService.generateRecommendations(selected);

                        appendOutput("📋 CONSEILS PROFESSIONNELS :");
                        List<String> conseils = (List<String>) reco.get("conseils");
                        if (conseils != null) {
                            for (String c : conseils) {
                                appendOutput("  • " + c);
                            }
                        }

                        appendOutput("\n🎓 FORMATIONS SUGGÉRÉES :");
                        List<String> formations = (List<String>) reco.get("formations_suggerees");
                        if (formations != null) {
                            for (String f : formations) {
                                appendOutput("  • " + f);
                            }
                        }

                        appendOutput("\n💼 COMPÉTENCES CLÉS :");
                        List<String> competences = (List<String>) reco.get("competences_cles");
                        if (competences != null) {
                            for (String comp : competences) {
                                appendOutput("  • " + comp);
                            }
                        }

                        appendOutput("\n Recommandations générées !");

                    } catch (Exception e) {
                        appendOutput(" Erreur : " + e.getMessage());
                    }
                    return null;
                }

                @Override
                protected void done() {
                    updateStatus("Recommandations générées", "success");
                }
            };

            worker.execute();
        }
    }
}