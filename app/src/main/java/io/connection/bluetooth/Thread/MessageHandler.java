package io.connection.bluetooth.Thread;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
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
                socketManager.write(this.wifiP2PService.getModule().name().getBytes());

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
        final String str[] = message.split("_");
        System.out.println("Actual message received::" + str[0]);
        if( str[0].equalsIgnoreCase("1") ) {
            String readMessage = new String(str[1]);

            Log.d(TAG, "run:  Accept Thread Receive Message"+readMessage);
            ChatDataConversation.putChatConversation(socketManager.getRemoteDeviceAddress(), ChatDataConversation.getUserName(socketManager.getRemoteDeviceAddress()) + ":  " + readMessage);
            Log.d(TAG, "run: Accept thread Receive Message Count -> "+ChatDataConversation.getChatConversation(socketManager.getRemoteDeviceAddress()).size());
            WifiP2PChatActivity.readMessagae(socketManager.getRemoteDeviceAddress());

            UtilsHandler.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, str[1], Toast.LENGTH_LONG).show();

//                    WifiP2pDevice device = new WifiP2pDevice();
//                    device.deviceName = socketManager.getRemoteDeviceAddress();
//
//                    Intent intent = new Intent(context, WifiP2PChatActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    intent.putExtra("device", device);
//                    context.startActivity(intent);

                }
            });
        }
        else if( str[0].equalsIgnoreCase("0") ) {

        }
        else if( str[0].equalsIgnoreCase("2") ) {
            final String businessCardInfo = new String(str[1]);
            System.out.println("Business card received :: " + businessCardInfo);

            UtilsHandler.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, businessCardInfo, Toast.LENGTH_LONG).show();
                }
            });
        }
        else if(str[0].startsWith("NowClosing")) {
            socketManager.close();
            socketManager = null;
        }
    }

    public void sendMessage(byte[] message) {
        if(socketManager != null) {
            socketManager.write(message);
        }
    }

    public void sendBusinessCard() {
        if(socketManager != null) {
            SharedPreferences prefs = context.getSharedPreferences("businesscard", Context.MODE_PRIVATE);

            String name = prefs.getString("name", "");
            String email = prefs.getString("email", "");
            String phone = prefs.getString("phone", "");
            String picture = prefs.getString("picture", "");
            String deviceId = prefs.getString("device_id", "");

            String businessCardInfo = Modules.BUSINESS_CARD.ordinal() + "_" + name + ",\n" + email + ",\n" + phone
                    + ",\n" + picture + ",\n" + deviceId;

            System.out.println("Business card info->" + businessCardInfo);
            socketManager.write(businessCardInfo.getBytes());
        }
    }
}
