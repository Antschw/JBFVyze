package fr.antschw.bfv.domain.model;

/**
 * Represents the hotkey configuration.
 */
public class HotkeyConfiguration {

    private String hotkey;

    /**
     * Constructor.
     *
     * @param hotkey the initial hotkey
     */
    public HotkeyConfiguration(String hotkey) {
        this.hotkey = hotkey;
    }

    public String getHotkey() {
        return hotkey;
    }

    public void setHotkey(String hotkey) {
        this.hotkey = hotkey;
    }
}
