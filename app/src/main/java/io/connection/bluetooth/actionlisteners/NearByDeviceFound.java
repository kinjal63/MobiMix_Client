package io.connection.bluetooth.actionlisteners;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.Collection;
import java.util.List;

/**
 * Created by KP49107 on 29-03-2017.
 */
public interface NearByDeviceFound {
    void onDevicesAvailable(Collection<WifiP2pDevice> devices);
}
