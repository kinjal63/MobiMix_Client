package io.connection.bluetooth.Thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import io.connection.bluetooth.Thread.module.ReadGameEventData;

/**
 * Created by songline on 07/12/16.
 */
public class GameEventConnectThread extends Thread {
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a56");

    private BluetoothSocket mmSocket;
    private static final String TAG = "GameRequestConnThread";

    public GameEventConnectThread(BluetoothDevice device, UUID deviceUUID) {
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(deviceUUID);
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

            ReadGameEventData gameData = new ReadGameEventData(mmSocket);
            gameData.start();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                mmSocket.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }
}
