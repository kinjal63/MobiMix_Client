package io.connection.bluetooth.Thread;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.util.List;

import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.Domain.LocalP2PDevice;
import io.connection.bluetooth.Domain.QueueManager;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.activity.ChatDataConversation;
import io.connection.bluetooth.activity.WifiP2PChatActivity;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.socketmanager.SocketHeartBeat;
import io.connection.bluetooth.socketmanager.SocketManager;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.NotificationUtil;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by KP49107 on 29-03-2017.
 */
public class MessageHandler {
    private Handler handler = null;
    private Modules module;
    private SocketManager socketManager;

    private Context context;
    private WifiDirectService wifiP2PService;

    private SocketHeartBeat heartbeat;

    private boolean isSocketConnected = false;

    private String TAG = "MessageHandler";

    public MessageHandler(Context context, WifiDirectService service) {
        this.context = context;
        this.wifiP2PService = service;
        //To receive message on differesnt thread
        startHandler();
    }

    private void startHandler() {
        HandlerThread thread = new HandlerThread("MyHandlerThread");
        thread.start();
        handler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                MessageHandler.this.handleMessage(msg);
            }
        };
    }

    public WifiDirectService getWifiP2PService() {
        return wifiP2PService;
    }

    public void handleMessage(Message msg) {
        System.out.println("Message received in handler, message object : " + msg.what);
        switch (msg.what) {
            case Constants.FIRSTMESSAGEXCHANGE:
                final Object obj = msg.obj;

                Log.d(TAG, "handleMessage, " + Constants.FIRSTMESSAGEXCHANGE + " case");

                socketManager = (SocketManager) obj;

                String message = getMessageModuleToSend();
                socketManager.writeMessage(message.getBytes());

                heartbeat = new SocketHeartBeat(socketManager);
                heartbeat.start();

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
    }

    private String getMessageModuleToSend() {
        String moduleName = this.wifiP2PService.getModule() == Modules.FILE_SHARING?
                Constants.FILESHARING_MODULE : (this.wifiP2PService.getModule() == Modules.BUSINESS_CARD ?
                Constants.BUSINESSCARD_MODULE : (this.wifiP2PService.getModule() == Modules.CHAT ? Constants.CHAT_MODULE : "None"));

        if(!UtilsHandler.getGamesFromStack().empty()) {
            final GameRequest gameRequest = UtilsHandler.removeGameFromStack();

            WifiDirectService.getInstance(context).updateGameConnectionAndLaunchGame(gameRequest);
            return Constants.START_GAME_MODULE + "_" + gameRequest.getGamePackageName() + "_" + LocalP2PDevice.getInstance().getLocalDevice().deviceAddress + "_" +
                    LocalP2PDevice.getInstance().getLocalDevice().deviceName;
        }
        else {
            return moduleName + "_" + LocalP2PDevice.getInstance().getLocalDevice().deviceAddress + "_" +
                    LocalP2PDevice.getInstance().getLocalDevice().deviceName;
        }
    }

    private void handleObject(String message) {
        System.out.println("Actual message received::" + message);
        if(message.startsWith(Constants.START_GAME_MODULE)) {
            JSONObject jsonObject = new JSONObject(message);
            
            sendEventToGUI();
            String gamePackageName = message.split("_")[1];
            UtilsHandler.generateNotification();
            UtilsHandler.launchGame(gamePackageName);
        }
        else if(message.startsWith(Constants.NO_MODULE) ||
                message.startsWith(Constants.CHAT_MODULE) ||
                message.startsWith(Constants.FILESHARING_MODULE) ||
                message.startsWith(Constants.BUSINESSCARD_MODULE)) {

            socketManager.setRemoteDevice(message.split("_")[1], message.split("_")[2]);
            socketConnected();

            if (message.startsWith(Constants.FILESHARING_MODULE)) {
                wifiP2PService.setModule(Modules.FILE_SHARING);
                readFiles();
            } else if (message.startsWith(Constants.CHAT_MODULE)) {
                wifiP2PService.setModule(Modules.CHAT);
                readChatData();
            } else if (message.startsWith(Constants.BUSINESSCARD_MODULE)) {
                wifiP2PService.setModule(Modules.BUSINESS_CARD);
                readBusinessCard();
            }
        }
        else if(message.startsWith("NowClosing")) {
            closeSocket();
//            if(wifiP2PService.getModule().ordinal() == 1) {
//                Intent intent = new Intent(context, Home_Master.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//            }
            wifiP2PService.setModule(Modules.NONE);
        }
        else {
            int module = wifiP2PService.getModule().ordinal();
            switch (module) {
                case 1:
                    ChatDataConversation.putChatConversation(socketManager.getRemoteDeviceAddress(), ChatDataConversation.getUserName(socketManager.getRemoteDeviceAddress()) + ":  " + message);
                    Log.d(TAG, "run: Accept thread Receive Message Count -> "+ChatDataConversation.getChatConversation(socketManager.getRemoteDeviceAddress()).size());
                    WifiP2PChatActivity.readMessagae(socketManager.getRemoteDeviceAddress());

                    WifiP2PRemoteDevice remoteDevice = socketManager.getRemoteDevice();

                    Intent intent = new Intent(MobiMixApplication.getInstance().getActivity(), WifiP2PChatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("device", remoteDevice);

                    NotificationUtil.sendChatNotification(intent, message, remoteDevice.getName());

//                    UtilsHandler.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            WifiP2pDevice device = new WifiP2pDevice();
//                            device.deviceName = socketManager.getRemoteDeviceAddress();
//
//                            Intent intent = new Intent(context, WifiP2PChatActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            intent.putExtra("device", device);
//                            intent.putExtra("remoteDeviceAddress", socketManager.getRemoteDeviceAddress());
//                            context.startActivity(intent);
//
//                        }
//                    });
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
        Log.d(TAG, "Starting to write business card");
        if(socketManager != null) {
            socketManager.writeBusinessCard();
        }
    }

    public void sendFiles(List<Uri> files) {
        QueueManager.addFilesToSend(files);
        if(socketManager != null) {
            socketManager.writeFiles();
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

    public void socketConnected() {
        isSocketConnected = true;
        wifiP2PService.notifyUserForConnectedSocket(socketManager.getRemoteDeviceAddress());
    }

    public void socketClosed() {
        closeSocket();

        isSocketConnected = false;
        wifiP2PService.notifyUserForClosedSocket();
    }

    public void setModule(Modules module) {
        wifiP2PService.setModule(module);
    }

    public Handler getHandler() {
        return this.handler;
    }

    public boolean isSocketConnected() {
        return this.isSocketConnected;
    }

    public String getRemoteDeviceAddress() {
        if(socketManager != null) {
            return socketManager.getRemoteDeviceAddress();
        }
        return null;
    }

    public void closeSocket() {
//        sendMessage(new String("NowClosing").getBytes());
//        try {
//            Thread.sleep(1500);
//        }catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        if( heartbeat!= null && !heartbeat.isInterrupted() ) {
            heartbeat.interrupt();
        }

        System.out.println("Closing socket and removing group.");

        wifiP2PService.setModule(Modules.NONE);
        wifiP2PService.closeConnection();
        wifiP2PService.removeGroup();

        System.out.println("Removing group for wifidirect");
    }
}
