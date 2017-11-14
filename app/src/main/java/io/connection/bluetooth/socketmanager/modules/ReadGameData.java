package io.connection.bluetooth.socketmanager.modules;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
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
    private InputStream is;
    private MessageHandler handler;

    public ReadGameData(Socket socket, MessageHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    public void readGameEvent() {
        String message;
        JSONObject object;
        try {
            is = socket.getInputStream();
            while (!disable) {
                if (is != null) {
                    try {
                        byte[] buffer = new byte[2048];
                        int bytes = is.read(buffer);
                        if (bytes == -1) {
                            break;
                        }
                        message = new String(buffer);
                        System.out.println("Getting message" + message);
                        object = new JSONObject(message);

                        int arg1 = object.optInt(Constants.GAME_EVENT, 0);
                        if(arg1 != 0) {
                            handler.getHandler().obtainMessage(Constants.MESSAGE_READ_GAME, arg1, -1, object).sendToTarget();
                        }
                    }
                    catch (JSONException e) {
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
