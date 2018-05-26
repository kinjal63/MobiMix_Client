package io.connection.bluetooth.socketmanager.modules;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.GameConstants;

/**
 * Created by Kinjal on 10/29/2017.
 */

public class ReadGameData {
    private Socket socket;
    private boolean disable = false;
    private ObjectInputStream ois;
    private MessageHandler handler;

    public ReadGameData(Socket socket, MessageHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    public void readGameEvent() {
        byte[] buffer = new byte[1024];
        JSONObject object;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            while (!disable) {
                if (ois != null) {
                    try {
                        int bytes = ois.read(buffer, 0, buffer.length);
                        if(bytes == -1 ) {
                            continue;
                        }
                        String msg = new String(buffer);
                        System.out.println("Getting message" + msg);

                        object = new JSONObject(msg);

                        int arg1 = object.optInt(GameConstants.GAME_EVENT, 0);
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
            handler.closeSocket();
            disable = true;
        }
    }
}
