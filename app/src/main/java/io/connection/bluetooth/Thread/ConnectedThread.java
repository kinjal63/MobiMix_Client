package io.connection.bluetooth.Thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.UUID;

import io.connection.bluetooth.Services.BluetoothService;
import io.connection.bluetooth.activity.DeviceChatActivity;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 07/08/16.
 */
public class ConnectedThread extends Thread implements Serializable {
    private final BluetoothDevice device;
    private final BluetoothSocket mmSocket;
    private static final String TAG = "ConnectedThread";
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static InputStream inputStream;
    private static final long serialVersionUID = 1L;

    public transient Context context;

    public ConnectedThread(BluetoothDevice device) {
        this.context = ImageCache.getContext();
        this.device = device;

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
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager.getAdapter().isEnabled()) {
            manager.getAdapter().cancelDiscovery();
        }
        try {
            Log.d(TAG, "run: connected  " + mmSocket.isConnected());
            mmSocket.connect();
            BluetoothService.getInstance().addSocketConnectionForAddress(device.getAddress());
        } catch (IOException connectException) {
            BluetoothService.getInstance().removeSocketConnection();
            this.interrupt();
            connectException.printStackTrace();
            Log.d(TAG, "run: " + connectException.getMessage());
            return;
        }
        Log.d(TAG, "run: is- connected " + mmSocket.isConnected());
    }

    public void sendMessage(byte[] message) {
        try {
            if (mmSocket.isConnected()) {
                mmSocket.getOutputStream().write(message);
                mmSocket.getOutputStream().flush();
            } else {
                UtilsHandler.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Device Not Connected", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {

//            DeviceChatActivity.disconnectedChat(device.getAddress());
            try {
                mmSocket.close();
                this.interrupt();
                BluetoothService.getInstance().removeSocketConnection();
                Log.d(TAG, "cancel: Socket Close ");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();


        }
    }

    public void cancel() {
        try {
            mmSocket.close();
            Log.d(TAG, "cancel: Socket Close ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class ReadingFile extends AsyncTask<Void, Integer, Void> {

    private static final String TAG = "SendingFile";
    final BluetoothSocket socket;
    private final Context context;
    final BluetoothDevice device;


    ReadingFile(BluetoothSocket socket, final Context context, BluetoothDevice device) {
        this.socket = socket;
        this.context = context;
        this.device = device;
    }


    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        try {
            Log.d(TAG, "onPostExecute: socket closed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onPostExecute: " + e.getMessage());
        }
        super.onPostExecute(aVoid);


    }


}




