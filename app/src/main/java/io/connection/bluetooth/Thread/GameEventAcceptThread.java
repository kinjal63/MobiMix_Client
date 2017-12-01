package io.connection.bluetooth.Thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import io.connection.bluetooth.Thread.module.ReadGameEventData;
import io.connection.bluetooth.activity.Home_Master;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 07/12/16.
 */
public class GameEventAcceptThread extends Thread {
    private UUID MY_UUID_SECURE;

    private final BluetoothServerSocket serverSocket;
    private static final String TAG = "GameEventAcceptThread";
    BluetoothSocket socket = null;
    Context mContext;

    public GameEventAcceptThread(BluetoothAdapter adapter, Context context) {
        MY_UUID_SECURE = UUID.fromString(ApplicationSharedPreferences.getInstance(context).getValue(Constants.PREF_MY_UUID));
        BluetoothServerSocket tmp = null;
        mContext = context;
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
                    ReadGameEventData readData = new ReadGameEventData(socket, mContext);
                    readData.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: " + e.getMessage());
            }

        }
    }
}
