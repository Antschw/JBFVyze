package fr.antschw.bfv.infrastructure.screenshot;

import fr.antschw.bfv.domain.service.ScreenshotService;
import fr.antschw.bfv.domain.exception.ScreenshotCaptureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Concrete implementation of ScreenshotService using {@link java.awt.Robot}.
 * Captures raw screenshots without preprocessing.
 */
public class ScreenshotAdapter implements ScreenshotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotAdapter.class);

    private final Robot robot;
    private final GraphicsDevice graphicsDevice;

    /**
     * Default constructor, real dependencies.
     */
    public ScreenshotAdapter() throws AWTException {
        this(new Robot(), GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }

    /**
     * Constructor for testing (dependency injection).
     */
    public ScreenshotAdapter(Robot robot, GraphicsDevice graphicsDevice) {
        this.robot = robot;
        this.graphicsDevice = graphicsDevice;
    }

    @Override
    public BufferedImage captureScreenshot() throws ScreenshotCaptureException {
        int screenWidth = graphicsDevice.getDisplayMode().getWidth();
        int screenHeight = graphicsDevice.getDisplayMode().getHeight();

        LOGGER.info("Capturing full screen area: {}x{}", screenWidth, screenHeight);

        try {
            Rectangle captureArea = new Rectangle(0, 0, screenWidth, screenHeight);
            BufferedImage screenshot = robot.createScreenCapture(captureArea);
            LOGGER.info("Raw screenshot captured successfully");
            return screenshot;
        } catch (Exception e) {
            LOGGER.error("Failed to capture screenshot: {}", e.getMessage());
            throw new ScreenshotCaptureException("Failed to capture screenshot", e);
        }
    }
}