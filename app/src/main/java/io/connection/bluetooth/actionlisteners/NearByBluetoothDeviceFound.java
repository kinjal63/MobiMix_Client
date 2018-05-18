package io.connection.bluetooth.actionlisteners;

import io.connection.bluetooth.Database.entity.MBNearbyPlayer;

/**
 * Created by KP49107 on 14-04-2017.
 */
public interface NearByBluetoothDeviceFound {
    void onBluetoothDeviceAvailable(MBNearbyPlayer device);
}
