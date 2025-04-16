package fr.antschw.bfv.infrastructure.api.model;

/**
 * Enumeration for supported API types.
 */
public enum ApiType {
    GAMETOOLS("GameTools"),
    BFVHACKERS("BFVHackers"),
    GAMETOOLS_PLAYERS("GameToolsPlayers");

    private final String name;

    ApiType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}