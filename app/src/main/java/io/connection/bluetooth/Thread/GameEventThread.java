package io.connection.bluetooth.Thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by Kinjal on 11/28/2017.
 * This is used to listen game events when requested by remote users
 */

public class GameEventThread extends Thread {
    private UUID MY_UUID_SECURE = null;

    private final BluetoothServerSocket serverSocket;
    private static final String TAG = "GameEventThread";
    BluetoothSocket socket = null;

    Context context;

    public GameEventThread(BluetoothAdapter bluetoothAdapter, Context context) {
        MY_UUID_SECURE = UUID.fromString(ApplicationSharedPreferences.getInstance(context).getValue(Constants.PREF_MY_UUID));

        BluetoothServerSocket tmp = null;
        this.context = context;
        try {

            tmp = bluetoothAdapter
                    .listenUsingRfcommWithServiceRecord(
                            "MyUUID", MY_UUID_SECURE);
        } catch (IOException e) {
        }
        serverSocket = tmp;
    }

    public void run() {
        while (true) {
            try {
                socket = serverSocket.accept();
                BluetoothDevice remoteDevice = socket.getRemoteDevice();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
