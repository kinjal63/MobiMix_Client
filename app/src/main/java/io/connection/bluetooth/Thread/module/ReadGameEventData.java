package io.connection.bluetooth.Thread.module;

import android.bluetooth.BluetoothSocket;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.GameConstants;

/**
 * Created by Kinjal on 12/1/2017.
 */

public class ReadGameEventData extends Thread {
    private BluetoothSocket bluetoothSocket = null;
    private Context context = null;
    private InputStream in;
    private boolean disable = false;

    public ReadGameEventData(BluetoothSocket socket, Context context) {
        this.bluetoothSocket = socket;
    }

    @Override
    public void run() {
        String message;
        JSONObject object;
        try {
            in = bluetoothSocket.getInputStream();
            while (!disable) {
                if (in != null) {
                    try {
                        byte[] buffer = new byte[2048];
                        int bytes = in.read(buffer);
                        if (bytes == -1) {
                            break;
                        }
                        message = new String(buffer);
                        System.out.println("Getting message" + message);
                        object = new JSONObject(message);

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
            disable = false;
            handler.socketClosed();
        }
    }
}