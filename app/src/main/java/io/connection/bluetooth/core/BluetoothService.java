package io.connection.bluetooth.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import io.connection.bluetooth.Api.WSManager;
import io.connection.bluetooth.Domain.GameConnectionInfo;
import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.Thread.ConnectedThread;
import io.connection.bluetooth.actionlisteners.BluetoothPairCallback;
import io.connection.bluetooth.actionlisteners.IUpdateListener;
import io.connection.bluetooth.actionlisteners.NearByBluetoothDeviceFound;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.receiver.BluetoothDeviceReceiver;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;

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

    BluetoothService() {

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

    public static BluetoothService getInstance() {
        if(bluetoothService == null) {
            bluetoothService = new BluetoothService();
        }
        return bluetoothService;
    }

    public void init() {
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

    public void setRemoteBluetoothDevice(BluetoothRemoteDevice device) {
        if( !bluetoothDeviceMap.containsKey(device.getDevice().getAddress()) ) {
            bluetoothDeviceMap.put(device.getDevice().getAddress(), device);
        }
        if( nearbyDeviceFoundAction != null ) {
            nearbyDeviceFoundAction.onBluetoothDeviceAvailable(device);
        }
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
}
