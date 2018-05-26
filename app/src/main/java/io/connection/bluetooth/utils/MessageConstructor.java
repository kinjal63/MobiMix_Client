package io.connection.bluetooth.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.connection.bluetooth.Database.entity.MBGameParticipants;
import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.core.EventData;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.utils.cache.CacheConstants;
import io.connection.bluetooth.utils.cache.MobiMixCache;

/**
 * Created by Kinjal on 10/29/2017.
 */

public class MessageConstructor {
    public static JSONObject constructObjectToSendGameRequestEvent(EventData eventObj) {
        GameRequest gameRequest = MobiMixCache.getGameFromCache(eventObj.userId_);
        JSONObject jsonObject = null;
        if(gameRequest != null) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put(GameConstants.GAME_ID, (int) gameRequest.getGameId());
                jsonObject.put(GameConstants.GAME_NAME, gameRequest.getGameName());
                jsonObject.put(GameConstants.GAME_PACKAGE_NAME, gameRequest.getGamePackageName());
                jsonObject.put(GameConstants.GAME_REQUEST_SENDER_ID, gameRequest.getRemoteUserId());
                jsonObject.put(GameConstants.GAME_REQUEST_SENDER_NAME, gameRequest.getRequesterUserName());
                jsonObject.put(GameConstants.GAME_REQUEST_CONNECTION_TYPE, gameRequest.getConnectionType());
                jsonObject.put(GameConstants.GAME_REQUEST_DEVICE_NAME, gameRequest.getBluetoothAddress());
            } catch (JSONException e) {
                jsonObject = null;
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToSendGameLaunchedEvent(EventData eventObj) {
        GameRequest gameRequest = MobiMixCache.getGameFromCache(eventObj.userId_);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(GameConstants.GAME_ID, gameRequest.getGameId());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToSendQueuedUserEvent(EventData eventObj) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(GameConstants.GAME_PLAYERS_IN_QUEUE, MobiMixCache.getQueuedPlayersFromCache());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToUpdateDBData(EventData eventObj) {
        GameRequest gameRequest = MobiMixCache.getCurrentGameRequestFromCache();

        JSONObject jsonObject = new JSONObject();
        try {
            String userId = ApplicationSharedPreferences.getInstance(
                    MobiMixApplication.getInstance().getContext()).getValue("user_id");
            if(Integer.parseInt(MobiMixCache.getFromCache(CacheConstants.CACHE_IS_GROUP_OWNER).toString()) == 1) {
                jsonObject.put(GameConstants.GROUP_OWNER_USER_ID, userId);
                jsonObject.put(GameConstants.CONNECTED_USER_ID, eventObj.userId_);
            }
            jsonObject.put(GameConstants.GAME_ID, gameRequest.getGameId());
            jsonObject.put(GameConstants.GAME_CONNECTION_TYPE, gameRequest.getConnectionType());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToSendDBDataInBatch(EventData eventObj) {
        JSONObject jsonObject = new JSONObject();
        try {
            List<MBGameParticipants> gameUsers = (List<MBGameParticipants>)eventObj.object_.opt(GameConstants.GAME_UPDATE_TABLE_DATA);

            if(gameUsers.size() <= 0) {
                return null;
            }

            String groupOwner = gameUsers.get(0).getPlayerId();
            long gameId = gameUsers.get(0).getGameId();
            int connectionType = gameUsers.get(0).getConnectionType();
            int maxPlayers = gameUsers.get(0).getMaxPlayers();

            String connectedUsers = "";
            for(MBGameParticipants gameParticipant : gameUsers) {
                connectedUsers += gameParticipant.getConnnectedPlayerId();
            }

            jsonObject.put(GameConstants.GAME_CONNECTED_USER_IDS, connectedUsers);
            jsonObject.put(GameConstants.GAME_GROUP_OWNER_USER_ID, groupOwner);
            jsonObject.put(GameConstants.GAME_ID, gameId);
            jsonObject.put(GameConstants.GAME_CONNECTION_TYPE, connectionType);
            jsonObject.put(GameConstants.GAME_MAX_PLAYERS, maxPlayers);

//            jsonObject.put(GameConstants.GAME_PARTICIPANTS, eventObj.object_.opt(GameConstants.GAME_UPDATE_TABLE_DATA));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject addEventAndSocketAddress(JSONObject object, EventData eventData) {
        String userId = ApplicationSharedPreferences.getInstance(
                MobiMixApplication.getInstance().getContext()).getValue("user_id");
        try {
            object.put(GameConstants.GAME_EVENT, eventData.event_);
            object.put(GameConstants.CLIENT_SOCKET_ADDRESS, eventData.socketAddr_);
            object.put(GameConstants.USER_ID, userId);
        }
        catch (JSONException e) {
            Log.e("MessageConstructor", "Event and Socket Address cannot be added to json object.");
            e.printStackTrace();
        }

        return object;
    }

    public static JSONObject getHandShakeSignalObj() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.HEARTBEAT_SIGNAL, Constants.HEARTBEAT_MESSAGE);
        }
        catch (JSONException e) {
            jsonObject = null;
            e.printStackTrace();
        }
        return jsonObject;
    }
}