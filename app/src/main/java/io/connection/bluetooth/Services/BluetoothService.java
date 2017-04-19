package io.connection.bluetooth.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.Thread.ConnectedThread;
import io.connection.bluetooth.actionlisteners.NearByBluetoothDeviceFound;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.receiver.BluetoothDeviceReceiver;
import io.connection.bluetooth.utils.Utils;

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

    BluetoothService() {

    }

    public static BluetoothService getInstance() {
        if(bluetoothService == null) {
            bluetoothService = new BluetoothService();
        }
        return bluetoothService;
    }

    public void init() {
        this.context = MobileMeasurementApplication.getInstance().getContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothDeviceFoundReceiver = BluetoothDeviceReceiver.getInstance();
        registerReceiver();

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            MobileMeasurementApplication.getInstance().getActivity()
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

    public void unregisterReceiver() {
        context.unregisterReceiver(bluetoothDeviceFoundReceiver);
    }

}
