package fr.antschw.bfv.domain.service;

/**
 * Exception thrown when an API request fails.
 */
public class ApiRequestException extends Exception {

    public ApiRequestException(String message) {
        super(message);
    }

    public ApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
