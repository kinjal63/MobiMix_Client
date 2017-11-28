package io.connection.bluetooth.core;

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
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.connection.bluetooth.Api.async.IAPIResponse;
import io.connection.bluetooth.Database.entity.MBGameInfo;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Domain.GameConnectionInfo;
import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.Domain.LocalP2PDevice;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.actionlisteners.DeviceConnectionListener;
import io.connection.bluetooth.actionlisteners.IUpdateListener;
import io.connection.bluetooth.actionlisteners.NearByDeviceFound;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.receiver.WiFiDirectBroadcastReceiver;
import io.connection.bluetooth.socketmanager.WifiP2PClientHandler;
import io.connection.bluetooth.socketmanager.WifiP2PServerHandler;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.UtilsHandler;
import io.connection.bluetooth.utils.cache.CacheConstants;
import io.connection.bluetooth.utils.cache.MobiMixCache;
import retrofit2.Call;

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

    private HashSet<WifiP2PRemoteDevice> wifiP2PDeviceList = new HashSet<>();
    private HashMap<String, WifiP2PRemoteDevice> connectedDeviceMap = new HashMap<>();
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
            if (wifiDirectService == null) {
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
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        setUp();
        setDeviceName();

        initiateDiscovery();
        new Timer().schedule(new DiscoveryTask(), 500, 45000);
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
                            new Class[]{android.net.wifi.p2p.WifiP2pManager.Channel.class});

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
        config.groupOwnerIntent = 15;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                UtilsHandler.dismissProgressDialog();
                Toast.makeText(mContext, "Wifi direct connection is established.",
                        Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onDeviceConnected(true);
                }
            }

            @Override
            public void onFailure(int reason) {
                UtilsHandler.dismissProgressDialog();
                Toast.makeText(mContext, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
                if (listener != null) {
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
        if (p2pInfo.groupFormed) {
            if (p2pInfo.isGroupOwner) {
                Log.d(TAG, "Connected as group owner");
                try {
                    Log.d(TAG, "socketHandler!=null? = " + (socketHandler != null));
                    socketHandler = new WifiP2PServerHandler(messageHandler);
                    socketHandler.start();

                    MobiMixCache.putInCache(CacheConstants.CACHE_IS_GROUP_OWNER, 1);

                } catch (IOException e) {
                    Log.e(TAG, "Failed to create a server thread - " + e);
                    return;
                }
            } else {
                Log.d(TAG, "Connected as peer");
                socketHandler = new WifiP2PClientHandler(messageHandler, p2pInfo.groupOwnerAddress);
                socketHandler.start();

                MobiMixCache.putInCache(CacheConstants.CACHE_IS_GROUP_OWNER, 0);
            }
        }
    }

    public void closeConnection() {
        if (socketHandler != null && socketHandler instanceof WifiP2PClientHandler) {
            ((WifiP2PClientHandler) socketHandler).closeSocketAndKillThisThread();
        } else if (socketHandler != null && socketHandler instanceof WifiP2PServerHandler) {
            ((WifiP2PServerHandler) socketHandler).closeSocketAndKillThisThread();
        }
    }

    public boolean isSocketConnectedWithHost(String hostName) {
        return messageHandler.isSocketConnected();
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Collection<WifiP2pDevice> p2pDeviceList = peerList.getDeviceList();

            wifiP2PDeviceList.clear();

            for (final WifiP2pDevice device : p2pDeviceList) {
                System.out.println("Peers list->" + device.deviceName);
                WifiP2PRemoteDevice remoteDevice = new WifiP2PRemoteDevice(device, device.deviceName);
                wifiP2PDeviceList.add(remoteDevice);
                User user = new User();
                user.setName(device.deviceName);
                user.setEmail(device.deviceName);

//                NetworkManager.getInstance().checkIfUserAvailable(user, new IAPIResponse<User>() {
//                    @Override
//                    public void onResponseSuccess(User body) {
//                    }
//
//                    @Override
//                    public void onResponseFailure(Call<User> call) {
//                    }
//                });

            }
            for (WifiP2PRemoteDevice device : connectedDeviceMap.values()) {
                wifiP2PDeviceList.add(device);
            }
            if (nearByDeviceCallback != null) {
                nearByDeviceCallback.onDevicesAvailable(wifiP2PDeviceList);
            }
            NetworkManager.getInstance().deleteUserIfNotFoundInVicinity();
        }
    };

    public void a(WifiP2pDevice device) {
        for (WifiP2PRemoteDevice remoteDevice : wifiP2PDeviceList) {
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
        if (messageHandler != null) {
            messageHandler.socketClosed();
        }
    }

    public void updateConnectionInfo(final GameRequest gameRequest, final boolean isNeedToNotify, final IUpdateListener iUpdateListener) {
        System.out.println("Updating connection info");
        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
                if (p2pInfo.groupFormed) {
                    System.out.println("Updating connection info + group formed");
                    GameConnectionInfo connectionInfo = new GameConnectionInfo();

                    if (p2pInfo.isGroupOwner) {
                        Log.d(TAG, "Connected as group owner");
                        connectionInfo.setIsGroupOwner(1);
//                            UtilsHandler.launchGame(gameRequest.getGamePackageName());
                    } else {
                        Log.d(TAG, "Connected as peer");
                        connectionInfo.setIsGroupOwner(0);
                    }

                    connectionInfo.setGameId(gameRequest.getGameId());
                    connectionInfo.setUserId(ApplicationSharedPreferences.getInstance(mContext).
                            getValue("user_id"));
                    connectionInfo.setConnectedUserId(gameRequest.getRemoteUserId());
                    connectionInfo.setIsNeedToNotify(isNeedToNotify);
                    connectionInfo.setConnectionType(gameRequest.getConnectionType());

                    NetworkManager.getInstance().updateConnectionInfo(connectionInfo, iUpdateListener);
                }
            }
        });
    }

    public void notifyUserForClosedSocket() {
        if (socketConnectionListener != null) {
            socketConnectionListener.socketClosed();
        }
        initiateDiscovery();
    }

    public void notifyUserForConnectedSocket(String remoteDeviceAddress) {
        if (socketConnectionListener != null) {
            socketConnectionListener.socketConnected(true, remoteDeviceAddress);
        }
    }

    public WifiP2pManager getWifiP2PManager() {
        return this.manager;
    }

    public WifiP2pManager.Channel getWifiP2PChannel() {
        return this.channel;
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

    public HashSet<WifiP2PRemoteDevice> getWifiP2PDeviceList() {
        return wifiP2PDeviceList;
    }

    public void addConnectedDevice(WifiP2PRemoteDevice remoteDevice) {
        this.connectedDeviceMap.put(remoteDevice.getDevice().deviceAddress, remoteDevice);
    }

    public void removeConnectedDevice(WifiP2PRemoteDevice device) {
        this.connectedDeviceMap.remove(device.getDevice().deviceAddress);
    }

    public void registerReceiver() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
    }

    public void unRegisterReceiver() {
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private class DiscoveryTask extends TimerTask {
        @Override
        public void run() {
            initiateDiscovery();
        }
    }

    public void updateGameConnectionAndLaunchGame(final GameRequest gameRequest) {
        updateConnectionInfo(gameRequest, false, new IUpdateListener() {
            @Override
            public void onUpdated() {
                UtilsHandler.launchGame(gameRequest.getGamePackageName());
            }
        });
    }

    public void acceptRequest(GameRequest gameRequest) {
        setWifiDirectDeviceName(gameRequest.getWifiAddress());
        HashSet<WifiP2PRemoteDevice> devices = getWifiP2PDeviceList();
        for (WifiP2PRemoteDevice remoteDevice : devices) {
            if (remoteDevice.getDevice().deviceName.equalsIgnoreCase(gameRequest.getWifiAddress())) {
                UtilsHandler.addGameInStack(gameRequest);

                connectWithWifiAddress(remoteDevice.getDevice().deviceAddress, new DeviceConnectionListener() {
                    @Override
                    public void onDeviceConnected(boolean isConnected) {
                    }
                });
            }
        }
    }

    private void connect(String deviceName, DeviceConnectionListener listener) {
        HashSet<WifiP2PRemoteDevice> devices = wifiDirectService.getWifiP2PDeviceList();
        for (WifiP2PRemoteDevice remoteDevice : devices) {
            if (remoteDevice.getDevice().deviceName.equalsIgnoreCase(deviceName)) {
                connectWithWifiAddress(remoteDevice.getDevice().deviceAddress, listener);
            }
        }
    }

    public void sendWifiDirectRequestToUser(final List<MBNearbyPlayer> players, final MBGameInfo gameInfo) {
        WifiP2pDevice localDevice = LocalP2PDevice.getInstance().getLocalDevice();
        if (localDevice.status == WifiP2pDevice.CONNECTED && !localDevice.isGroupOwner()) {
            removeGroup();
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (players.size() > 0) {
            MBNearbyPlayer player = players.get(0);
            connect(player.getEmail(), new DeviceConnectionListener() {
                @Override
                public void onDeviceConnected(boolean isConnected) {
                    GameRequest gameRequest = new GameRequest();
                    gameRequest.setGameId(gameInfo.getGameId());
                    gameRequest.setGameName(gameInfo.getGameName());
                    gameRequest.setGamePackageName(gameInfo.getGamePackageName());
                    gameRequest.setConnectionType(2);
                    gameRequest.setRemoteUserId(ApplicationSharedPreferences.getInstance(mContext).
                            getValue("user_id"));
                    gameRequest.setRemoteUserName(ApplicationSharedPreferences.getInstance(mContext).
                            getValue("user_name"));
                    gameRequest.setWifiAddress(ApplicationSharedPreferences.getInstance(mContext).
                            getValue("email"));

                    MobiMixCache.putGameInCache(ApplicationSharedPreferences.getInstance(mContext).
                            getValue("user_id"), gameRequest);
//                    UtilsHandler.addGameInStack(gameRequest);
                    // remove 1st player from list as it is connected
                    players.remove(0);

                    // add all players except 1st in queue
                    UtilsHandler.addPlayersInQueue(players);
                }
            });
        }
    }
}
