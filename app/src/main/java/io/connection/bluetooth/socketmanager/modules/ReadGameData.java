package io.connection.bluetooth.socketmanager.modules;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by Kinjal on 10/29/2017.
 */

public class ReadGameData {
    private Socket socket;
    private boolean disable = false;
    private ObjectInputStream ois;
    private MessageHandler handler;

    public ReadGameData(ObjectInputStream inputStream, MessageHandler handler) {
        this.ois = inputStream;
        this.handler = handler;
    }

    public void readGameEvent() {
        byte[] buffer = new byte[1024];
        JSONObject object;

        try {
            while (!disable) {
                if (ois != null) {
                    try {
                        object = (JSONObject) ois.readObject();
                        if (object == null) {
                            break;
                        }

                        System.out.println("Getting message" + new String(buffer));
                        int arg1 = object.optInt(Constants.GAME_EVENT, 0);
                        handler.getHandler().obtainMessage(Constants.MESSAGE_READ_GAME, arg1, -1, object).sendToTarget();
                    }
                    catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            handler.socketClosed();
        }
    }
}
