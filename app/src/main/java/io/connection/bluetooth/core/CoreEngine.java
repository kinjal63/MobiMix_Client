package io.connection.bluetooth.core;

import android.content.Context;
import android.os.Message;

import org.json.JSONObject;

import java.util.List;

import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.gui.GUIManager;
import io.connection.bluetooth.enums.RadioType;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.GameConstants;

/**
 * Created by Kinjal on 10/26/2017.
 */

public class CoreEngine {
    public static void sendEventToGUI(Message message) {
        GUIManager.getObject().getHandler().obtainMessage(message.arg1,
                -1, -1, message.obj).sendToTarget();
    }

    public static void sendEventToHandler(EventData eventData) {
        MessageHandler messageHandler = BluetoothService.getInstance().handler();
        if( messageHandler != null ) {
            messageHandler.sendEvent(eventData);
        }
    }

    public static void sendEventToRadioService(EventData eventData) {
        if(eventData.radioType_ == RadioType.WIFI_DIRECT) {
            WifiDirectService.getInstance(MobiMixApplication.getInstance().getContext()).handleEvent(eventData);
        }
        else if(eventData.radioType_ == RadioType.BLUETOOTH) {
            BluetoothService.getInstance().handleEvent(eventData);
        }
    }
}
