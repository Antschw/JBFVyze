package fr.antschw.bfv.domain.model;

/**
 * Modèle de données représentant le résumé des statistiques de BFVHackers pour un serveur.
 */
public record HackersSummary(
        int totalPlayers,
        int numLegit,
        int numSus,
        int numVerySus, 
        int numHackers,
        int age) {

    /**
     * Constructeur.
     * 
     * @param totalPlayers Nombre total de joueurs sur le serveur
     * @param numLegit Nombre de joueurs légitimes
     * @param numSus Nombre de joueurs suspects
     * @param numVerySus Nombre de joueurs très suspects
     * @param numHackers Nombre de hackers avérés
     * @param age Âge des données en secondes
     */
    public HackersSummary {
    }
    
    /**
     * Retourne le pourcentage de joueurs suspects ou pire.
     * 
     * @return Le pourcentage de joueurs suspects ou pire
     */
    public double getSuspiciousPercentage() {
        if (totalPlayers == 0) return 0;
        return 100.0 * (numSus + numVerySus + numHackers) / totalPlayers;
    }
    
    /**
     * Retourne le pourcentage de joueurs problématiques (très suspects ou hackers).
     * 
     * @return Le pourcentage de joueurs problématiques
     */
    public double getProblemPercentage() {
        if (totalPlayers == 0) return 0;
        return 100.0 * (numVerySus + numHackers) / totalPlayers;
    }
}