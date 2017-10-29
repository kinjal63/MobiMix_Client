package io.connection.bluetooth.utils;

import org.json.JSONException;
import org.json.JSONObject;

import io.connection.bluetooth.core.MobiMix;

/**
 * Created by Kinjal on 10/29/2017.
 */

public class MessageConstructor {

    public static JSONObject constructObjectToRequestGameData() {
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
}
