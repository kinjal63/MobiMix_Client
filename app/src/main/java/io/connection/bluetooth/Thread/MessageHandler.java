package io.connection.bluetooth.Thread;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
import io.connection.bluetooth.activity.ChatDataConversation;
import io.connection.bluetooth.activity.DeviceChatActivity;
import io.connection.bluetooth.activity.Home_Master;
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
    private SocketConnectionListener mSocketConnectionListener;

    private String TAG = "MessageHandler";

    public MessageHandler(Context context, WifiDirectService service) {
        this.context = context;
        this.wifiP2PService = service;
    }

    public Handler getHandler() {
        return this.handler;
    }

    public void setSocketConnectionListener(SocketConnectionListener socketConnectionListener) {
        this.mSocketConnectionListener = socketConnectionListener;
    }

    public WifiDirectService getWifiP2PService() {
        return wifiP2PService;
    }

    @Override
    public boolean handleMessage(Message msg) {
        System.out.println("Message received in handler, message object : " + msg.what);
        switch (msg.what) {
            case Constants.FIRSTMESSAGEXCHANGE:
                final Object obj = msg.obj;

                Log.d(TAG, "handleMessage, " + Constants.FIRSTMESSAGEXCHANGE + " case");

                socketManager = (SocketManager) obj;
                String moduleName = this.wifiP2PService.getModule() == Modules.FILE_SHARING?
                        Constants.FILESHARING_MODULE : (this.wifiP2PService.getModule() == Modules.BUSINESS_CARD ?
                        Constants.BUSINESSCARD_MODULE : (this.wifiP2PService.getModule() == Modules.CHAT ? Constants.CHAT_MODULE : "None"));

                socketManager.writeMessage(moduleName.getBytes());
                if( this.mSocketConnectionListener != null ) {
                    this.mSocketConnectionListener.socketConnected(true);
                }

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
        System.out.println("Actual message received::" + message);
        if(message.startsWith(Constants.NO_MODULE)) {

        }
        else if(message.startsWith(Constants.FILESHARING_MODULE)) {
            wifiP2PService.setModule(Modules.FILE_SHARING);
            readFiles();
        }
        else if(message.startsWith(Constants.CHAT_MODULE)) {
            wifiP2PService.setModule(Modules.CHAT);
            readChatData();
        }
        else if(message.startsWith(Constants.BUSINESSCARD_MODULE)) {
            wifiP2PService.setModule(Modules.BUSINESS_CARD);
            readBusinessCard();
        }
        else if(message.startsWith("NowClosing")) {
            closeSocket();
            wifiP2PService.setModule(Modules.NONE);
            Intent intent = new Intent(context, Home_Master.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else {
            int module = wifiP2PService.getModule().ordinal();
            switch (module) {
                case 1:
                    ChatDataConversation.putChatConversation(socketManager.getRemoteDeviceAddress(), ChatDataConversation.getUserName(socketManager.getRemoteDeviceAddress()) + ":  " + message);
                    Log.d(TAG, "run: Accept thread Receive Message Count -> "+ChatDataConversation.getChatConversation(socketManager.getRemoteDeviceAddress()).size());
                    WifiP2PChatActivity.readMessagae(socketManager.getRemoteDeviceAddress());

                    UtilsHandler.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WifiP2pDevice device = new WifiP2pDevice();
                            device.deviceName = socketManager.getRemoteDeviceAddress();

                            Intent intent = new Intent(context, WifiP2PChatActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("device", device);
                            context.startActivity(intent);

                        }
                    });
                    break;
                case 2:
                    String businessCardInfo = message;
                    System.out.println("Business card received :: " + businessCardInfo);
                    break;
                case 0:
                    break;
            }
        }
    }

    public void sendMessage(byte[] message) {
        if(socketManager != null) {
            socketManager.writeMessage(message);
        }
    }

    public void sendBusinessCard() {
        if(socketManager != null) {
            socketManager.writeBusinessCard();
        }
    }

    public void sendFiles(List<Uri> files) {
        if(socketManager != null) {
            socketManager.writeFiles(files);
        }
    }

    public void readChatData() {
        if(socketManager != null) {
            socketManager.readChatData();
        }
    }

    public void readBusinessCard() {
        if(socketManager != null) {
            socketManager.readBusinessCard();
        }
    }

    public void readFiles() {
        if(socketManager != null) {
            socketManager.readFiles();
        }
    }

    public void closeSocket() {
        wifiP2PService.closeConnection();
        wifiP2PService.removeGroup();
    }
}
