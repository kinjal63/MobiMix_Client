package io.connection.bluetooth.Thread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.socketmanager.SocketManager;
import io.connection.bluetooth.utils.Constants;

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

    public MessageHandler(Context context, WifiDirectService WifiP2PService) {
        this.context = context;
        this.wifiP2PService = wifiP2PService;
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
                    handleObject(msg.obj.toString());
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
