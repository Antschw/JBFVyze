package fr.antschw.bfv.application.util.constants;

/**
 * Contains constant values related to API configuration.
 */
public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }

    // Api names
    public static final String GAMETOOLS_NAME = "GameTools";
    public static final String BFVHACKERS_NAME = "BFVHackers";
    public static final String GAMETOOLS_PLAYERS_NAME = "GameToolsPlayers";

    public static final String GAMETOOLS_API_BASE_URL = "https://api.gametools.network";
    public static final String BFVHACKERS_API_BASE_URL = "https://bfvhackers.com/api/v1/server-hackers";
    public static final int HTTP_TIMEOUT_SECONDS = 10;

    // GameTools Server Parameters
    public static final String GAMETOOLS_SERVER_ENDPOINT = "/bfv/servers";
    public static final String QUERY_PARAM_NAME = "name";
    public static final String QUERY_PARAM_REGION = "region";
    public static final String QUERY_PARAM_PLATFORM = "platform";
    public static final String QUERY_PARAM_LIMIT = "limit";

    public static final String REGION_ALL = "all";
    public static final String PLATFORM_PC = "pc";
    public static final String LIMIT_VALUE = "12";

    // GameTools Players Parameters
    public static final String GAMETOOLS_PLAYERS_ENDPOINT = "/bfv/players";

    // GameTools Player Stats Parameters
    public static final String GAMETOOLS_STATS_ENDPOINT = "/bfv/stats";
    public static final String QUERY_PARAM_FORMAT_VALUES = "format_values";
    public static final String QUERY_PARAM_SKIP_BATTLELOG = "skip_battlelog";
    public static final String QUERY_PARAM_LANG = "lang";

    public static final String FORMAT_VALUES_TRUE = "true";
    public static final String SKIP_BATTLELOG_FALSE = "false";
    public static final String LANG_EN_US = "en-us";

    // BFVHackers Parameters
    public static final String QUERY_PARAM_SERVERID = "server-id";

    // JSON Fields - Server Response
    public static final String JSON_SERVERS = "servers";
    public static final String JSON_PREFIX = "prefix";
    public static final String JSON_GAMEID = "gameId";
    public static final String JSON_NUM_HACKERS = "num_hackers";

    // JSON Fields - Players Response
    public static final String JSON_TEAMS = "teams";
    public static final String JSON_PLAYERS = "players";
    public static final String JSON_NAME = "name";
    public static final String JSON_PLAYER_ID = "player_id";
    public static final String JSON_USER_ID = "user_id";
    public static final String JSON_PLATOON = "platoon";
    public static final String JSON_RANK = "rank";

    // JSON Fields - Player Stats Response
    public static final String JSON_USERNAME = "userName";
    public static final String JSON_KILLS_PER_MINUTE = "killsPerMinute";
    public static final String JSON_ACCURACY = "accuracy";
    public static final String JSON_HEADSHOTS = "headshots";
    public static final String JSON_TIME_PLAYED = "timePlayed";
    public static final String JSON_SECONDS_PLAYED = "secondsPlayed";
    public static final String JSON_KILLS = "kills";
    public static final String JSON_DEATHS = "deaths";
    public static final String JSON_KILL_DEATH = "killDeath";


    // UI Constants
    /** Default startup width of the main window. */
    public static final int WINDOW_WIDTH = 600;
    /** Default startup height of the main window. */
    public static final int WINDOW_HEIGHT = 600;
    /** Spacing used in the navigation bar. */
    public static final int NAVBAR_SPACING = 30;
    /** Padding around the edges of the content pane. */
    public static final int WINDOW_PADDING = 20;
}