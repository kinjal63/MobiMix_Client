package io.connection.bluetooth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import io.connection.bluetooth.Domain.LocalP2PDevice;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.cache.CacheConstants;
import io.connection.bluetooth.utils.cache.MobiMixCache;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private String wifiDirectDeviceName;
    private String TAG = "WifiP2PBroadcastReceiver";

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
                mManager.requestPeers(mChannel, wifiService.peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if (mManager == null) {
                    return;
                }

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    // we are connected with the other device, request connection
                    // info to find group owner IP
                    ApplicationSharedPreferences.getInstance(context).addBooleanValue(Constants.PREF_WIFIDIRECT_CONNECTED, true);
                    Log.d(TAG, "Connected to p2p. Requesting network details");
                    mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener) WifiDirectService.getInstance(context));
                }
                else {
                    MobiMixCache.putInCache(CacheConstants.CACHE_IS_GROUP_OWNER, 0);
                    ApplicationSharedPreferences.getInstance(context).addBooleanValue(Constants.PREF_WIFIDIRECT_CONNECTED, false);

                    WifiDirectService.getInstance(context).closeConnection();
                    WifiDirectService.getInstance(context).initiateDiscovery();
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                LocalP2PDevice.getInstance().setLocalDevice(device);

                System.out.println("Device status->" + device.status);

                if(device.status == WifiP2pDevice.CONNECTED) {
                    ApplicationSharedPreferences.getInstance(context).addBooleanValue(Constants.PREF_WIFIDIRECT_CONNECTED, true);
                    mManager.requestConnectionInfo(mChannel, (WifiP2pManager.ConnectionInfoListener) WifiDirectService.getInstance(context));
                }
                else {
                    WifiDirectService wifiDirectService = WifiDirectService.getInstance(context);
                    wifiDirectService.initiateDiscovery();
                }

//            if (mManager != null) {
//                mManager.requestPeers(mChannel, WifiDirectService.getInstance(context).peerListListener);
//            }
        }
    }

    public void setWifiDirectDeviceName(String wifiDirectDeviceName) {
        this.wifiDirectDeviceName = wifiDirectDeviceName;
    }
}