package io.connection.bluetooth.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.core.MobiMix;

/**
 * Created by Kinjal on 10/29/2017.
 */

public class MessageConstructor {

    public static JSONObject constructObjectToAskGameRequest() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.REQUEST_GAME_EVENT, MobiMix.GameEvent.EVENT_GAME_REQUEST_ASK);
        }
        catch (JSONException e) {
            jsonObject = null;
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject constructObjectToSendGameRequest(GameRequest gameRequest) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.GAME_EVENT, MobiMix.GameEvent.EVENT_GAME_REQUEST_ASK);
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

}
