package fr.antschw.bfv.ui;

import fr.antschw.bfv.application.orchestrator.ServerScanCoordinator;
import fr.antschw.bfv.application.util.AppConstants;
import fr.antschw.bfv.domain.service.HotkeyListenerService;
import fr.antschw.bfv.domain.service.UserStatsCacheService;
import fr.antschw.bfv.infrastructure.binding.AppModule;
import fr.antschw.bfv.infrastructure.cache.UserStatsCacheAdapter;
import fr.antschw.bfv.ui.control.ResizeController;
import fr.antschw.bfv.ui.control.TitleBarController;
import fr.antschw.bfv.ui.control.WindowController;
import fr.antschw.bfvocr.api.BFVOcrFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * JavaFX Application entry point for BFVyze.
 */
public class BFVyzeApplication extends Application {

    private final static Logger LOGGER = LoggerFactory.getLogger(BFVyzeApplication.class.getName());

    private Injector injector;

    @Override
    public void start(Stage primaryStage) {
        // DI setup
        injector = Guice.createInjector(
                new AppModule()
        );

        Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
        MainController mainController = injector.getInstance(MainController.class);

        BorderPane root = new BorderPane();

        TitleBarController titleBarController = new TitleBarController();
        root.setTop(titleBarController.createTitleBar(primaryStage));
        root.setCenter(mainController.getRoot());

        ResizeController resizeController = new ResizeController();
        resizeController.makeResizable(primaryStage, root);

        Scene scene = new Scene(root, AppConstants.WINDOW_WIDTH, AppConstants.WINDOW_HEIGHT);

        if (getClass().getResource("/styles/style.css") == null) {
            LOGGER.warn("Warning: CSS stylesheet not found!");
        }
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        new WindowController().configureStage(primaryStage);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // 1) Stop hotkey listener
        try {
            HotkeyListenerService hotkey = injector.getInstance(HotkeyListenerService.class);
            hotkey.stopListening();
        } catch (Exception e) {
            LOGGER.warn("Failed to stop hotkey listener on shutdown.", e);
        }

        // 2) Shutdown scan executor
        try {
            ServerScanCoordinator scanService = injector.getInstance(ServerScanCoordinator.class);
            scanService.shutdown();
        } catch (Exception e) {
            LOGGER.warn("Failed to shutdown scan service on shutdown.", e);
        }

        // 3) Sauvegarde du cache existante
        super.stop();
        if (injector != null) {
            try {
                UserStatsCacheService cacheAdapter = injector.getInstance(UserStatsCacheService.class);
                if (cacheAdapter instanceof UserStatsCacheAdapter jsonCache) {
                    LOGGER.info("Saving cache to disk...");
                    jsonCache.saveToDisk();
                }
            } catch (Exception e) {
                LOGGER.error("Error saving cache: {}", e.getMessage(), e);
            }
        }

        // 4) Libération des ressources OCR
        BFVOcrFactory.shutdown();
        LOGGER.info("Application stopped, OCR resources released");

        // 5) Forcer la sortie pour terminer tout thread non-daemon (AWT, JNativeHook…)
        System.exit(0);
    }


    public static void main(String[] args) {
        launch(args);
    }
}