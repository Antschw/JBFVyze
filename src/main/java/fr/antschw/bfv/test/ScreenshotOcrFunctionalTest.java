package fr.antschw.bfv.test;

import fr.antschw.bfv.domain.model.ServerInfo;
import fr.antschw.bfv.domain.service.HotkeyConfigurationService;
import fr.antschw.bfv.infrastructure.api.client.BfvHackersApiClient;
import fr.antschw.bfv.infrastructure.api.client.GameToolsApiClient;
import fr.antschw.bfv.infrastructure.hotkey.InMemoryHotkeyConfigurationAdapter;
import fr.antschw.bfv.infrastructure.screenshot.AwtRobotScreenshotAdapter;
import fr.antschw.bfvocr.api.BFVOcrFactory;
import fr.antschw.bfvocr.api.BFVOcrService;
import fr.antschw.bfvocr.exceptions.BFVOcrException;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced functional test for complete BFV server scanning workflow:
 * 1. Hotkey-triggered screenshot capture
 * 2. OCR processing using BFVOcr library
 * 3. GameTools API call to get long server ID
 * 4. BFVHackers API call to get cheater count
 * <p>
 * Uses JNativeHook to capture F9 keypress even when the window is not in focus.
 */
public class ScreenshotOcrFunctionalTest implements NativeKeyListener {

    // UI Components
    private final JFrame frame;
    private final JLabel statusLabel;
    private final JLabel ocrResultLabel;
    private final JLabel gameToolsResultLabel;
    private final JLabel hackersResultLabel;
    private final JLabel timeLabel;
    private final JPanel resultPanel;
    private final JProgressBar progressBar;
    private final JButton configButton;
    private final JPanel scanHistoryPanel;
    private final JScrollPane historyScrollPane;

    // Services
    private final BFVOcrService ocrService;
    private final AwtRobotScreenshotAdapter screenshotAdapter;
    private final GameToolsApiClient gameToolsApiClient;
    private final BfvHackersApiClient bfvHackersApiClient;
    private final HotkeyConfigurationService hotkeyConfigService;
    private final ExecutorService executorService;

    // Constants
    private static final String DEFAULT_HOTKEY = "F9";
    private static final String APP_TITLE = "BFV Server Scanner";
    private static final String SCAN_READY_TEXT = "Appuyez sur %s pour lancer un scan (fonctionne depuis n'importe quelle fenêtre)";
    private static final String SCAN_RUNNING_TEXT = "Scan en cours...";
    private static final Color BG_COLOR = new Color(44, 51, 73);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SUCCESS_COLOR = new Color(76, 187, 23);
    private static final Color ERROR_COLOR = new Color(255, 89, 89);

    /**
     * Constructor to set up the application.
     */
    public ScreenshotOcrFunctionalTest() throws AWTException {
        // Set up UI with dark theme look and feel
        setupLookAndFeel();

        // Create main frame
        frame = new JFrame(APP_TITLE);
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(BG_COLOR);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

        // Status panel on top
        JPanel statusPanel = new JPanel(new BorderLayout(10, 10));
        statusPanel.setBackground(BG_COLOR);
        statusPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        statusLabel = createStyledLabel(String.format(SCAN_READY_TEXT, DEFAULT_HOTKEY), JLabel.CENTER);
        statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.BOLD, 14));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        configButton = new JButton("Configurer Hotkey");
        configButton.setFocusPainted(false);
        configButton.addActionListener(e -> configureHotkey());
        statusPanel.add(configButton, BorderLayout.EAST);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        statusPanel.add(progressBar, BorderLayout.SOUTH);

        frame.add(statusPanel, BorderLayout.NORTH);

        // Results panel in center
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(BG_COLOR);
        resultPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        ocrResultLabel = createStyledLabel("OCR: En attente de scan", JLabel.LEFT);
        gameToolsResultLabel = createStyledLabel("GameTools: En attente de scan", JLabel.LEFT);
        hackersResultLabel = createStyledLabel("BFVHackers: En attente de scan", JLabel.LEFT);
        timeLabel = createStyledLabel("Temps d'exécution: --", JLabel.LEFT);

        resultPanel.add(createSeparator("Résultats du dernier scan"));
        resultPanel.add(Box.createVerticalStrut(10));
        resultPanel.add(ocrResultLabel);
        resultPanel.add(Box.createVerticalStrut(5));
        resultPanel.add(gameToolsResultLabel);
        resultPanel.add(Box.createVerticalStrut(5));
        resultPanel.add(hackersResultLabel);
        resultPanel.add(Box.createVerticalStrut(5));
        resultPanel.add(timeLabel);

        frame.add(resultPanel, BorderLayout.CENTER);

        // History panel at bottom
        scanHistoryPanel = new JPanel();
        scanHistoryPanel.setLayout(new BoxLayout(scanHistoryPanel, BoxLayout.Y_AXIS));
        scanHistoryPanel.setBackground(BG_COLOR);

        historyScrollPane = new JScrollPane(scanHistoryPanel);
        historyScrollPane.setPreferredSize(new Dimension(600, 200));
        historyScrollPane.setBorder(BorderFactory.createEmptyBorder());
        historyScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel historyContainer = new JPanel(new BorderLayout());
        historyContainer.setBackground(BG_COLOR);
        historyContainer.setBorder(new EmptyBorder(5, 10, 10, 10));
        historyContainer.add(createSeparator("Historique des scans"), BorderLayout.NORTH);
        historyContainer.add(historyScrollPane, BorderLayout.CENTER);

        frame.add(historyContainer, BorderLayout.SOUTH);

        // Initialize services
        ocrService = BFVOcrFactory.createDefaultService();
        screenshotAdapter = new AwtRobotScreenshotAdapter();
        gameToolsApiClient = new GameToolsApiClient();
        bfvHackersApiClient = new BfvHackersApiClient();
        hotkeyConfigService = new InMemoryHotkeyConfigurationAdapter();
        executorService = Executors.newSingleThreadExecutor();

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BFVOcrFactory.shutdown();
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException e) {
                System.err.println("Error unregistering native hook: " + e.getMessage());
            }
            executorService.shutdown();
            System.out.println("Resources released");
        }));

        // Show the window
        frame.setVisible(true);
    }

    /**
     * Creates a styled JLabel with specified text and alignment.
     */
    private JLabel createStyledLabel(String text, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Creates a styled separator with a title.
     */
    private JComponent createSeparator(String title) {
        JPanel separatorPanel = new JPanel(new BorderLayout(10, 0));
        separatorPanel.setBackground(BG_COLOR);
        separatorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 12));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(100, 100, 100));

        separatorPanel.add(titleLabel, BorderLayout.WEST);
        separatorPanel.add(separator, BorderLayout.CENTER);

        return separatorPanel;
    }

    /**
     * Configure the hotkey via a dialog.
     */
    private void configureHotkey() {
        configButton.setEnabled(false);

        JDialog dialog = new JDialog(frame, "Configurer Hotkey", true);
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(BG_COLOR);

        JLabel instructionLabel = createStyledLabel("Appuyez sur une touche pour la définir comme hotkey:", JLabel.CENTER);
        JLabel currentKeyLabel = createStyledLabel("Touche actuelle: " + hotkeyConfigService.getConfiguration().getHotkey(), JLabel.CENTER);
        currentKeyLabel.setFont(new Font(currentKeyLabel.getFont().getName(), Font.BOLD, 14));

        JButton saveButton = new JButton("Enregistrer");
        saveButton.setEnabled(false);

        JTextField keyField = new JTextField();
        keyField.setEditable(false);
        keyField.setBackground(new Color(60, 70, 100));
        keyField.setForeground(TEXT_COLOR);
        keyField.setHorizontalAlignment(JTextField.CENTER);
        keyField.setFont(new Font(keyField.getFont().getName(), Font.BOLD, 16));

        keyField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                // Convertir le code de touche Java AWT en texte directement
                String keyText = java.awt.event.KeyEvent.getKeyText(evt.getKeyCode());
                keyField.setText(keyText);
                saveButton.setEnabled(true);
            }
        });

        saveButton.addActionListener(e -> {
            if (!keyField.getText().isEmpty()) {
                hotkeyConfigService.updateConfiguration(keyField.getText());
                statusLabel.setText(String.format(SCAN_READY_TEXT, keyField.getText()));
                dialog.dispose();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.add(saveButton);

        contentPanel.add(instructionLabel, BorderLayout.NORTH);
        contentPanel.add(keyField, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel, BorderLayout.CENTER);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                configButton.setEnabled(true);
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Setup the look and feel for a dark theme.
     */
    private void setupLookAndFeel() {
        try {
            // Use system look and feel with customizations
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Customize colors for components
            UIManager.put("Panel.background", BG_COLOR);
            UIManager.put("Button.background", new Color(70, 80, 120));
            UIManager.put("Button.foreground", TEXT_COLOR);
            UIManager.put("ProgressBar.foreground", new Color(0, 122, 204));
            UIManager.put("ScrollPane.background", BG_COLOR);
            UIManager.put("ScrollBar.thumb", new Color(100, 100, 100));
            UIManager.put("ScrollBar.track", BG_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the UI for scan in progress.
     */
    private void updateUiForScanStarted() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(SCAN_RUNNING_TEXT);
            progressBar.setVisible(true);
            configButton.setEnabled(false);
            ocrResultLabel.setText("OCR: En cours...");
            ocrResultLabel.setForeground(TEXT_COLOR);
            gameToolsResultLabel.setText("GameTools: En attente...");
            gameToolsResultLabel.setForeground(TEXT_COLOR);
            hackersResultLabel.setText("BFVHackers: En attente...");
            hackersResultLabel.setForeground(TEXT_COLOR);
            timeLabel.setText("Temps d'exécution: calcul en cours...");
        });
    }

    /**
     * Updates the UI when scan is completed.
     */
    private void updateUiForScanCompleted(Duration duration) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(String.format(SCAN_READY_TEXT,
                    hotkeyConfigService.getConfiguration().getHotkey()));
            progressBar.setVisible(false);
            configButton.setEnabled(true);
            timeLabel.setText(String.format("Temps d'exécution: %d.%ds",
                    duration.toSeconds(), duration.toMillisPart() / 100));
        });
    }

    /**
     * Adds a scan result to the history panel.
     */
    private void addToHistory(String serverId, boolean success, String summary) {
        SwingUtilities.invokeLater(() -> {
            // Create timestamped entry
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timestamp = sdf.format(new Date());

            JPanel entryPanel = new JPanel();
            entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
            entryPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(70, 70, 70)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            entryPanel.setBackground(BG_COLOR);
            entryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(BG_COLOR);

            JLabel timeLabel = createStyledLabel(timestamp, JLabel.LEFT);
            timeLabel.setFont(new Font(timeLabel.getFont().getName(), Font.ITALIC, 11));

            String statusText = success ? "✓ Succès" : "✗ Échec";
            JLabel statusLabel = new JLabel(statusText);
            statusLabel.setForeground(success ? SUCCESS_COLOR : ERROR_COLOR);
            statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.BOLD, 12));

            headerPanel.add(timeLabel, BorderLayout.WEST);
            headerPanel.add(statusLabel, BorderLayout.EAST);

            JLabel titleLabel = createStyledLabel(
                    "Serveur #" + (serverId != null ? serverId : "inconnu"), JLabel.LEFT);
            titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 13));

            JLabel summaryLabel = createStyledLabel(summary, JLabel.LEFT);
            summaryLabel.setFont(new Font(summaryLabel.getFont().getName(), Font.PLAIN, 12));

            entryPanel.add(headerPanel);
            entryPanel.add(titleLabel);
            entryPanel.add(summaryLabel);

            // Add to top of history
            scanHistoryPanel.add(entryPanel, 0);
            scanHistoryPanel.revalidate();
            historyScrollPane.getVerticalScrollBar().setValue(0);
        });
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        String keyText = NativeKeyEvent.getKeyText(e.getKeyCode());
        String configuredHotkey = hotkeyConfigService.getConfiguration().getHotkey();

        if (keyText.equalsIgnoreCase(configuredHotkey) && !progressBar.isVisible()) {
            // Start the scan process in a background thread
            executorService.submit(this::performFullScan);
        }
    }

    /**
     * Performs the full server scanning workflow:
     * 1. Screenshot capture
     * 2. OCR processing
     * 3. GameTools API call
     * 4. BFVHackers API call
     */
    private void performFullScan() {
        Instant startTime = Instant.now();
        updateUiForScanStarted();

        String serverId = null;
        String longServerId = null;
        boolean success = false;
        String summary = "";

        try {
            // Step 1: Capture screenshot
            BufferedImage screenshot = screenshotAdapter.captureScreenshot();

            // Step 2: OCR processing
            try {
                Optional<String> serverIdOpt = ocrService.tryExtractServerNumber(screenshot);

                if (serverIdOpt.isPresent()) {
                    serverId = serverIdOpt.get();
                    String finalServerId = serverId;
                    SwingUtilities.invokeLater(() -> {
                        ocrResultLabel.setText("OCR: Server #" + finalServerId + " trouvé");
                        ocrResultLabel.setForeground(SUCCESS_COLOR);
                    });
                    summary += "OCR: ✓ | ";
                } else {
                    SwingUtilities.invokeLater(() -> {
                        ocrResultLabel.setText("OCR: Aucun numéro de serveur trouvé");
                        ocrResultLabel.setForeground(ERROR_COLOR);
                    });
                    summary += "OCR: ✗ | ";
                    throw new BFVOcrException("No server number found in screenshot");
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    ocrResultLabel.setText("OCR: Erreur - " + e.getMessage());
                    ocrResultLabel.setForeground(ERROR_COLOR);
                });
                summary += "OCR: ✗ | ";
                throw e;
            }

            // Step 3: GameTools API call
            try {
                SwingUtilities.invokeLater(() -> {
                    gameToolsResultLabel.setText("GameTools: Requête en cours...");
                });

                ServerInfo gameToolsInfo = gameToolsApiClient.fetchServerInfo(serverId);
                longServerId = String.valueOf(gameToolsInfo.getLongServerId());

                String finalLongServerId = longServerId;
                SwingUtilities.invokeLater(() -> {
                    gameToolsResultLabel.setText("GameTools: ID long: " + finalLongServerId);
                    gameToolsResultLabel.setForeground(SUCCESS_COLOR);
                });
                summary += "GameTools: ✓ | ";
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    gameToolsResultLabel.setText("GameTools: Erreur - " + e.getMessage());
                    gameToolsResultLabel.setForeground(ERROR_COLOR);
                });
                summary += "GameTools: ✗ | ";
                throw e;
            }

            // Step 4: BFVHackers API call
            try {
                SwingUtilities.invokeLater(() -> {
                    hackersResultLabel.setText("BFVHackers: Requête en cours...");
                });

                ServerInfo hackersInfo = bfvHackersApiClient.fetchServerInfo(longServerId);
                int cheaterCount = hackersInfo.getCheaterCount();

                SwingUtilities.invokeLater(() -> {
                    hackersResultLabel.setText("BFVHackers: " + cheaterCount + " cheaters détectés");
                    hackersResultLabel.setForeground(SUCCESS_COLOR);
                });
                summary += "BFVHackers: ✓";
                success = true;
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    hackersResultLabel.setText("BFVHackers: Erreur - " + e.getMessage());
                    hackersResultLabel.setForeground(ERROR_COLOR);
                });
                summary += "BFVHackers: ✗";
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Duration duration = Duration.between(startTime, Instant.now());
            updateUiForScanCompleted(duration);
            addToHistory(serverId, success, summary);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Not used
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Not used
    }

    public static void main(String[] args) {
        // Disable JNativeHook logging
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        SwingUtilities.invokeLater(() -> {
            try {
                // Register JNativeHook
                GlobalScreen.registerNativeHook();

                // Create and set up the application
                ScreenshotOcrFunctionalTest app = new ScreenshotOcrFunctionalTest();

                // Add the app as a native key listener
                GlobalScreen.addNativeKeyListener(app);

                System.out.println("Global hotkey listener registered successfully.");
                System.out.println("Press F9 from any window to capture and analyze screenshot.");

            } catch (NativeHookException | AWTException ex) {
                System.err.println("Error setting up the application: " + ex.getMessage());
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }
}