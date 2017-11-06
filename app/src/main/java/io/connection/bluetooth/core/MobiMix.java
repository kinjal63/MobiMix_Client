package io.connection.bluetooth.core;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class MobiMix {
    /*
    Constants to get network calls for DB sync operation
     */
    public interface APIResponse {
        public static final int RESPONSE_GET_NEARBY_PLAYERS = 1;
    }

    public interface DBRequest {
        public static final int DB_FIND_NEARBY_PLAYERS = 101;
        public static final int DB_FIND_MUTUAL_GAMES = 102;
        public static final int DB_UPDATE_GAME_TABLE = 103;
    }

    public interface DBResponse {
        public static final int DB_RES_FIND_NEARBY_PLAYERS = 201;
        public static final int DB_RES_FIND_MUTUAL_GAMES = 202;
    }

    public interface MBDatabase {
        public static final String DATABASE_NAME = "mobimix.db";
        public static final String TABLE_NAME_NEARBY_PLAYERS = "mb_nearby_players";
        public static final int DATABASE_VERSION = 1;
    }

    public interface GUIEvent {
        public static final int EVENT_GAME_REQUEST = 1;
    }

    public interface GameEvent {
        public static final int EVENT_GAME_INFO_REQUEST = 301;
        public static final int EVENT_GAME_INFO_RESPONSE = 302;
        public static final int EVENT_GAME_LAUNCHED = 303;
        public static final int EVENT_GAME_UPDATE_TABLE = 304;
    }
}
