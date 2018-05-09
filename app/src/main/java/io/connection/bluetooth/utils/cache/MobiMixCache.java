package io.connection.bluetooth.utils.cache;

import android.bluetooth.BluetoothSocket;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import io.connection.bluetooth.Database.entity.MBGameParticipants;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Domain.GameRequest;

/**
 * Created by Kinjal on 11/6/2017.
 */
public class MobiMixCache {
    private static HashMap<String, Object> cachePropertiesMap = new HashMap<>();
    private static HashMap<String, GameRequest> cacheGameMap = new HashMap<>();
    private static List<Socket> cacheClientSockets = new ArrayList<>();
    private static List<BluetoothSocket> cacheClientBluetoothSockets = new ArrayList<>();
    private static GameRequest cacheCurrentGameRequest = null;
    private static List<MBNearbyPlayer> cacheQueuedGamePlayers = new ArrayList<>();

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
        setCurrentGameRequestInCache(gameRequest);
        cacheGameMap.put(user, gameRequest);
    }

    public static void setCurrentGameRequestInCache(GameRequest gameRequest) {
        cacheCurrentGameRequest = gameRequest;
    }

    public static GameRequest getCurrentGameRequestFromCache() {
        return cacheCurrentGameRequest;
    }

    public static void clearFromCache() {
        cacheCurrentGameRequest = null;
        cacheGameMap.clear();
        cacheQueuedGamePlayers.clear();
        cacheClientSockets.clear();
    }

    public static void addClientSocket(Socket socket) {
        cacheClientSockets.add(socket);
    }

    public static List<Socket> getClientSockets() {
        return cacheClientSockets;
    }

    public static void addPlayersInQueueCache(List<MBNearbyPlayer> players) {
        cacheQueuedGamePlayers.addAll(players);
    }

    public static List<MBNearbyPlayer> getQueuedPlayersFromCache() {
        return cacheQueuedGamePlayers;
    }

    public static void removeQueuedPlayersFromCache() {
        cacheQueuedGamePlayers.clear();
    }

}