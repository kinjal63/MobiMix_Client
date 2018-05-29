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
        public static final int DB_FIND_GAME_FROM_ID = 104;
        public static final int DB_READ_GAME_TABLES = 105;
        public static final int DB_UPDATE_GAME_TABLE_BATCH = 106;
        public static final int DB_DELETE_GAME_PARTICIPANTS = 107;
        public static final int DB_FIND_NEARBY_PLAYER_FROM_EMAIL = 108;
    }

    public interface DBResponse {
        public static final int DB_RES_FIND_NEARBY_PLAYERS = 201;
        public static final int DB_RES_FIND_MUTUAL_GAMES = 202;
        public static final int DB_RES_FIND_NEARBY_PLAYER = 203;
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
        public static final int EVENT_CONNECTION_ESTABLISHED_ACK = 300;
        public static final int EVENT_GAME_INFO_REQUEST_ASK = 301;
        public static final int EVENT_GAME_INFO_REQUEST = 302;
        //        public static final int EVENT_GAME_INFO_RESPONSE = 302;
        public static final int EVENT_GAME_INFO_REQUEST_ACK = 304;
        public static final int EVENT_GAME_LAUNCHED = 305;
        public static final int EVENT_GAME_LAUNCHED_ACK = 306;
        public static final int EVENT_GAME_UPDATE_TABLE_REQUEST = 307;
        public static final int EVENT_GAME_UPDATE_TABLE_DATA = 308;
        public static final int EVENT_GAME_UPDATE_TABLE_ACK = 309;
        public static final int EVENT_GAME_QUEUED_USER_ASK = 310;
        public static final int EVENT_GAME_QUEUED_USER = 311;
        //set an event to send game request to queued users
        public static final int EVENT_GAME_REQUEST_TO_QUEUED_USERS = 312;
        public static final int EVENT_GAME_REQUEST_TO_USERS = 313;
        public static final int EVENT_GAME_READ_TABLE_DATA = 314;
        public static final int EVENT_GAME_QUEUED_USER_ACK = 315;

        public static final int EVENT_GAME_CONNECTION_CLOSED = 350;

        public static final int EVENT_SOCKET_INITIALIZED = 351;
        public static final int EVENT_SOCKET_DISCONNECTED = 352;

        // Bluetooth events
        public static final int EVENT_GAME_START = -300;
    }

    public interface ScoketEvents {
        public static final String EVENT_BUSINESSCARD_RECEIVED = "BusinessCard_Received";
        public static final String EVENT_FILE_RECEIVED = "File_Received";
    }
}
