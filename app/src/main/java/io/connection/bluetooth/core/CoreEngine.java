package io.connection.bluetooth.core;

import android.os.Message;

import io.connection.bluetooth.activity.gui.GUIManager;

/**
 * Created by Kinjal on 10/26/2017.
 */

public class CoreEngine {
    public static void sendEventToGUI(Message message) {
        GUIManager.getObject().getHandler().obtainMessage(MobiMix.GUIEvent.EVENT_GAME_REQUEST,
                -1, -1, message.obj).sendToTarget();
    }
}
