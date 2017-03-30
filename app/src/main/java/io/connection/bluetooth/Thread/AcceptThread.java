package io.connection.bluetooth.Thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.connection.bluetooth.activity.BusinessCardReceivedList;
import io.connection.bluetooth.activity.ChatDataConversation;
import io.connection.bluetooth.activity.DeviceChatActivity;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 01/08/16.
 */
public class AcceptThread extends Thread {
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private final BluetoothServerSocket serverSocket;
    private static final String TAG = "AcceptThread";
    BluetoothSocket socket = null;

    Context context;

    public AcceptThread(BluetoothAdapter bluetoothAdapter, Context context) {
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

                if (socket.isConnected()) {
                    Log.d(TAG, "run:  connection successfull");
                    Log.d(TAG, "run: " + socket.getRemoteDevice().getName() + "  " +
                            socket.getRemoteDevice().getAddress());
                    readFile readfile = new readFile(socket);
                    readfile.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: " + e.getMessage());
            }

        }
    }

}

class readFile extends Thread {
    private static final String TAG = "readFile";
    BluetoothSocket socket = null;
    BluetoothDevice device = null;
    InputStream in = null;
    OutputStream out = null;
    Context context;
    byte[] buffer = new byte[1024];
    int bytes;


    readFile(BluetoothSocket socket) {
        this.socket = socket;
        this.context = ImageCache.getContext();
    }

    @Override
    public void run() {
        while (socket.isConnected() ) {

            try {
                Log.d(TAG, "run: Avaliable   Size  before" +socket.getInputStream().available());
                bytes = socket.getInputStream().read(buffer);
                String readMessage = new String(buffer);
                buffer=new byte[1024];
                if(readMessage.startsWith("NOWweArECloSing")){
                    socket.close();
                    break;
                }
                Log.d(TAG, "run:  Accept Thread Receive Message"+readMessage);
                ChatDataConversation.putChatConversation(socket.getRemoteDevice().getAddress(), ChatDataConversation.getUserName(socket.getRemoteDevice().getAddress()) + ":  " + readMessage);
                Log.d(TAG, "run: Accept thread Receive Message Count -> "+ChatDataConversation.getChatConversation(socket.getRemoteDevice().getAddress()).size());
                DeviceChatActivity.readMessagae(socket.getRemoteDevice());
                Log.d(TAG, "run: Avaliable   Size after " +socket.getInputStream().available());
            } catch (Exception e) {
                try {
                    socket.close();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                e.printStackTrace();
                break;
            }
            finally {
                try {
                    socket.close();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

            UtilsHandler.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(context, DeviceChatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("device", device);
                    context.startActivity(intent);

                }
            });
        }

    }


}




