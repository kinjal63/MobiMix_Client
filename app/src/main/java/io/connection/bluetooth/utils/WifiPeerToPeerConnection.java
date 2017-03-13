package io.connection.bluetooth.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import io.connection.bluetooth.receiver.WiFiDirectBroadcastReceiver;

/**
 * Created by Kinjal on 1/12/2017.
 */

public class WifiPeerToPeerConnection {
    private static WifiPeerToPeerConnection instance = null;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    IntentFilter mIntentFilter;
    private Context context;

    WifiPeerToPeerConnection(Context context) {
        this.context = context;

        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, context.getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public static WifiPeerToPeerConnection getInstance(Context context) {
        if( instance == null ) {
            instance = new WifiPeerToPeerConnection(context);
        }
        return instance;
    }

    public void connectWithWifiAddress(String wifiAddress) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(context, "Wifi direct connection is established.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(context, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Toast.makeText(context, "Connect failed. Retry.",
                    Toast.LENGTH_SHORT).show();
        }
    };

    public void registerReceiver() {
        this.context.registerReceiver(mReceiver, mIntentFilter);
    }

    public void unRegisterReceiver() {
        this.context.unregisterReceiver(mReceiver);
    }
}
