package fr.antschw.bfv.utils;

import java.util.ResourceBundle;

/**
 * Utility class for managing internationalization resource bundles.
 */
public final class I18nUtils {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages_en");

    private I18nUtils() {}

    /**
     * Retrieves a localized string by key.
     *
     * @param key the key of the message
     * @return localized message string
     */
    public static String get(String key) {
        return bundle.getString(key);
    }

    /**
     * Provides direct access to the loaded ResourceBundle.
     *
     * @return ResourceBundle used for internationalization
     */
    public static ResourceBundle getBundle() {
        return bundle;
    }
}
