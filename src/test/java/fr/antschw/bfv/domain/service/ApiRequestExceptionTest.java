
package fr.antschw.bfv.domain.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiRequestExceptionTest {

    @Test
    void testConstructorWithMessage() {
        ApiRequestException exception = new ApiRequestException("Error occurred");
        assertEquals("Error occurred", exception.getMessage());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        ApiRequestException exception = new ApiRequestException("Error occurred", cause);
        assertEquals("Error occurred", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
