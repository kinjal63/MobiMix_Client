package io.connection.bluetooth.Thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import io.connection.bluetooth.Thread.module.ReadGameEventData;
import io.connection.bluetooth.core.BluetoothService;
import io.connection.bluetooth.core.EventData;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.socketmanager.BluetoothSocketManager;
import io.connection.bluetooth.socketmanager.WifiSocketManager;

/**
 * Created by kinjal on 07/12/17.
 */
public class GameEventAcceptThread extends Thread {
    public static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final BluetoothServerSocket serverSocket;
    private static final String TAG = "GameEventAcceptThread";
    private BluetoothSocket socket = null;
    private BluetoothSocketManager bluetoothSocketManager = null;

    public GameEventAcceptThread(BluetoothAdapter adapter) {
        BluetoothServerSocket tmp = null;
        try {
            tmp = adapter
                    .listenUsingRfcommWithServiceRecord(
                            "MyUUID", MY_UUID_SECURE);
        } catch (IOException e) {
        }
        serverSocket = tmp;
    }

    @Override
    public void run() {
        while (true) {
            try {
                socket = serverSocket.accept();

                if (socket.isConnected()) {
                    Log.d(TAG, "run:  connection successful");
                    Log.d(TAG, "run: " + socket.getRemoteDevice().getName() + "  " +
                            socket.getRemoteDevice().getAddress());

                    bluetoothSocketManager = new BluetoothSocketManager(socket);
                    new Thread(bluetoothSocketManager).start();

                    Thread.sleep(1000);
                    bluetoothSocketManager.sendConnectionEstablishedEvent();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Log.d(TAG, "run: " + e.getMessage());
            }
        }
    }
}
