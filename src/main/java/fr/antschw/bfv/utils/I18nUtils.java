package fr.antschw.bfv.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for managing internationalization resource bundles.
 */
public class I18nUtils {
    private static Locale currentLocale = Locale.getDefault();
    private static ResourceBundle bundle = loadBundle(currentLocale);

    private I18nUtils() {
        // Prevent instantiation
    }

    private static ResourceBundle loadBundle(Locale locale) {
        return ResourceBundle.getBundle("i18n.messages", locale);
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = loadBundle(locale);
    }

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
