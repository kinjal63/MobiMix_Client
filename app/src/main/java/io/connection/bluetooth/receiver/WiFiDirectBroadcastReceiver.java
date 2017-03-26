package io.connection.bluetooth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

import io.connection.bluetooth.Services.WifiDirectService;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private String wifiDirectDeviceName;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                WifiDirectService.getInstance(context).initialize();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                WifiDirectService wifiService = WifiDirectService.getInstance(context);
                if(wifiService.getWifiDirectDeviceName() != null && wifiService.getWifiDirectDeviceName().length() > 0) {
                    mManager.requestPeers(mChannel, WifiDirectService.getInstance(context).peerListListener);
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            if (mManager != null) {
//                mManager.requestPeers(mChannel, WifiDirectService.getInstance(context).peerListListener);
//            }
        }
    }

    public void setWifiDirectDeviceName(String wifiDirectDeviceName) {
        this.wifiDirectDeviceName = wifiDirectDeviceName;
    }
}