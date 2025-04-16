
package fr.antschw.bfv.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScreenshotCaptureExceptionTest {

    @Test
    void testConstructorWithMessage() {
        ScreenshotCaptureException exception = new ScreenshotCaptureException("Error occurred");
        assertEquals("Error occurred", exception.getMessage());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        ScreenshotCaptureException exception = new ScreenshotCaptureException("Error occurred", cause);
        assertEquals("Error occurred", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
