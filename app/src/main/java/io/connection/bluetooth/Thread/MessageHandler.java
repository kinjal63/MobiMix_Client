package io.connection.bluetooth.Thread;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.activity.ChatDataConversation;
import io.connection.bluetooth.activity.DeviceChatActivity;
import io.connection.bluetooth.activity.WifiP2PChatActivity;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.socketmanager.SocketManager;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by KP49107 on 29-03-2017.
 */
public class MessageHandler implements Handler.Callback {
    private Handler handler = new Handler(this);
    private Modules module;
    private SocketManager socketManager;

    private Context context;
    private WifiDirectService wifiP2PService;

    private String TAG = "MessageHandler";

    public MessageHandler(Context context, WifiDirectService service) {
        this.context = context;
        this.wifiP2PService = service;
    }

    public Handler getHandler() {
        return this.handler;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.FIRSTMESSAGEXCHANGE:
                final Object obj = msg.obj;

                Log.d(TAG, "handleMessage, " + Constants.FIRSTMESSAGEXCHANGE + " case");

                socketManager = (SocketManager) obj;
                socketManager.write(this.wifiP2PService.getModule().name().getBytes());

                break;
            case Constants.MESSAGE_READ:
                if(msg.obj != null) {
                    byte[] buf = (byte[])msg.obj;
                    handleObject(new String(buf));
                }
                break;
            default:
                break;
        }

        return true;
    }

    private void handleObject(String message) {
        String str[] = message.split("_");
        if( str[0].equalsIgnoreCase("0") ) {
            String readMessage = new String(str[1]);

            if(readMessage.startsWith("NOWweArECloSing")){
                socketManager.close();
            }
            Log.d(TAG, "run:  Accept Thread Receive Message"+readMessage);
            ChatDataConversation.putChatConversation(socketManager.getRemoteDeviceAddress(), ChatDataConversation.getUserName(socketManager.getRemoteDeviceAddress()) + ":  " + readMessage);
            Log.d(TAG, "run: Accept thread Receive Message Count -> "+ChatDataConversation.getChatConversation(socketManager.getRemoteDeviceAddress()).size());
            WifiP2PChatActivity.readMessagae(socketManager.getRemoteDeviceAddress());

            UtilsHandler.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WifiP2pDevice device = new WifiP2pDevice();
                    device.deviceName = socketManager.getRemoteDeviceAddress();

                    Intent intent = new Intent(context, WifiP2PChatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("device", device);
                    context.startActivity(intent);

                }
            });
        }
        else if( str[0].equalsIgnoreCase("1") ) {

        }
        else if( str[0].equalsIgnoreCase("2") ) {

        }
    }

    public void sendMessage(byte[] message) {
        if(socketManager != null) {
            socketManager.write(message);
        }
    }
}
