package fr.antschw.bfv.common.constants;

/**
 * Contains constants used in ScanServer orchestrator.
 */
public final class ScanConstants {

    private ScanConstants() {
        // Prevent instantiation
    }

    public static final String SCAN_START_MESSAGE = "Starting server scan...";
    public static final String SCAN_SUCCESS_MESSAGE = "Scan completed successfully.";
    public static final String SCAN_ERROR_MESSAGE = "Error during server scan.";
}
