package io.connection.bluetooth.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Domain.GameConnectionInfo;
import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.Thread.ConnectedThread;
import io.connection.bluetooth.Thread.GameEventAcceptThread;
import io.connection.bluetooth.Thread.GameEventConnectThread;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.actionlisteners.BluetoothPairCallback;
import io.connection.bluetooth.actionlisteners.DeviceConnectionListener;
import io.connection.bluetooth.actionlisteners.IUpdateListener;
import io.connection.bluetooth.actionlisteners.NearByBluetoothDeviceFound;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.receiver.BluetoothDeviceReceiver;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;
import io.connection.bluetooth.utils.cache.CacheConstants;
import io.connection.bluetooth.utils.cache.MobiMixCache;

/**
 * Created by KP49107 on 14-04-2017.
 */
public class BluetoothService {
    private static BluetoothService bluetoothService;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDeviceReceiver bluetoothDeviceFoundReceiver;
    private Context context;
    private NearByBluetoothDeviceFound nearbyDeviceFoundAction;
    private Map<String, BluetoothRemoteDevice> bluetoothDeviceMap =
            new LinkedHashMap<String, BluetoothRemoteDevice>();
    private SocketConnectionListener socketConnectionListener;
    private Timer discoveryTimer;
    private ConnectedThread connectedThread;
    private Vector<String> connectedSocketAddresses = new Vector<>();

    private String TAG = BluetoothService.class.getSimpleName();
    private Modules module;
    private String className;
    private MessageHandler handler;
    private BluetoothSocket bluetoothSocket;

    BluetoothService() {

    }

    public static BluetoothService getBluetoothService() {
        return bluetoothService;
    }

    public void setModule(Modules module) {
        this.module = module;
    }

    public Modules getModule() {
        return this.module;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public MessageHandler handler() {
        return handler;
    }

    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }

    public static BluetoothService getInstance() {
        if(bluetoothService == null) {
            bluetoothService = new BluetoothService();
        }
        return bluetoothService;
    }

    public void init() {
//        this.handler = new MessageHandler(context, this);

//        this.handler = handler_;
        this.context = MobiMixApplication.getInstance().getContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothDeviceFoundReceiver = BluetoothDeviceReceiver.getInstance();
        registerReceiver();

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            MobiMixApplication.getInstance().getActivity()
                    .startActivityForResult(enableBlueTooth, 1);
        } else {
            startBluetoothDiscoveryTimer();
        }
    }

    public void registerReceiver() {
        context.registerReceiver(bluetoothDeviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    public void initiateDiscovery() {
        startBluetoothDiscoveryTimer();
    }

    public void startDiscovery() {
        bluetoothDeviceMap.clear();

        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        bluetoothDeviceFoundReceiver.findAlreadyBondedDevice();
        bluetoothAdapter.startDiscovery();
    }

    private void startBluetoothDiscoveryTimer() {
        if(discoveryTimer != null) {
            discoveryTimer.cancel();
            discoveryTimer.purge();
        }
        discoveryTimer = new Timer();
        discoveryTimer.schedule(new BluetoothSearchTask(), 500, 600000);
    }

    private class BluetoothSearchTask extends TimerTask {
        @Override
        public void run() {
            startDiscovery();
        }
    }

    public void startChatThread(BluetoothDevice device) {
        closeAllRunningThreads();
        startConnectionThread(device);
    }

    private synchronized void startConnectionThread(BluetoothDevice device) {
        connectedThread = new ConnectedThread(device);
        connectedThread.start();
    }

    private synchronized void closeAllRunningThreads() {
        if( connectedThread != null && !connectedThread.isInterrupted() ) {
            connectedThread.cancel();

            connectedThread.interrupt();
            connectedThread = null;
        }
    }

    public void setNearByBluetoothDeviceAction(NearByBluetoothDeviceFound nearbyDeviceFoundAction) {
        this.nearbyDeviceFoundAction = nearbyDeviceFoundAction;
    }

    public void setSocketConnectionListener(SocketConnectionListener socketConnectionListener) {
        this.socketConnectionListener = socketConnectionListener;

    }

    public void notifyConnectEventToUser(String remoteDeviceAddress) {
        if( this.socketConnectionListener != null ) {
            this.socketConnectionListener.socketConnected(true, remoteDeviceAddress);
        }
    }

    public void notifyDisconnectEventToUser() {
        if( this.socketConnectionListener != null ) {
            this.socketConnectionListener.socketClosed();
        }
    }

    public void addRemoteBluetoothDevice(BluetoothRemoteDevice device) {
        if( !bluetoothDeviceMap.containsKey(device.getDevice().getAddress()) ) {
            bluetoothDeviceMap.put(device.getDevice().getName(), device);
        }
        if( nearbyDeviceFoundAction != null ) {
            nearbyDeviceFoundAction.onBluetoothDeviceAvailable(device);
        }
        NetworkManager.getInstance().setAvailabilityForBluetoothDevice(device.getName());
    }

    public void addSocketConnectionForAddress(String deviceAddress) {
        connectedSocketAddresses.addElement(deviceAddress);
        notifyConnectEventToUser(deviceAddress);
    }

    public void removeSocketConnection() {
        connectedSocketAddresses.clear();
        closeAllRunningThreads();
        notifyDisconnectEventToUser();
    }

    public boolean isSocketConnectedForAddress(String deviceAddress) {
        if(connectedSocketAddresses.contains(deviceAddress)) {
            return true;
        }
        return false;
    }

    public List<BluetoothRemoteDevice> getBluetoothDevices() {
        List<BluetoothRemoteDevice> devices = new ArrayList<BluetoothRemoteDevice>();
        if( !bluetoothDeviceMap.isEmpty() ) {
            devices.addAll(bluetoothDeviceMap.values());
        }
        return devices;
    }

    public BluetoothRemoteDevice getBluetoothDevice(String deviceName) {
        if(bluetoothDeviceMap.containsKey(deviceName)) {
            return bluetoothDeviceMap.get(deviceName);
        }
        return null;
    }

    public void sendChatMessage(byte[] message) {
        if( connectedThread != null ) {
            connectedThread.sendMessage(message);
        }
    }

    public void endChat() {
        if( connectedThread != null ) {
            String message = "NOWweArECloSing";
            sendChatMessage(message.getBytes());

            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            removeSocketConnection();
        }
    }

    public void updateConnectionInfo(final GameRequest gameRequest, final boolean isNeedToNotify, final int isGroupOwner, final IUpdateListener iUpdateListener) {
        GameConnectionInfo connectionInfo = new GameConnectionInfo();

//        if (isGroupOwner) {
//            Log.d(TAG, "Connected as group owner");
//            connectionInfo.setIsGroupOwner(1);
//        } else {
//            Log.d(TAG, "Connected as peer");
//            connectionInfo.setIsGroupOwner(0);
//        }

        connectionInfo.setGameId(gameRequest.getGameId());
        connectionInfo.setUserId(ApplicationSharedPreferences.getInstance(MobiMixApplication.getInstance().getContext()).
                getValue("user_id"));
        connectionInfo.setConnectedUserId(gameRequest.getRemoteUserId());
        connectionInfo.setIsNeedToNotify(isNeedToNotify);
        connectionInfo.setIsGroupOwner(isGroupOwner);
        connectionInfo.setConnectionType(gameRequest.getConnectionType());

        NetworkManager.getInstance().updateConnectionInfo(connectionInfo, iUpdateListener);
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(bluetoothDeviceFoundReceiver);
    }

    public void acceptRequest(final GameRequest gameRequest) {
        BluetoothDeviceReceiver.getInstance().pairWithDevice(gameRequest.getBluetoothAddress(), new BluetoothPairCallback() {
            @Override
            public void devicePaired(boolean isPaired) {
                if (isPaired) {
                    BluetoothService.getInstance().updateConnectionInfo(gameRequest, true, 1, new IUpdateListener() {
                        @Override
                        public void onUpdated() {
//                            UtilsHandler.launchGame(gameRequest.getGamePackageName());
                        }
                    });
                }
            }
        });
    }

    public void sendBluetoothRequestToUser(final List<MBNearbyPlayer> players, final MBGameInfo gameInfo) {
        if(handler != null)
            handler.closeBluetoothSocket();

        if (players.size() > 0) {
            final MBNearbyPlayer player = players.get(0);
            connect(player.getEmail(), new DeviceConnectionListener() {
                @Override
                public void onDeviceConnected(boolean isConnected) {
                    GameRequest gameRequest = new GameRequest();
                    gameRequest.setGameId(gameInfo.getGameId());
                    gameRequest.setGameName(gameInfo.getGameName());
                    gameRequest.setGamePackageName(gameInfo.getGamePackageName());
                    gameRequest.setConnectionType(1);
                    gameRequest.setRemoteUserId(ApplicationSharedPreferences.getInstance(context).
                            getValue("user_id"));
                    gameRequest.setRemoteUserName(ApplicationSharedPreferences.getInstance(context).
                            getValue("user_name"));
                    gameRequest.setWifiAddress(ApplicationSharedPreferences.getInstance(context).
                            getValue("email"));
                    gameRequest.setBluetoothAddress(ApplicationSharedPreferences.getInstance(context).
                            getValue("email"));

                    MobiMixCache.putGameInCache(player.getPlayerId(), gameRequest);
                    MobiMixCache.putInCache(CacheConstants.CACHE_IS_GROUP_OWNER, 1);
//                    UtilsHandler.addGameInStack(gameRequest);
                    // remove 1st player from list as it is connected
                    players.remove(0);

                    // add all players except 1st in queue
                    MobiMixCache.addPlayersInQueueCache(players);
                }
            });
        }
    }

    private void connect(String deviceName, DeviceConnectionListener connectionListener) {
        BluetoothRemoteDevice remoteDevice = getBluetoothDevice(deviceName);
        UUID deviceUUID = GameEventAcceptThread.MY_UUID_SECURE;

        // Start Bluetooth Connection thread to connect with bluetooth device
        if(remoteDevice == null) {
//            Utils.showErrorDialog(MobiMixApplication.getInstance().getContext(), "Device is not found");
            return;
        }
        GameEventConnectThread gameEventConnectThread = new GameEventConnectThread(remoteDevice.getDevice(), deviceUUID, connectionListener);
        gameEventConnectThread.start();
    }

    public void handleEvent(int event) {
        switch (event) {
            case MobiMix.GameEvent.EVENT_GAME_REQUEST_TO_QUEUED_USERS:
                // send game request to queued playes
                break;
            default:
                break;
        }
    }
}
