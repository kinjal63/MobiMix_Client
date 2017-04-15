package io.connection.bluetooth.actionlisteners;

import java.util.Collection;

import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;

/**
 * Created by KP49107 on 14-04-2017.
 */
public interface NearByBluetoothDeviceFound {
    void onBluetoothDeviceAvailable(BluetoothRemoteDevice device);
}
