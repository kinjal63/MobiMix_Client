package io.connection.bluetooth.socketmanager.modules;

import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by Kinjal on 4/8/2017.
 */

public class ReadChatData {
    private Socket socket;
    private boolean disable = false;
    private InputStream is;
    private MessageHandler handler;

    public ReadChatData(Socket socket, MessageHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    public void readChatData() {
        byte[] buffer = new byte[1024];
        int bytes;
        try {
            is = socket.getInputStream();
            while (!disable) {

                if (is != null) {
                    bytes = is.read(buffer);
                    if (bytes == -1) {
                        break;
                    }

                    System.out.println("Getting message" + new String(buffer));
                    handler.getHandler().obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
