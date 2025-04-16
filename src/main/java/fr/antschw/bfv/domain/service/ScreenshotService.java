package fr.antschw.bfv.domain.service;

import java.awt.image.BufferedImage;

/**
 * Defines the contract for capturing screenshots.
 */
public interface ScreenshotService {

    /**
     * Captures and returns a raw screenshot without preprocessing.
     *
     * @return BufferedImage representing the raw screenshot
     * @throws ScreenshotCaptureException if the capture fails
     */
    BufferedImage captureScreenshot() throws ScreenshotCaptureException;
}