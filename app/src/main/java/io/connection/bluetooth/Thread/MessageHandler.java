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
import java.util.Timer;
import java.util.TimerTask;

import io.connection.bluetooth.Database.entity.MBGameInfo;
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

                final JSONObject obj = (JSONObject)msg.obj;

                this.wifiSocketManager = (WifiSocketManager)obj.opt("wifi_socket_manager");
                String socketAddr = obj.optString("wifi_socket_client");

                Log.d(TAG, "handleMessage, " + Constants.FIRSTMESSAGEXCHANGE + " case");
//                wifiSocketManager = (WifiSocketManager) obj;

                String moduleName = getMessageModuleToSend();
                this.wifiSocketManager.writeObject(socketAddr, moduleName);

                heartbeat = new SocketHeartBeat(wifiSocketManager);
                heartbeat.start();

                break;
            case Constants.FIRSTMESSAGEXCHANGE_BLUETOOTH:
                closeWifiSocket();
                wifiSocketManager = null;

                final Object o = msg.obj;
                Log.d(TAG, "handleMessage, " + Constants.FIRSTMESSAGEXCHANGE + " case");

//                bluetoothSocketManager = (BluetoothSocketManager) o;
//                JSONObject jsonObject = MessageConstructor.constructObjectToRequestForEvent(MobiMix.GameEvent.EVENT_GAME_START);
//                bluetoothSocketManager.writeObject(jsonObject.toString().getBytes());
                break;

            case Constants.MESSAGE_READ:
                if (msg.obj != null) {
                    byte[] buf = (byte[]) msg.obj;
                    handleObject(new String(buf), null);
                }
                break;
            case Constants.MESSAGE_READ_CHAT:
                if(msg.obj != null) {
                    JSONObject jsonObject = (JSONObject)msg.obj;
                    String chatMessage = jsonObject.optString(GameConstants.CHAT_MESSAGE);
                    String socketAddress = jsonObject.optString(GameConstants.CLIENT_SOCKET_ADDRESS);

                    handleObject(chatMessage, socketAddress);
                }
                break;
            case Constants.MESSAGE_READ_BUSINESS_CARD:
                break;
            case Constants.MESSAGE_READ_GAME:
                if (msg.obj != null) {
                    handleGameObject(msg);
                }
                break;
            case Constants.MESSAGE_HEARBEAT:
                if (msg.obj != null) {
                    wifiSocketManager.sendToAll(MessageConstructor.getHandShakeSignalObj());
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

    private void handleObject(String message, String socketAddress) {
        System.out.println("Message handled by actual message received::" + message);

        if (message.startsWith(Constants.NO_MODULE) ||
                message.startsWith(Constants.CHAT_MODULE) ||
                message.startsWith(Constants.FILESHARING_MODULE) ||
                message.startsWith(Constants.BUSINESSCARD_MODULE) ||
                message.startsWith(Constants.GAME_MODULE)) {

            WifiP2PRemoteDevice device = wifiSocketManager.setRemoteDevice(message.split("_")[1], message.split("_")[2]);
            wifiP2PService.addConnectedDevice(device);
            socketConnected();

            // Send Socket initialization and disconnection events to GUI
            Message msg = new Message();
            msg.arg1 = MobiMix.GameEvent.EVENT_SOCKET_INITIALIZED;
            try {
                msg.obj = new JSONObject().put(GameConstants.CLIENT_SOCKET_ADDRESS, wifiSocketManager.getConnectedSocketInetAddress());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            CoreEngine.sendEventToGUI(msg);

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
                if (!Utils.isGroupOwner()) {
                    eventData.socketAddr_ = wifiSocketManager.getConnectedSocketInetAddress();
                } else {
                    startTimerToCheckAllUsersAreConnected();
                }
                if(MobiMixCache.getCurrentGameRequestFromCache() == null) {
                    sendEvent(eventData);
                }
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
                    intent.putExtra("socketAddress", socketAddress);
                    intent.putExtra("isNeedToReconnect", false);

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

    public void sendMessage(String socketAddress, Object object) {
        if (wifiSocketManager != null) {
            wifiSocketManager.writeObject(socketAddress, object);
        }
    }

    public void sendBusinessCard(String socketAddress, Modules module) {
        if (wifiSocketManager != null) {
            wifiSocketManager.writeFileObject(socketAddress, module);
        }
    }

    public void sendBusinessCard() {
        Log.d(TAG, "Starting to write business card");
        if (wifiSocketManager != null) {
            wifiSocketManager.writeBusinessCard();
        }
    }

    public void sendFiles(String socketAddress, Modules module, List<Uri> files) {
        QueueManager.addFilesToSend(files);
        if (wifiSocketManager != null) {
            wifiSocketManager.writeFileObject(socketAddress, module);
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

    public void closeSocket() {
        wifiP2PService.closeConnection();
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
        if (bluetoothSocketManager != null)
            bluetoothSocketManager.closeSocket();
    }

    public void closeWifiSocket() {
        isSocketConnected = false;

        if (heartbeat != null && !heartbeat.isInterrupted()) {
            heartbeat.interrupt();
        }

        System.out.println("Closing socket and removing group.");

        if(wifiSocketManager != null) {
            wifiSocketManager.closeSocketAndKillThread();
        }

        System.out.println("Removing group for wifidirect");
    }

    private void handleGameObject(Message message) {
        LogUtils.printLog(TAG, "Message handled by handleGameObject Event::" + message.arg1);

        JSONObject object = (JSONObject) message.obj;
        String userId = object.optString(GameConstants.USER_ID);

        EventData eventData = new EventData();
        eventData.userId_ = userId;
        eventData.socketAddr_ = object.optString(GameConstants.CLIENT_SOCKET_ADDRESS);

        switch (message.arg1) {
            // Send Game Request to notify remote user if connection is established
            case MobiMix.GameEvent.EVENT_CONNECTION_ESTABLISHED_ACK:
                GameRequest gameRequest = MobiMixCache.getGameFromCache(userId);
                if (gameRequest != null) {
                    eventData.event_ = MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST;
                } else {
                    eventData.event_ = MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST_ASK;
                }
                if(Utils.isGroupOwner()) {
                    startTimerToCheckAllUsersAreConnected();
                }
                break;
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST_ASK:
                eventData.event_ = MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST;
                break;

            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED_ACK:
                if (Utils.isGroupOwner()) {
                    Message msg = new Message();
                    msg.arg1 = message.arg1;
                    msg.obj = MessageConstructor.constructObjectToUpdateDBData(eventData);
                    CoreEngine.sendEventToGUI(msg);
                    if (MobiMixCache.getQueuedPlayersFromCache().size() <= 0) {
                        eventData.event_ = MobiMix.GameEvent.EVENT_GAME_QUEUED_USER_ASK;
                        // Add user entry in game_tables in group_owner
                    } else {
                        eventData.event_ = MobiMix.GameEvent.EVENT_GAME_REQUEST_TO_QUEUED_USERS;
                    }
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
                {
                    EventData eventData1 = new EventData();
                    eventData1.event_ = MobiMix.GameEvent.EVENT_GAME_REQUEST_TO_QUEUED_USERS;
                    eventData1.object_ = object;
                    sendEventToRadioService(eventData1);
                }

                // Acknowledge recipient to ack event
                eventData.event_ = MobiMix.GameEvent.EVENT_GAME_QUEUED_USER_ACK;
                break;
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                if (Utils.isGroupOwner()) {
                    CoreEngine.sendEventToGUI(message);

                    // checks in queue, if there is any user to send request it will send
                    List<MBNearbyPlayer> queuedPlayers = MobiMixCache.getQueuedPlayersFromCache();
                    if(queuedPlayers.size() > 0) {
                        sendEventToRadioService(new EventData(MobiMix.GameEvent.EVENT_GAME_REQUEST_TO_QUEUED_USERS));
                    }
                }
                // Send Ack for game launch
                eventData.event_ = MobiMix.GameEvent.EVENT_GAME_LAUNCHED_ACK;
                break;
            case MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA:
                {
                    EventData eventData1 = new EventData();
                    eventData1.event_ = MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA;
                    eventData1.object_ = object;
                    sendEventToRadioService(eventData1);
                }
                // Acknowledge recipient to ack event
                eventData.event_ = MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_ACK;
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
        if(eventData.socketAddr_ == null) {
            eventData.socketAddr_ = wifiSocketManager.getConnectedSocketInetAddress();
        }

        switch (eventData.event_) {
            case MobiMix.GameEvent.EVENT_CONNECTION_ESTABLISHED_ACK:
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST_ACK:
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST_ASK:
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED_ACK:
            case MobiMix.GameEvent.EVENT_GAME_QUEUED_USER_ASK:
                break;
            case MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA:
                eventObj = MessageConstructor.constructObjectToSendDBDataInBatch(eventData);
                break;
            case MobiMix.GameEvent.EVENT_GAME_LAUNCHED:
                if(eventData.socketAddr_ == null) {
                    eventData.socketAddr_ = wifiSocketManager.getConnectedSocketInetAddress();
                }
                eventObj = MessageConstructor.constructObjectToSendGameLaunchedEvent(eventData);
            break;
            case MobiMix.GameEvent.EVENT_GAME_INFO_REQUEST:
                eventObj = MessageConstructor.constructObjectToSendGameRequestEvent(eventData);
                break;
            case MobiMix.GameEvent.EVENT_GAME_QUEUED_USER:
                eventObj = MessageConstructor.constructObjectToSendQueuedUserEvent(eventData);
                break;
            default:
                break;
        }

        if (eventObj == null) {
            eventObj = new JSONObject();
        }
        eventObj = MessageConstructor.addEventAndSocketAddress(eventObj, eventData);
        if (bluetoothSocketManager != null) {
            bluetoothSocketManager.writeObject(eventObj);
        }
        if (wifiSocketManager != null) {
            wifiSocketManager.writeObject(eventData.socketAddr_, eventObj);
        }
    }

    public void sendEventToAllUsers(EventData eventData) {
        wifiSocketManager.sendToAll(eventData);
    }

    private void sendEventToRadioService(EventData eventData) {
        int event = eventData.event_;
        JSONObject object = eventData.object_;
        if(object == null) {
            object = new JSONObject();
        }

        switch (event) {
            case MobiMix.GameEvent.EVENT_GAME_REQUEST_TO_QUEUED_USERS:
            case MobiMix.GameEvent.EVENT_GAME_QUEUED_USER:
                if (object != null) {
                    List<MBNearbyPlayer> players = (List<MBNearbyPlayer>) object.opt(GameConstants.GAME_PLAYERS_IN_QUEUE);
                    if (players != null && players.size() > 0) {
                        MobiMixCache.addPlayersInQueueCache(players);

//                        MBGameInfo mbGameInfo = (MBGameInfo)object.opt("mb_game_info");
//                        List<MBNearbyPlayer> nearbyPlayers = (List<MBNearbyPlayer>)object.opt("mb_selected_players");
//                        boolean isReqForQueuedPlayers = object.optBoolean("mb_request_queue");
                    }
                }
            case MobiMix.GameEvent.EVENT_GAME_READ_TABLE_DATA:
            case MobiMix.GameEvent.EVENT_GAME_UPDATE_TABLE_DATA:
                GameRequest gameRequest = MobiMixCache.getCurrentGameRequestFromCache();
                if(gameRequest == null) {
                    return;
                }
                int connectionType = gameRequest.getConnectionType();
                long gameId = gameRequest.getGameId();
                String gamePackageName = gameRequest.getGamePackageName();
                try {
                    if (connectionType == 1) {
                        object.put(GameConstants.GAME_CONNECTION_TYPE, connectionType);
                        object.put(GameConstants.GAME_ID, gameId);
                        object.put(GameConstants.GAME_PACKAGE_NAME, gamePackageName);
                    } else if (connectionType == 2) {
                        object.put(GameConstants.GAME_CONNECTION_TYPE, connectionType);
                        object.put(GameConstants.GAME_ID, gameId);
                        object.put(GameConstants.GAME_PACKAGE_NAME, gamePackageName);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                Message msg = new Message();
                msg.arg1 = event;
                msg.obj = object;
                CoreEngine.sendEventToGUI(msg);
                break;
            default:
                break;
        }
    }

    private void startTimerToCheckAllUsersAreConnected() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Utils.isGroupOwner()) {
                    sendEventToRadioService(new EventData(MobiMix.GameEvent.EVENT_GAME_READ_TABLE_DATA));
                    timer.cancel();
                }
            }
        }, 25000);
    }
}
