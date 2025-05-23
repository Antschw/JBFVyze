package fr.antschw.bfv.domain.exception;

/**
 * Exception thrown when setting up hotkey listener fails.
 */
public class HotkeyListenerException extends Exception {

    public HotkeyListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
