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

import io.connection.bluetooth.Domain.LocalP2PDevice;
import io.connection.bluetooth.Domain.QueueManager;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.activity.ChatDataConversation;
import io.connection.bluetooth.activity.WifiP2PChatActivity;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.core.CoreEngine;
import io.connection.bluetooth.core.EventData;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.socketmanager.SocketHeartBeat;
import io.connection.bluetooth.socketmanager.SocketManager;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.LogUtils;
import io.connection.bluetooth.utils.MessageConstructor;
import io.connection.bluetooth.utils.NotificationUtil;

/**
 * Created by KP49107 on 29-03-2017.
 */
public class MessageHandler {
    private Handler handler = null;
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

                String moduleName = getMessageModuleToSend();
                socketManager.writeObject(moduleName);

                heartbeat = new SocketHeartBeat(socketManager);
                heartbeat.start();

                break;

            case Constants.MESSAGE_READ:
                if (msg.obj != null) {
                    byte[] buf = (byte[]) msg.obj;
                    handleObject(new String(buf));
                }
                break;
            case Constants.MESSAGE_READ_GAME:
                if (msg.obj != null) {
                    handleGameObject(msg);
                }
                break;
            default:
                break;
        }
    }

    private String getMessageModuleToSend() {
        String moduleName = this.wifiP2PService.getModule().getModuleName();
//        if(moduleName.equalsIgnoreCase(Modules.GAME.getModuleName())) {
//            readData();
//        }
        return moduleName + "_" + LocalP2PDevice.getInstance().getLocalDevice().deviceAddress + "_" +
                LocalP2PDevice.getInstance().getLocalDevice().deviceName;
    }

    private void handleObject(String message) {
        System.out.println("Actual message received::" + message);

        if (message.startsWith(Constants.NO_MODULE) ||
                message.startsWith(Constants.CHAT_MODULE) ||
                message.startsWith(Constants.FILESHARING_MODULE) ||
                message.startsWith(Constants.BUSINESSCARD_MODULE) ||
                message.startsWith(Constants.GAME_MODULE)) {

            WifiP2PRemoteDevice device = socketManager.setRemoteDevice(message.split("_")[1], message.split("_")[2]);
            wifiP2PService.addConnectedDevice(device);
            socketConnected();

            // Enable read module after receiving module in First Message

            if (message.startsWith(Constants.FILESHARING_MODULE)) {
                wifiP2PService.setModule(Modules.FILE_SHARING);
            } else if (message.startsWith(Constants.CHAT_MODULE)) {
                wifiP2PService.setModule(Modules.CHAT);
            } else if (message.startsWith(Constants.BUSINESSCARD_MODULE)) {
                wifiP2PService.setModule(Modules.BUSINESS_CARD);
            } else if (message.startsWith(Constants.GAME_MODULE)) {
                wifiP2PService.setModule(Modules.GAME);

                JSONObject object = MessageConstructor.constructObjectToSendEvent(MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST);
                if(object != null) {
                    socketManager.writeObject(object);
                }
            }
            readData();

        } else if (message.startsWith("NowClosing")) {
            closeSocket();
            wifiP2PService.setModule(Modules.NONE);
        } else {
            int module = wifiP2PService.getModule().ordinal();
            switch (module) {
                case 1:
                    ChatDataConversation.putChatConversation(socketManager.getRemoteDeviceAddress(), ChatDataConversation.getUserName(socketManager.getRemoteDeviceAddress()) + ":  " + message);
                    Log.d(TAG, "run: Accept thread Receive Message Count -> " + ChatDataConversation.getChatConversation(socketManager.getRemoteDeviceAddress()).size());
                    WifiP2PChatActivity.readMessagae(socketManager.getRemoteDeviceAddress());

                    WifiP2PRemoteDevice remoteDevice = socketManager.getRemoteDevice();

                    Intent intent = new Intent(MobiMixApplication.getInstance().getActivity(), WifiP2PChatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("device", remoteDevice);

                    NotificationUtil.sendChatNotification(intent, message, remoteDevice.getName());
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

    private void handleGameObject(Message message) {
        LogUtils.printLog(TAG, "handleGameObject Event::" + message.arg1);
        switch (message.arg1) {
            // Send Game Info if requested by remote user after connection established
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST:
                if(socketManager != null) {
                    socketManager.writeObject(MessageConstructor.constructObjectToSendGameRequest());
                }
                break;
            case MobiMix.GameEvent.EVENT_GAME_INFO_RESPONSE:
                CoreEngine.sendEventToGUI(message);
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                CoreEngine.sendEventToGUI(message);
            default:
                break;
        }
    }

    public void sendMessage(byte[] message) {
        if (socketManager != null) {
            socketManager.writeMessage(message);
        }
    }

    public void sendBusinessCard() {
        Log.d(TAG, "Starting to write business card");
        if (socketManager != null) {
            socketManager.writeBusinessCard();
        }
    }

    public void sendFiles(List<Uri> files) {
        QueueManager.addFilesToSend(files);
        if (socketManager != null) {
            socketManager.writeFiles();
        }
    }

    public void readData() {
        if (socketManager != null) {
            socketManager.readData();
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
        if (socketManager != null) {
            return socketManager.getRemoteDeviceAddress();
        }
        return null;
    }

    public void closeSocket() {
        if (heartbeat != null && !heartbeat.isInterrupted()) {
            heartbeat.interrupt();
        }

        System.out.println("Closing socket and removing group.");

        wifiP2PService.setModule(Modules.NONE);
        wifiP2PService.closeConnection();
        wifiP2PService.removeGroup();

        wifiP2PService.removeConnectedDevice(socketManager.getRemoteDevice());

        System.out.println("Removing group for wifidirect");
    }

    public void sendEvent(EventData eventData) {
        JSONObject eventObj = null;
        switch (eventData.event_) {
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                eventObj = MessageConstructor.getEventGameLaunchedObject(eventData.userId_);
                break;
            default:
                break;
        }
        if(socketManager != null && eventObj != null) {
            socketManager.writeObject(eventObj);
        }
    }
}
