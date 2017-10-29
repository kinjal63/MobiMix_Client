package io.connection.bluetooth.socketmanager.modules;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Map;

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

    public ReadGameData(Socket socket, MessageHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    public void readGameEvent() {
        byte[] buffer = new byte[1024];
        Map<String, String> objectMap;

        try {
            ois = new ObjectInputStream(socket.getInputStream());
            while (!disable) {
                if (ois != null) {
                    try {
                        objectMap = (Map) ois.readObject();
                        if (objectMap == null) {
                            break;
                        }

                        System.out.println("Getting message" + new String(buffer));
                        handler.getHandler().obtainMessage(Constants.MESSAGE_READ_GAME, 0, -1, objectMap).sendToTarget();
                    }
                    catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
