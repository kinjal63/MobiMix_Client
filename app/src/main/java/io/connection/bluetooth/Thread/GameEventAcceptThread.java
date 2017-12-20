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

/**
 * Created by kinjal on 07/12/17.
 */
public class GameEventAcceptThread extends Thread {
    public static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final BluetoothServerSocket serverSocket;
    private static final String TAG = "GameEventAcceptThread";
    private BluetoothSocket socket = null;
    private MessageHandler handler;

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

                    handler = BluetoothService.getInstance().handler();
                    handler.setBluetoothSocket(socket);

                    EventData eventData = new EventData();
                    eventData.event_ = MobiMix.GameEvent.EVENT_CONNECTION_ESTABLISHED_ACK;
                    handler.sendEvent(eventData);

                    ReadGameEventData readData = new ReadGameEventData(socket);
                    readData.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: " + e.getMessage());
            }
        }
    }
}
