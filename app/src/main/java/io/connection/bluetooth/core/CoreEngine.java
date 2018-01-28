package io.connection.bluetooth.core;

import android.os.Message;

import org.json.JSONObject;

import java.util.List;

import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.gui.GUIManager;
import io.connection.bluetooth.utils.GameConstants;

/**
 * Created by Kinjal on 10/26/2017.
 */

public class CoreEngine {
    public static void sendEventToGUI(Message message) {
        GUIManager.getObject().getHandler().obtainMessage(message.arg1,
                -1, -1, message.obj).sendToTarget();
    }

//    public static void sendEventToRadioService(Message message) {
//        JSONObject jsonObject = (JSONObject) message.obj;
//        int gameConnectionType = jsonObject.optInt(GameConstants.GAME_CONNECTION_TYPE);
//        long gameId = jsonObject.optLong(GameConstants.GAME_ID);
//        if( gameConnectionType == 1 ) {
//            BluetoothService.getInstance().sendBluetoothRequestToUser(
//                    (List<MBNearbyPlayer>)jsonObject.opt(GameConstants.GAME_PLAYERS_IN_QUEUE));
//        }
//    }

    public static void sendEventToHandler(EventData eventData) {
        MessageHandler messageHandler = BluetoothService.getInstance().handler();
        if( messageHandler != null ) {
            messageHandler.sendEvent(eventData);
        }
    }
}
