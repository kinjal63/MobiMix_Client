package io.connection.bluetooth.utils;

import org.json.JSONException;
import org.json.JSONObject;

import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.utils.cache.CacheConstants;
import io.connection.bluetooth.utils.cache.MobiMixCache;

/**
 * Created by Kinjal on 10/29/2017.
 */

public class MessageConstructor {

    public static JSONObject constructObjectToSendEvent(int event) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.GAME_EVENT, event);
        }
        catch (JSONException e) {
            jsonObject = null;
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToSendGameRequest() {
        GameRequest gameRequest = UtilsHandler.removeGameFromStack();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.GAME_EVENT, MobiMix.GameEvent.EVENT_GAME_INFO_RESPONSE);
            jsonObject.put(Constants.GAME_ID, (int) gameRequest.getGameId());
            jsonObject.put(Constants.GAME_NAME, gameRequest.getGameName());
            jsonObject.put(Constants.GAME_PACKAGE_NAME, gameRequest.getGamePackageName());
            jsonObject.put(Constants.GAME_REQUEST_SENDER_ID, gameRequest.getRemoteUserId());
            jsonObject.put(Constants.GAME_REQUEST_SENDER_NAME, gameRequest.getRemoteUserName());
            jsonObject.put(Constants.GAME_REQUEST_CONNECTION_TYPE, gameRequest.getConnectionType());
        }
        catch (JSONException e) {
            jsonObject = null;
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getEventGameLaunchedObject(String connectedUserId) {
        GameRequest gameRequest = MobiMixCache.getGameFromCache(connectedUserId);
        JSONObject jsonObject = new JSONObject();
        try {
            String userId = ApplicationSharedPreferences.getInstance(
                    MobiMixApplication.getInstance().getContext()).getValue("user_id");
            if(Integer.parseInt(MobiMixCache.getFromCache(CacheConstants.CACHE_IS_GROUP_OWNER).toString()) == 1) {
                jsonObject.put(Constants.GROUP_OWNER_USER_ID, userId);
                jsonObject.put(Constants.CONNECTED_USER_ID, gameRequest.getRemoteUserId());
            }
            else {
                jsonObject.put(Constants.GROUP_OWNER_USER_ID, gameRequest.getRemoteUserId());
                jsonObject.put(Constants.CONNECTED_USER_ID, userId);
            }
            jsonObject.put(Constants.GAME_ID, gameRequest.getGameId());
            jsonObject.put(Constants.GAME_CONNECTION_TYPE, gameRequest.getConnectionType());
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
