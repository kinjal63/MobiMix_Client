package io.connection.bluetooth.Thread;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
                    this.mSocketConnectionListener.socketConnected(false);
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
        if(message.equalsIgnoreCase(Constants.NO_MODULE)) {

        }
        else if(message.equalsIgnoreCase(Constants.FILESHARING_MODULE)) {
            wifiP2PService.setModule(Modules.FILE_SHARING);
            socketManager.readFiles();
        }
        else if(message.equalsIgnoreCase(Constants.CHAT_MODULE)) {
            wifiP2PService.setModule(Modules.CHAT);
            socketManager.readChatData();
        }
        else if(message.equalsIgnoreCase(Constants.BUSINESSCARD_MODULE)) {
            wifiP2PService.setModule(Modules.BUSINESS_CARD);
            socketManager.readBusinessCard();
        }
        else if(message.startsWith("NowClosing")) {
            socketManager.close();
            socketManager = null;
            wifiP2PService.setModule(Modules.NONE);
            Intent intent = new Intent(context, Home_Master.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
        else {
            int module = wifiP2PService.getModule().ordinal();
            switch (module) {
                case 0:
                    ChatDataConversation.putChatConversation(socketManager.getRemoteDeviceAddress(), ChatDataConversation.getUserName(socketManager.getRemoteDeviceAddress()) + ":  " + message);
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
                    break;
                case 1:
                    String businessCardInfo = message;
                    System.out.println("Business card received :: " + businessCardInfo);
                    break;
                case 2:
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
}
