package fr.antschw.bfv.domain.service;

/**
 * Interface de service pour la gestion des paramètres persistants.
 */
public interface SettingsService {

    /**
     * Récupère le raccourci clavier configuré.
     *
     * @return la touche configurée
     */
    String getHotkey();

    /**
     * Définit le raccourci clavier.
     *
     * @param hotkey la touche à configurer
     */
    void setHotkey(String hotkey);

    /**
     * Récupère le nom du joueur surveillé.
     *
     * @return le nom du joueur
     */
    String getPlayerName();

    /**
     * Définit le nom du joueur à surveiller.
     *
     * @param playerName le nom du joueur
     */
    void setPlayerName(String playerName);

    /**
     * Indique si l'identifiant du joueur est utilisé.
     *
     * @return true si c'est un ID, false si c'est un nom
     */
    boolean isUsePlayerId();

    /**
     * Définit si l'identifiant du joueur est utilisé.
     *
     * @param usePlayerId true pour utiliser l'ID, false pour le nom
     */
    void setUsePlayerId(boolean usePlayerId);
}