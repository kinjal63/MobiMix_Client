package io.connection.bluetooth.utils;

import java.util.UUID;

/**
 * Created by songline on 01/08/16.
 */
public class Constants {

    public static final String NAME_UUID = "custom_uuid";
    // public static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static UUID uuid = UUID.fromString("6f50463f-e73f-48ca-a5f6-d2080cd24363");
    //public static final String endPointAddress = "http://transfer-teqnihome.rhcloud.com/";
    public static final String endPointAddress = "http://192.168.0.106:8080/";
    // public static final String endPointAddress = "http://192.168.43.123:8080/";
    public static final String TOKEN = "push_token";
    public static final String TOKEN_KEY = "token";   
    public static final String LOGIN = "login";
    public static final String GAME = "game";
    public static final String GAME_TIME_MORNING = "game_time_morning";
    public static final String GAME_TIME_AFTERNOON = "game_time_afternoon";
    public static final String GAME_TIME_EVENING = "game_time_evening";
    public static final String LOGIN_KEY = "login_id_key";
    public static final String NAME_KEY = "name_key";
    public static final String EMAIL_KEY = "email_key";
    public static final String DOB_KEY = "dob_key";
    public static final String APP_VERSION = "app_version";
    public static final String ERROR_MESSAGE = "Something went wrong, try again later";
    public static final String INTERNET_ERROR_MESSAGE = "No Internet Connection";
    public static final String CHAT_MODULE = "Module :: Chat";
    public static final String BUSINESSCARD_MODULE = "Module :: BusinessCard";
    public static final String FILESHARING_MODULE = "Module :: FileSharing";
    public static final String GAME_MODULE = "Module :: GameModule";
    public static final String NO_MODULE = "None";
    public static final String STR_UNDERSCORE = "_";

    public static final int GROUP_OWNER_PORT = 5050;
    public static final int SOCKET_TIMEOUT = 5000;
    public static final int FIRSTMESSAGEXCHANGE = 0x500 + 1;
    public static final int FIRSTMESSAGEXCHANGE_BLUETOOTH = 0x400 + 1;
    public static final int MESSAGE_READ = 0x400 + 2;
    public static final int MODULE_READ = 0x400 + 3;
    public static final int INIT_MODULE = 0x400 + 4;
    public static final int GAME_REQUEST_ACCEPTED = 0x400 + 5;
    public static final int MESSAGE_READ_GAME = 0x400 + 6;
    public static final int MESSAGE_HEARBEAT = 0x400 + 7;

    public static final int THREAD_COUNT = 20; //maximum number of clients that this GO can manage
    public static final int THREAD_POOL_EXECUTOR_KEEP_ALIVE_TIME = 10;

    public static final String PREF_WIFIDIRECT_CONNECTED = "WifiDirect_Connected";
    public static final String PREF_CHAT_ACTIVITY_OPEN = "Chat_Screen_Open_";
    public static final String PREF_MY_UUID = "my_uuid";

    // HeartBeat signal
    public static final String HEARTBEAT_SIGNAL = "heartbeat";
    public static final String HEARTBEAT_MESSAGE = "hello";
}
//  Query

/**
 * SELECT  * , SQRT( POW(69.1 * (latitude - 12.9735414), 2) + POW(69.1 * (77.6366984 - longitude) * COS(latitude / 57.3), 2)) AS distance FROM taqnihome_user user HAVING distance < 0.00621371 ORDER BY distance
 **/
