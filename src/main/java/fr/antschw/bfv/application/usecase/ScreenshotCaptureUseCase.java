package fr.antschw.bfv.application.usecase;

import fr.antschw.bfv.domain.service.ScreenshotCaptureException;
import fr.antschw.bfv.domain.service.ScreenshotService;

import java.awt.image.BufferedImage;

/**
 * Orchestrates the screenshot capture use case.
 */
public class ScreenshotCaptureUseCase {

    private final ScreenshotService screenshotService;

    /**
     * Constructor.
     *
     * @param screenshotService the service responsible for capturing screenshots
     */
    public ScreenshotCaptureUseCase(ScreenshotService screenshotService) {
        this.screenshotService = screenshotService;
    }

    /**
     * Captures and returns a raw screenshot.
     *
     * @return BufferedImage representing the raw screenshot
     * @throws ScreenshotCaptureException if capture fails
     */
    public BufferedImage captureScreenshot() throws ScreenshotCaptureException {
        return screenshotService.captureScreenshot();
    }
}