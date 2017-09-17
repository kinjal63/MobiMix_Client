package io.connection.bluetooth.actionlisteners;

import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;

/**
 * Created by Kinjal on 4/22/2017.
 */

public interface DeviceClickListener {
    void onBluetoothDeviceClick(BluetoothRemoteDevice... device);
    void onWifiDeviceClick(WifiP2PRemoteDevice device);
}
