package io.connection.bluetooth.actionlisteners;

import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;

/**
 * Created by Kinjal on 4/22/2017.
 */

public interface DeviceClickListener {
    void onBluetoothDeviceClick(BluetoothRemoteDevice... device);
    void onWifiDeviceClick(MBNearbyPlayer device);
}
