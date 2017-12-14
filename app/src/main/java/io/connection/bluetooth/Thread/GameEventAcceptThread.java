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
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by kinjal on 07/12/17.
 */
public class GameEventAcceptThread extends Thread {
    private UUID MY_UUID_SECURE;
    private final BluetoothServerSocket serverSocket;
    private static final String TAG = "GameEventAcceptThread";
    private BluetoothSocket socket = null;
    private MessageHandler handler;

    public GameEventAcceptThread(Context context, BluetoothAdapter adapter) {
        handler = BluetoothService.getInstance().handler();

        MY_UUID_SECURE = UUID.fromString(ApplicationSharedPreferences.getInstance(context).getValue(Constants.PREF_MY_UUID));
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
