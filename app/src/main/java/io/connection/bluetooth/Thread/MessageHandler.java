package io.connection.bluetooth.Thread;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.Domain.LocalP2PDevice;
import io.connection.bluetooth.Domain.QueueManager;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.activity.ChatDataConversation;
import io.connection.bluetooth.activity.WifiP2PChatActivity;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.core.BluetoothService;
import io.connection.bluetooth.core.CoreEngine;
import io.connection.bluetooth.core.EventData;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.socketmanager.BluetoothSocketManager;
import io.connection.bluetooth.socketmanager.SocketHeartBeat;
import io.connection.bluetooth.socketmanager.WifiSocketManager;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.GameConstants;
import io.connection.bluetooth.utils.LogUtils;
import io.connection.bluetooth.utils.MessageConstructor;
import io.connection.bluetooth.utils.NotificationUtil;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.cache.CacheConstants;
import io.connection.bluetooth.utils.cache.MobiMixCache;

/**
 * Created by KP49107 on 29-03-2017.
 */
public class MessageHandler {
    private Handler handler = null;
    private WifiSocketManager wifiSocketManager;
    private BluetoothSocketManager bluetoothSocketManager;

    private Context context;
    private WifiDirectService wifiP2PService;

    private SocketHeartBeat heartbeat;
    private boolean isSocketConnected = false;
    private String TAG = "MessageHandler";

    public MessageHandler(Context context, WifiDirectService wifiP2PService) {
        this.context = context;
        this.wifiP2PService = wifiP2PService;
        //To receive message on different thread
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

    public void setManager(BluetoothSocketManager socketManager) {
        this.bluetoothSocketManager = socketManager;
    }

    public void handleMessage(Message msg) {
        System.out.println("Message received in handler, message object : " + msg.what);
        switch (msg.what) {
            case Constants.FIRSTMESSAGEXCHANGE:
                closeBluetoothSocket();
                bluetoothSocketManager = null;

                final Object obj = msg.obj;

                Log.d(TAG, "handleMessage, " + Constants.FIRSTMESSAGEXCHANGE + " case");
                wifiSocketManager = (WifiSocketManager) obj;

                String moduleName = getMessageModuleToSend();
                wifiSocketManager.writeObject(moduleName);

                heartbeat = new SocketHeartBeat(wifiSocketManager);
                heartbeat.start();

                break;
            case Constants.FIRSTMESSAGEXCHANGE_BLUETOOTH:
                closeWifiSocket();
                wifiSocketManager = null;

                final Object o = msg.obj;
                Log.d(TAG, "handleMessage, " + Constants.FIRSTMESSAGEXCHANGE + " case");

                bluetoothSocketManager = (BluetoothSocketManager) o;
                JSONObject jsonObject = MessageConstructor.constructObjectToRequestForEvent(MobiMix.GameEvent.EVENT_GAME_START);
                bluetoothSocketManager.writeObject(jsonObject.toString().getBytes());
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
            case Constants.MESSAGE_HEARBEAT:
                if (msg.obj != null) {
                    wifiSocketManager.writeObject(MessageConstructor.getHandShakeSignalObj());
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
        System.out.println("Message handled by actual message received::" + message);

        if (message.startsWith(Constants.NO_MODULE) ||
                message.startsWith(Constants.CHAT_MODULE) ||
                message.startsWith(Constants.FILESHARING_MODULE) ||
                message.startsWith(Constants.BUSINESSCARD_MODULE) ||
                message.startsWith(Constants.GAME_MODULE)) {

            WifiP2PRemoteDevice device = wifiSocketManager.setRemoteDevice(message.split("_")[1], message.split("_")[2]);
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

                EventData eventData = new EventData();
                eventData.event_ = MobiMix.GameEvent.EVENT_CONNECTION_ESTABLISHED_ACK;
                sendEvent(eventData);
            }
            readData();

        } else if (message.startsWith("NowClosing")) {
            closeWifiSocket();
            wifiP2PService.setModule(Modules.NONE);
        } else {
            int module = wifiP2PService.getModule().ordinal();
            switch (module) {
                case 1:
                    ChatDataConversation.putChatConversation(wifiSocketManager.getRemoteDeviceAddress(), ChatDataConversation.getUserName(wifiSocketManager.getRemoteDeviceAddress()) + ":  " + message);
                    Log.d(TAG, "run: Accept thread Receive Message Count -> " + ChatDataConversation.getChatConversation(wifiSocketManager.getRemoteDeviceAddress()).size());
                    WifiP2PChatActivity.readMessagae(wifiSocketManager.getRemoteDeviceAddress());

                    WifiP2PRemoteDevice remoteDevice = wifiSocketManager.getRemoteDevice();

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

    public void sendMessage(byte[] message) {
        if (wifiSocketManager != null) {
            wifiSocketManager.writeMessage(message);
        }
    }

    public void sendBusinessCard() {
        Log.d(TAG, "Starting to write business card");
        if (wifiSocketManager != null) {
            wifiSocketManager.writeBusinessCard();
        }
    }

    public void sendFiles(List<Uri> files) {
        QueueManager.addFilesToSend(files);
        if (wifiSocketManager != null) {
            wifiSocketManager.writeFiles();
        }
    }

    public void readData() {
        if (wifiSocketManager != null) {
            wifiSocketManager.readData();
        }
    }

    public void socketConnected() {
        isSocketConnected = true;
        wifiP2PService.notifyUserForConnectedSocket(wifiSocketManager.getRemoteDeviceAddress());
    }

    public void socketClosed() {
        closeWifiSocket();

        isSocketConnected = false;
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
        if (wifiSocketManager != null) {
            return wifiSocketManager.getRemoteDeviceAddress();
        }
        return null;
    }

    public void closeBluetoothSocket() {
        if (heartbeat != null && !heartbeat.isInterrupted()) {
            heartbeat.interrupt();
        }
        if(bluetoothSocketManager != null)
            bluetoothSocketManager.closeSocket();
    }

    public void closeWifiSocket() {
        if (heartbeat != null && !heartbeat.isInterrupted()) {
            heartbeat.interrupt();
        }

        System.out.println("Closing socket and removing group.");

        if(wifiP2PService != null ) {
            wifiP2PService.setModule(Modules.NONE);
            wifiP2PService.closeConnection();
            wifiP2PService.removeGroup();

            wifiP2PService.removeConnectedDevice();
            wifiP2PService.notifyUserForClosedSocket();
        }

        System.out.println("Removing group for wifidirect");
    }

    private void handleGameObject(Message message) {
        LogUtils.printLog(TAG, "Message handled by handleGameObject Event::" + message.arg1);

        JSONObject object = (JSONObject) message.obj;
        String userId = object.optString(GameConstants.USER_ID);

        EventData eventData = new EventData();
        eventData.userId_ = userId;

        switch (message.arg1) {
            // Send Game Request to notify remote user if connection is established
            case MobiMix.GameEvent.EVENT_CONNECTION_ESTABLISHED_ACK:
                GameRequest gameRequest = MobiMixCache.getGameFromCache(userId);
                if (gameRequest != null) {
                    eventData.event_ = MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST;
                } else {
                    eventData.event_ = MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST_ASK;
                }
                break;
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST_ASK:
                eventData.event_ = MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST;
                break;

            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED_ACK:
                if (Integer.parseInt(MobiMixCache.getFromCache(CacheConstants.CACHE_IS_GROUP_OWNER).toString()) == 1) {
                    eventData.event_ = MobiMix.GameEvent.EVENT_GAME_QUEUED_USER_ASK;
//                    eventData.event_ = MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA;
                } else {
                    eventData.event_ = MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_REQUEST;
                }
                break;
            case MobiMix.GameEvent.EVENT_GAME_QUEUED_USER_ASK:
                eventData.event_ = MobiMix.GameEvent.EVENT_GAME_QUEUED_USER;
                break;
            case MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_REQUEST:
                eventData.event_ = MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA;
                break;
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST:
                eventData.event_ = MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST_ACK;
                CoreEngine.sendEventToGUI(message);
                break;
            case MobiMix.GameEvent.EVENT_GAME_QUEUED_USER:
                sendEventToRadioService(eventData);
                break;
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                CoreEngine.sendEventToGUI(message);
                // checks in queue, if there is any user to send request it will send
                if(Utils.isGroupOwner()) {
                    eventData.event_ = MobiMix.GameEvent.EVENT_GAME_REQUEST_TO_QUEUED_USERS;
                    eventData.object_ = object;
                    sendEventToRadioService(eventData);
                }
                break;
            case MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA:
                CoreEngine.sendEventToGUI(message);
                break;
            default:
                break;
        }
        if (eventData.event_ > 0) {
            sendEvent(eventData);
        }
    }

    public void sendEvent(EventData eventData) {
        System.out.println("Message send by handler : " + eventData.event_);
        JSONObject eventObj = null;

        switch (eventData.event_) {
            case MobiMix.GameEvent.EVENT_CONNECTION_ESTABLISHED_ACK:
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST_ACK:
                eventObj = MessageConstructor.constructObjectToSendAckEvent(eventData.event_);
                break;
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                eventObj = MessageConstructor.constructObjectToSendGameLaunchedEvent(eventData);
                break;
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED_ACK:
                eventObj = MessageConstructor.constructObjectToSendAckEvent(eventData.event_);
                break;
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST:
                eventObj = MessageConstructor.constructObjectToSendGameRequestEvent(eventData);
                break;
            case MobiMix.GameEvent.EVENT_GAME_QUEUED_USER_ASK:
                eventObj = MessageConstructor.constructObjectToRequestForEvent(eventData.event_);
                break;
            case MobiMix.GameEvent.EVENT_GAME_QUEUED_USER:
                eventObj = MessageConstructor.constructObjectToSendQueuedUserEvent(eventData);
                break;
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST_ASK:
            case MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_REQUEST:
                eventObj = MessageConstructor.constructObjectToRequestForEvent(eventData.event_);
                break;
            case MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA:
                eventObj = MessageConstructor.constructObjectToUpdateDBData(eventData);

                //update its own database
                Message msg = new Message();
                msg.arg1 = eventData.event_;
                msg.obj = eventObj;
                CoreEngine.sendEventToGUI(msg);
                break;
            default:
                break;
        }
        if (bluetoothSocketManager != null && eventObj != null) {
            bluetoothSocketManager.writeObject(eventObj);
        }
        if (wifiSocketManager != null && eventObj != null) {
            wifiSocketManager.writeObject(eventObj);
        }
    }

    private void sendEventToRadioService(EventData eventData) {
        int event = eventData.event_;
        JSONObject object = eventData.object_;
        try {
            switch (event) {
                case MobiMix.GameEvent.EVENT_GAME_REQUEST_TO_QUEUED_USERS:
                    if(object != null && object.getInt(GameConstants.GAME_CONNECTION_TYPE) == 1) {
                        BluetoothService.getInstance().handleEvent(event);
                    }
                    else if (object != null && object.getInt(GameConstants.GAME_CONNECTION_TYPE) == 2) {
                        WifiDirectService.getInstance(context).handleEvent(event);
                    }
                    break;
                case MobiMix.GameEvent.EVENT_GAME_QUEUED_USER:
                    if(object != null) {
                        List<MBNearbyPlayer> players = (List<MBNearbyPlayer>)object.opt(GameConstants.GAME_PLAYERS_IN_QUEUE);
                        if(players != null && players.size() > 0) {
                            MobiMixCache.addPlayersInQueueCache(players);
                            int connectionType = MobiMixCache.getCurrentGameRequestFromCache().getConnectionType();
                            if(connectionType == 1) {
                                BluetoothService.getInstance().handleEvent(event);
                            }
                            else if(connectionType == 2) {
                                WifiDirectService.getInstance(context).handleEvent(event);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
