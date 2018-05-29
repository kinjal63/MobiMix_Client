package io.connection.bluetooth.socketmanager.modules;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.GameConstants;

/**
 * Created by Kinjal on 4/8/2017.
 */

public class ReadChatData implements Runnable {
    private Socket socket;
    private boolean disable = false;
    private ObjectInputStream ois;
    private MessageHandler handler;

    public ReadChatData(ObjectInputStream socket, MessageHandler handler) {
        this.ois = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        JSONObject object = null;
        try {
//            ois = new ObjectInputStream(socket.getInputStream());
            while (!disable) {
                if (ois != null) {
                    String eventObj = (String)ois.readObject();
                    if (eventObj == null) {
                        break;
                    }
                    System.out.println("Getting message" + eventObj);

                    object = new JSONObject(eventObj);
                    handler.getHandler().obtainMessage(Constants.MESSAGE_READ_CHAT, -1, -1, object).sendToTarget();
                }
            }
        }
        catch (IOException | JSONException | ClassNotFoundException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            disable = true;
        }
    }
}
