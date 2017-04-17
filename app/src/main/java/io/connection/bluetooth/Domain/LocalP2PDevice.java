package io.connection.bluetooth.Domain;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Kinjal on 4/18/2017.
 */

public class LocalP2PDevice {
    private WifiP2pDevice localDevice;

    private static final LocalP2PDevice instance = new LocalP2PDevice();

    /**
     * Method to get the instance of this class.
     * @return instance of this class.
     */
    public static LocalP2PDevice getInstance() {
        return instance;
    }

    /**
     * Private constructor, because is a singleton class.
     */
    private LocalP2PDevice(){
        localDevice = new WifiP2pDevice();
    }

    public WifiP2pDevice getLocalDevice() {
        return localDevice;
    }

    public void setLocalDevice(WifiP2pDevice localDevice) {
        this.localDevice = localDevice;
    }
}
