package io.connection.bluetooth.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.actionlisteners.DeviceConnectionListener;
import io.connection.bluetooth.actionlisteners.NearByDeviceFound;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.receiver.WiFiDirectBroadcastReceiver;
import io.connection.bluetooth.socketmanager.WifiP2PClientHandler;
import io.connection.bluetooth.socketmanager.WifiP2PServerHandler;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by KP49107 on 23-03-2017.
 */
public class WifiDirectService implements WifiP2pManager.ConnectionInfoListener {
    private static WifiDirectService wifiDirectService;
    private static String obj = "wifiDirectService";
    private static Context mContext = null;
    private String wifiDirectDeviceName = "";

    private String className = "";
    private Modules module = Modules.NONE;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private IntentFilter mIntentFilter;
    private WiFiDirectBroadcastReceiver mReceiver;

    private List<WifiP2PRemoteDevice> wifiP2PDeviceList = new ArrayList<>();
    private Thread socketHandler;
    private MessageHandler messageHandler;

    private NearByDeviceFound nearByDeviceCallback;
    private SocketConnectionListener socketConnectionListener;

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
        setDeviceName();
        new Timer().schedule(new DiscoveryTask(), 500, 30000);

        messageHandler = new MessageHandler(mContext, this);
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

    public void connectWithWifiAddress(String wifiDirectAddress, final DeviceConnectionListener listener) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiDirectAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                UtilsHandler.dismissProgressDialog();
                Toast.makeText(mContext, "Wifi direct connection is established.",
                        Toast.LENGTH_SHORT).show();
                if( listener != null ) {
                    listener.onDeviceConnected(true);
                }
            }

            @Override
            public void onFailure(int reason) {
                UtilsHandler.dismissProgressDialog();
                Toast.makeText(mContext, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
                if( listener != null ) {
                    listener.onDeviceConnected(false);
                }
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
/*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */
        if(p2pInfo.groupFormed) {
//            if( socketHandler != null ) {
//                closeConnetion();
//            }

            if (p2pInfo.isGroupOwner) {
                Log.d(TAG, "Connected as group owner");
                try {
                    Log.d(TAG, "socketHandler!=null? = " + (socketHandler != null));
                    socketHandler = new WifiP2PServerHandler(messageHandler);
                    socketHandler.start();

                } catch (IOException e) {
                    Log.e(TAG, "Failed to create a server thread - " + e);
                    return;
                }
            } else {
                Log.d(TAG, "Connected as peer");
                socketHandler = new WifiP2PClientHandler(messageHandler, p2pInfo.groupOwnerAddress);
                socketHandler.start();
            }
        }
    }

    public void closeConnection() {
        if(socketHandler != null && socketHandler instanceof WifiP2PClientHandler) {
            ((WifiP2PClientHandler)socketHandler).closeSocketAndKillThisThread();
        }
        else if(socketHandler != null && socketHandler instanceof WifiP2PServerHandler) {
            ((WifiP2PServerHandler)socketHandler).closeSocketAndKillThisThread();
        }
    }

    public boolean isSocketConnectedWithHost(String hostName) {
        return messageHandler.isSocketConnected();
//        if(socketHandler != null && socketHandler instanceof WifiP2PClientHandler) {
//            return ((WifiP2PClientHandler)socketHandler).checkSocketConnection(hostName);
//        }
//        else if(socketHandler != null && socketHandler instanceof WifiP2PServerHandler) {
//            return ((WifiP2PServerHandler)socketHandler).checkSocketConnection(hostName);
//        }
//        return false;
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
        Collection<WifiP2pDevice> p2pDeviceList = peerList.getDeviceList();

        wifiP2PDeviceList.clear();

        for(WifiP2pDevice device : p2pDeviceList) {
            WifiP2PRemoteDevice remoteDevice = new WifiP2PRemoteDevice(device, device.deviceName);
            wifiP2PDeviceList.add(remoteDevice);
        }
        if( nearByDeviceCallback != null ) {
            nearByDeviceCallback.onDevicesAvailable(wifiP2PDeviceList);
        }
        }
    };

    public void a(WifiP2pDevice device) {
        for( WifiP2PRemoteDevice remoteDevice : wifiP2PDeviceList ) {
            if (remoteDevice.getDevice().deviceName.equalsIgnoreCase(wifiDirectDeviceName)) {
                connectWithWifiAddress(device.deviceAddress, null);
            }
        }
        setWifiDirectDeviceName(null);
    }

    public void setNearByDeviceFoundCallback(NearByDeviceFound nearByDeviceCallback) {
        this.nearByDeviceCallback = nearByDeviceCallback;
    }

    public void setSocketConnectionListener(SocketConnectionListener socketConnectionListener) {
        this.socketConnectionListener = socketConnectionListener;
    }

    public void setWifiDirectDeviceName(String wifiDirectDeviceName) {
        this.wifiDirectDeviceName = wifiDirectDeviceName;
    }

    public void removeGroup() {
        if (manager != null && channel != null) {
            manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
//                    if (group != null && manager != null && channel != null
//                            && group.isGroupOwner()) {
                        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "removeGroup onSuccess -");
                                initiateDiscovery();
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "removeGroup onFailure -" + reason);
                            }
                        });
                    }
//                }
            });
        }
    }

    public void closeSocket() {
        if(messageHandler != null) {
            messageHandler.sendMessage(new String("NowClosing").getBytes());
            try {
                Thread.sleep(1500);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        messageHandler.closeSocket();
    }

    public void notifyUserForClosedSocket() {
        if(socketConnectionListener != null) {
            socketConnectionListener.socketClosed();
        }
    }

    public void notifyUserForConnectedSocket(String remoteDeviceAddress) {
        if(socketConnectionListener != null) {
            socketConnectionListener.socketConnected(true, remoteDeviceAddress);
        }
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    public Modules getModule() {
        return this.module;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setModule(Modules module) {
        this.module = module;
    }

    public String getWifiDirectDeviceName() {
        return this.wifiDirectDeviceName;
    }

    public String getRemoteDeviceAddress() {
        return messageHandler.getRemoteDeviceAddress();
    }

    public List<WifiP2PRemoteDevice> getWifiP2PDeviceList() {
        return wifiP2PDeviceList;
    }

    public void registerReceiver() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
    }

    public void unRegisterReceiver() {
        try {
            mContext.unregisterReceiver(mReceiver);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
    private class DiscoveryTask extends TimerTask {
        @Override
        public void run() {
            initiateDiscovery();
        }
    }

}
