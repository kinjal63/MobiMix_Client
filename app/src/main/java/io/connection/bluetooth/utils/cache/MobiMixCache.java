package io.connection.bluetooth.utils.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import io.connection.bluetooth.Database.entity.MBGameParticipants;
import io.connection.bluetooth.Domain.GameRequest;

/**
 * Created by Kinjal on 11/6/2017.
 */
public class MobiMixCache {
    private static HashMap<String, Object> cachePropertiesMap = new HashMap<>();
    private static HashMap<String, GameRequest> cacheGameMap = new HashMap<>();
    private static Vector<String> cacheUserRequests = new Vector<>();

    public static void saveGames(List<MBGameParticipants> gameParticipants) {
        gameParticipants.addAll(gameParticipants);
    }

    public static boolean getCacheFromUserRequests(String user) {
        return cacheUserRequests.contains(user);
    }

    public static void setCacheUserRequests(String gameRequestForUser) {
        cacheUserRequests.add(gameRequestForUser);
    }

    public static void removeUserRequests(String gameRequestForUser) {
        cacheUserRequests.add(gameRequestForUser);
    }

    public static void putInCache(String key, Object value) {
        cachePropertiesMap.put(key, value);
    }

    public static Object getFromCache(String key) {
        return cachePropertiesMap.get(key);
    }

    public static GameRequest getGameFromCache(String user) {
        return cacheGameMap.get(user);
    }

    public static void putGameInCache(String user, GameRequest gameRequest) {
        cacheGameMap.put(user, gameRequest);
    }
}
