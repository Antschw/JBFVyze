
package fr.antschw.bfv.domain.exception;

/**
 * Custom exception thrown when screenshot capturing fails.
 */
public class ScreenshotCaptureException extends Exception {

    /**
     * Constructs a ScreenshotCaptureException with a message.
     *
     * @param message the exception message
     */
    public ScreenshotCaptureException(String message) {
        super(message);
    }

    /**
     * Constructs a ScreenshotCaptureException with a message and cause.
     *
     * @param message the exception message
     * @param cause the underlying cause
     */
    public ScreenshotCaptureException(String message, Throwable cause) {
        super(message, cause);
    }
}
