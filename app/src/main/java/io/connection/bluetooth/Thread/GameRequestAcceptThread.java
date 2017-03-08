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

import io.connection.bluetooth.activity.Home_Master;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 07/12/16.
 */
public class GameRequestAcceptThread extends Thread {
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a56");

    private final BluetoothServerSocket serverSocket;
    private static final String TAG = "GameRequestAcceptThread";
    BluetoothSocket socket = null;
    Context mContext;

    public GameRequestAcceptThread(BluetoothAdapter adapter, Context context) {
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
                    Log.d(TAG, "run:  connection successfull");
                    Log.d(TAG, "run: " + socket.getRemoteDevice().getName() + "  " +
                            socket.getRemoteDevice().getAddress());
                    ReadInputRequest readInputRequest = new ReadInputRequest(socket, mContext);
                    readInputRequest.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: " + e.getMessage());
            }

        }
    }


}

class ReadInputRequest extends Thread {
    BluetoothSocket bluetoothSocket = null;
    int bufferSize = 1024;
    int bytes;
    byte[] buffer = new byte[8 * bufferSize];
    Context context;

    ReadInputRequest(BluetoothSocket socket, Context mcontext) {
        bluetoothSocket = socket;
        context = mcontext;
    }


    @Override
    public void run() {
        while (bluetoothSocket.isConnected()) {
            try {
                bytes = bluetoothSocket.getInputStream().read(buffer);
                String readMessage = new String(buffer);

                System.out.println("Message ==> " + readMessage);
                buffer = new byte[1024];
                if (readMessage.startsWith("OK_DONE")) {
                    System.out.println("In oK DONE ");
                    bluetoothSocket.close();
                    break;
                } else if (readMessage.startsWith("Request")) {

                    // Home_Master.ReceiveMessage(readMessage);

                    final String abc = readMessage;
                    UtilsHandler.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new Home_Master().ReceiveMessage(abc, bluetoothSocket);
                        }
                    });

                    System.out.println("Request Socket Close ");

                } else if (readMessage.startsWith("Response")) {
                    readMessage.trim();
                    final String[] response = readMessage.split(":");
                    System.out.println(Arrays.toString(response));
                    UtilsHandler.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response[1].trim().contains("1")) {
                                System.out.println(Arrays.toString(response));
                                Toast.makeText(ImageCache.getContext(), response[2].split("\\$\\#\\$")[0].trim() + " User Agree", Toast.LENGTH_LONG).show();
                                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(response[2].split("\\$\\#\\$")[1].trim());
                                try {
                                    context.startActivity(launchIntent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Game Not Found", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(ImageCache.getContext(), response[2].trim() + "  User DisAgree", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    System.out.println("Response Socket Close ");
                    bluetoothSocket.close();

                }


              /*      Log.d(TAG, "run:  Accept Thread Receive Message" + readMessage);
                    ChatDataConversation.putChatConversation(socket.getRemoteDevice().getAddress(), ChatDataConversation.getUserName(socket.getRemoteDevice().getAddress()) + ":  " + readMessage);
                    Log.d(TAG, "run: Accept thread Receive Message Count -> " + ChatDataConversation.getChatConversation(socket.getRemoteDevice().getAddress()).size());
                    DeviceChatActivity.readMessagae(socket.getRemoteDevice());
                    Log.d(TAG, "run: Avaliable   Size after " + socket.getInputStream().available());*/


            } catch (Exception e) {

                try {
                    bluetoothSocket.close();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }

    }
}

