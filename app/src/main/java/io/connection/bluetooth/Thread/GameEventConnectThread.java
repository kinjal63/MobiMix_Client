package io.connection.bluetooth.Thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import io.connection.bluetooth.activity.ImageCache;

/**
 * Created by songline on 07/12/16.
 */
public class GameEventConnectThread extends Thread {
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a56");

    final BluetoothSocket mmSocket;
    private static final String TAG = "GameRequestConnThread";
    BluetoothSocket socket = null;
    Context mContext;
    final BluetoothDevice device;
    String name;
    String gameName;
    int flag;
    String Response;

    public void setGame(String name, String gameName) {
        this.name = name;
        this.gameName = gameName;
    }

    public void setResponse(String response) {
        Response = response;
    }


    public GameEventConnectThread(BluetoothDevice device, int flag) {
        mContext = ImageCache.getContext();
        this.device = device;
        this.flag = flag;


        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);

        } catch (IOException e) {

            Log.d(TAG, "ConnectThread: " + e.getMessage());
        }
        mmSocket = tmp;


    }


    @Override
    public void run() {
        try {
            if (!mmSocket.isConnected())
                mmSocket.connect();
            if (flag == 1)
                sendGameRequest(name, gameName);
            else if (flag == 0) {
                sendResponse(Response);
            }

            while (mmSocket.isConnected()) {

            }
            mmSocket.close();
            System.out.println("Socket Close " + TAG);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                mmSocket.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }

        }
    }

    public void sendGameRequest(String name, String gameName) {

        if (mmSocket.isConnected()) {
            try {
                mmSocket.getOutputStream().write(("Request:" + gameName + ":" + name).getBytes());

                try {
                    Thread.sleep(3000);
                    mmSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // mmSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public void sendResponse(String response) {
        if (mmSocket.isConnected()) {
            try {
                mmSocket.getOutputStream().write((response).getBytes());
                try {
                    Thread.sleep(3000);
                    mmSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //  mmSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
