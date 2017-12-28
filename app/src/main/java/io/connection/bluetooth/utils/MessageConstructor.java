package io.connection.bluetooth.utils;

import org.json.JSONException;
import org.json.JSONObject;

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
    public static JSONObject constructObjectToSendAckEvent(int event) {
        JSONObject jsonObject = new JSONObject();
        String userId = ApplicationSharedPreferences.getInstance(
                MobiMixApplication.getInstance().getContext()).getValue("user_id");
        try {
            jsonObject.put(GameConstants.GAME_EVENT, event);
            jsonObject.put(GameConstants.USER_ID, userId);
        }
        catch (JSONException e) {
            jsonObject = null;
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToRequestForEvent(int event) {
        JSONObject jsonObject = new JSONObject();
        String userId = ApplicationSharedPreferences.getInstance(
                MobiMixApplication.getInstance().getContext()).getValue("user_id");
        try {
            jsonObject.put(GameConstants.GAME_EVENT, event);
            jsonObject.put(GameConstants.USER_ID, userId);
        }
        catch (JSONException e) {
            jsonObject = null;
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToSendGameRequestEvent(EventData eventData) {
        GameRequest gameRequest = MobiMixCache.getGameFromCache(eventData.userId_);
        JSONObject jsonObject = null;
        if(gameRequest != null) {
            jsonObject = new JSONObject();
            String userId = ApplicationSharedPreferences.getInstance(
                    MobiMixApplication.getInstance().getContext()).getValue("user_id");
            try {
                jsonObject.put(GameConstants.USER_ID, userId);
                jsonObject.put(GameConstants.GAME_EVENT, MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST);
                jsonObject.put(GameConstants.GAME_ID, (int) gameRequest.getGameId());
                jsonObject.put(GameConstants.GAME_NAME, gameRequest.getGameName());
                jsonObject.put(GameConstants.GAME_PACKAGE_NAME, gameRequest.getGamePackageName());
                jsonObject.put(GameConstants.GAME_REQUEST_SENDER_ID, gameRequest.getRemoteUserId());
                jsonObject.put(GameConstants.GAME_REQUEST_SENDER_NAME, gameRequest.getRemoteUserName());
                jsonObject.put(GameConstants.GAME_REQUEST_CONNECTION_TYPE, gameRequest.getConnectionType());
                jsonObject.put(GameConstants.GAME_REQUEST_DEVICE_NAME, gameRequest.getBluetoothAddress());
            } catch (JSONException e) {
                jsonObject = null;
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToSendGameLaunchedEvent(EventData eventData) {
        GameRequest gameRequest = MobiMixCache.getGameFromCache(eventData.userId_);
        JSONObject jsonObject = new JSONObject();
        try {
            String userId = ApplicationSharedPreferences.getInstance(
                    MobiMixApplication.getInstance().getContext()).getValue("user_id");

            jsonObject.put(GameConstants.GAME_EVENT, eventData.event_);
            jsonObject.put(GameConstants.USER_ID, userId);
            jsonObject.put(GameConstants.GAME_ID, gameRequest.getGameId());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToUpdateDBData(EventData eventData) {
        GameRequest gameRequest = MobiMixCache.getGameFromCache(eventData.userId_);
        JSONObject jsonObject = new JSONObject();
        try {
            String userId = ApplicationSharedPreferences.getInstance(
                    MobiMixApplication.getInstance().getContext()).getValue("user_id");
            if(Integer.parseInt(MobiMixCache.getFromCache(CacheConstants.CACHE_IS_GROUP_OWNER).toString()) == 1) {
                jsonObject.put(GameConstants.GROUP_OWNER_USER_ID, userId);
                jsonObject.put(GameConstants.CONNECTED_USER_ID, gameRequest.getRemoteUserId());
            }
            else {
                jsonObject.put(GameConstants.GROUP_OWNER_USER_ID, gameRequest.getRemoteUserId());
                jsonObject.put(GameConstants.CONNECTED_USER_ID, userId);
            }
            jsonObject.put(GameConstants.GAME_EVENT, eventData.event_);
            jsonObject.put(GameConstants.GAME_ID, gameRequest.getGameId());
            jsonObject.put(GameConstants.GAME_CONNECTION_TYPE, gameRequest.getConnectionType());
            jsonObject.put(GameConstants.USER_ID, userId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
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
