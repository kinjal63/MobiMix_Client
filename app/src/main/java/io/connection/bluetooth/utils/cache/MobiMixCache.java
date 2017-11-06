package io.connection.bluetooth.utils.cache;

import java.util.HashMap;
import java.util.List;

import io.connection.bluetooth.Database.entity.MBGameParticipants;
import io.connection.bluetooth.Domain.GameRequest;

/**
 * Created by Kinjal on 11/6/2017.
 */
public class MobiMixCache {
    private static HashMap<String, Object> cachePropertiesMap = new HashMap<>();
    private static HashMap<String, GameRequest> cacheGameMap = new HashMap<>();

    public static void saveGames(List<MBGameParticipants> gameParticipants) {
        gameParticipants.addAll(gameParticipants);
    }

    public static void putInCache(String key, Object value) {
        cachePropertiesMap.put(key, value);
    }

    public static Object getFromCache(String key) {
        return cachePropertiesMap.get(key);
    }

    public static GameRequest getGameFromCache(String key) {
        return cacheGameMap.get(key);
    }

    public static void putGameInCache(String key, GameRequest gameRequest) {
        cacheGameMap.put(key, gameRequest);
    }
}
