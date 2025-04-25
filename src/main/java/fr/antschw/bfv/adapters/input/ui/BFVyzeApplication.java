package fr.antschw.bfv.adapters.input.ui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import fr.antschw.bfv.adapters.input.window.ResizeController;
import fr.antschw.bfv.adapters.input.window.TitleBarController;
import fr.antschw.bfv.adapters.input.window.WindowManager;
import fr.antschw.bfv.common.constants.UIConstants;
import fr.antschw.bfv.infrastructure.config.HotkeyModule;
import fr.antschw.bfv.infrastructure.config.ScanModule;
import fr.antschw.bfv.infrastructure.config.ServerScanUiModule;
import fr.antschw.bfvocr.api.BFVOcrFactory;
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

    @Override
    public void start(Stage primaryStage) {
        // DI setup
        Injector injector = Guice.createInjector(
                new ScanModule(),
                new HotkeyModule(),
                new ServerScanUiModule()
        );

        Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
        MainController mainController = injector.getInstance(MainController.class);

        BorderPane root = new BorderPane();

        TitleBarController titleBarController = new TitleBarController();
        root.setTop(titleBarController.createTitleBar(primaryStage));
        root.setCenter(mainController.getRoot());

        ResizeController resizeController = new ResizeController();
        resizeController.makeResizable(primaryStage, root);

        Scene scene = new Scene(root, UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);

        if (getClass().getResource("/styles/style.css") == null) {
            LOGGER.warn("Warning: CSS stylesheet not found!");
        }
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        new WindowManager().configureStage(primaryStage);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        BFVOcrFactory.shutdown();

        LOGGER.info("Application stopped, OCR resources released");
    }

    public static void main(String[] args) {
        launch(args);
    }
}