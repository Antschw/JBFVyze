package fr.antschw.bfv.infrastructure.api.model;

import static fr.antschw.bfv.common.constants.ApiConstants.BFVHACKERS_NAME;
import static fr.antschw.bfv.common.constants.ApiConstants.GAMETOOLS_NAME;
import static fr.antschw.bfv.common.constants.ApiConstants.GAMETOOLS_PLAYERS_NAME;

/**
 * Enumeration for supported API types.
 */
public enum ApiType {
    GAMETOOLS(GAMETOOLS_NAME),
    BFVHACKERS(BFVHACKERS_NAME),
    GAMETOOLS_PLAYERS(GAMETOOLS_PLAYERS_NAME);

    private final String name;

    ApiType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}