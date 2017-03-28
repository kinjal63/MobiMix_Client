package io.connection.bluetooth.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.connection.bluetooth.receiver.WiFiDirectBroadcastReceiver;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by KP49107 on 23-03-2017.
 */
public class WifiDirectService implements WifiP2pManager.ConnectionInfoListener {
    private static WifiDirectService wifiDirectService;
    private static String obj = "wifiDirectService";
    private static Context mContext = null;
    private String wifiDirectDeviceName = "";

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private IntentFilter mIntentFilter;
    private WiFiDirectBroadcastReceiver mReceiver;

    private List<WifiP2pDevice> wifiP2PDeviceList = new ArrayList<>();

    private String TAG = "WifiDirectService";

    private WifiDirectService() {
        initialize();
    }

    public static WifiDirectService getInstance(Context context) {
        mContext = context;
        synchronized (obj) {
            if( wifiDirectService == null ) {
                wifiDirectService = new WifiDirectService();
            }
        }
        return wifiDirectService;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void initialize() {
        WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        setUp();
        enableP2P();
        setDeviceName();
    }

    public void connectWithPeer() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "discovery is initiated.");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.requestPeers(channel, peerListListener);
                    }
                }, 1500);
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "discovery initiation is failed.");
            }
        });
    }

    public void initiateDiscovery() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "discovery is initiated.");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "discovery initiation is failed.");
            }
        });
    }

    WifiP2pManager.ChannelListener mChannelListener = new WifiP2pManager.ChannelListener() {
        @Override
        public void onChannelDisconnected() {
            Log.d(TAG, "Channel is disconnected");
        }
    };

    private void setUp() {
        manager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(mContext, Looper.getMainLooper(), mChannelListener);

        mReceiver = new WiFiDirectBroadcastReceiver(manager, channel);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void enableP2P() {
        try {
            Class<?> wifiP2PManager = Class
                    .forName("android.net.wifi.p2p.WifiP2pManager");

            Method method = wifiP2PManager
                    .getMethod(
                            "enableP2p",
                            new Class[] { android.net.wifi.p2p.WifiP2pManager.Channel.class });

            method.invoke(manager, channel);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setDeviceName() {
        try {
            Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            Method setDeviceName = manager.getClass().getMethod(
                    "setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);

            Object arglist[] = new Object[3];
            arglist[0] = channel;
            arglist[1] = ApplicationSharedPreferences.getInstance(mContext).getValue("email");
            arglist[2] = new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    System.out.println("setDeviceName succeeded");
                }

                @Override
                public void onFailure(int reason) {
                    System.out.println("setDeviceName failed");
                }
            };

            setDeviceName.invoke(manager, arglist);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void connectWithWifiAddress(String wifiDirectAddress) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiDirectAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                UtilsHandler.dismissProgressDialog();
                Toast.makeText(mContext, "Wifi direct connection is established.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                UtilsHandler.dismissProgressDialog();
                Toast.makeText(mContext, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            boolean isDeviceFound = false;
            Collection<WifiP2pDevice> p2pDeviceList = peerList.getDeviceList();

            wifiP2PDeviceList.clear();
            wifiP2PDeviceList.addAll(p2pDeviceList);

            for(WifiP2pDevice device : p2pDeviceList) {
                if( device.deviceName.equalsIgnoreCase(wifiDirectDeviceName) ) {
                    connectWithWifiAddress(device.deviceAddress);
                    isDeviceFound = true;
                }
            }
            setWifiDirectDeviceName(null);

            if( !isDeviceFound ) {
                UtilsHandler.dismissProgressDialog();
                Toast.makeText(mContext, "Device not found. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void setWifiDirectDeviceName(String wifiDirectDeviceName) {
        this.wifiDirectDeviceName = wifiDirectDeviceName;
    }

    public String getWifiDirectDeviceName() {
        return this.wifiDirectDeviceName;
    }

    public List<WifiP2pDevice> getWifiP2PDeviceList() {
        return wifiP2PDeviceList;
    }

    public void registerReceiver() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
    }

    public void unRegisterReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }
}
