package io.connection.bluetooth.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.actionlisteners.NearByBluetoothDeviceFound;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.receiver.BluetoothDeviceReceiver;

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

    private String TAG = BluetoothService.class.getSimpleName();

    BluetoothService() {
        init();
    }

    public static BluetoothService getInstance() {
        if(bluetoothService == null) {
            bluetoothService = new BluetoothService();
        }
        return bluetoothService;
    }

    public void setNearByBluetoothDeviceAction(NearByBluetoothDeviceFound nearbyDeviceFoundAction) {
        this.nearbyDeviceFoundAction = nearbyDeviceFoundAction;
    }

    public void setRemoteBluetoothDevice(BluetoothRemoteDevice device) {
        if( !bluetoothDeviceMap.containsKey(device.getDevice().getAddress()) ) {
            bluetoothDeviceMap.put(device.getDevice().getAddress(), device);
        }
    }

    public List<BluetoothRemoteDevice> getBluetoothDevices() {
        List<BluetoothRemoteDevice> devices = new ArrayList<BluetoothRemoteDevice>();
        if( !bluetoothDeviceMap.isEmpty() ) {
            devices.addAll(bluetoothDeviceMap.values());
        }
        return devices;
    }

    private void init() {
        this.context = MobileMeasurementApplication.getInstance().getContext();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothDeviceFoundReceiver = new BluetoothDeviceReceiver();
        context.registerReceiver(bluetoothDeviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            MobileMeasurementApplication.getInstance().getActivity()
                    .startActivityForResult(enableBlueTooth, 1);
        } else {
            startBluetoothDiscovery();
        }
    }

    public void initiateDiscovery() {
        // clear stored bluetooth devices
        bluetoothDeviceMap.clear();

        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();
        bluetoothDeviceFoundReceiver.findAlreadyBondedDevice();
    }

    private void startBluetoothDiscovery() {
        Timer timer = new Timer();
        timer.schedule(new BluetoothSearchTask(), 500, 600000);
    }

    private class BluetoothSearchTask extends TimerTask {
        @Override
        public void run() {
            initiateDiscovery();
        }
    }

}
