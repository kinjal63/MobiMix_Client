package io.connection.bluetooth.activity;

import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Database.DBParams;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.Domain.LocalP2PDevice;
import io.connection.bluetooth.R;
import io.connection.bluetooth.actionlisteners.ISocketEventListener;
import io.connection.bluetooth.activity.gui.GUIManager;
import io.connection.bluetooth.core.BluetoothService;
import io.connection.bluetooth.core.IWifiDisconnectionListener;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.Thread.ConnectedBusinessThread;
import io.connection.bluetooth.actionlisteners.DeviceClickListener;
import io.connection.bluetooth.actionlisteners.DeviceConnectionListener;
import io.connection.bluetooth.actionlisteners.NearByBluetoothDeviceFound;
import io.connection.bluetooth.actionlisteners.NearByDeviceFound;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
import io.connection.bluetooth.adapter.BluetoothDeviceAdapter;
import io.connection.bluetooth.adapter.WifiP2PDeviceAdapter;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 07/09/16.
 */
public class BusinessCardListActivityUser extends BaseActivity implements SearchView.OnQueryTextListener,
        DeviceClickListener, IDBResponse {
    private static final String TAG = "BusinessCardListActivit";
    BluetoothAdapter bluetoothAdapter;
    private Toolbar toolbar;
    static ConnectedBusinessThread connectedThread;

    private BluetoothDeviceAdapter bluetoothDeviceAdapter;
    private WifiP2PDeviceAdapter wifiDeviceAdapter;

    private WifiP2pDevice p2pDevice;

    static RecyclerView deviceLayout;
    ApiCall apiCall;
    private Context mContext;
    private SearchView searchView;

    private ArrayList<MBNearbyPlayer> listUsers = new ArrayList<>();
    private NetworkType networkType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.business_layout_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
        ImageCache.setContext(mContext);
        apiCall = ApiClient.getClient().create(ApiCall.class);

        if( getIntent().getStringExtra("networkType") != null &&
                getIntent().getStringExtra("networkType").equalsIgnoreCase(NetworkType.BLUETOOTH.name()))
        {
            networkType = NetworkType.BLUETOOTH;
            initBluetooth();
        }
        else if( getIntent().getStringExtra("networkType") != null &&
                getIntent().getStringExtra("networkType").equalsIgnoreCase(NetworkType.WIFI_DIRECT.name()))
        {
            networkType = NetworkType.WIFI_DIRECT;
            initWifiDirect();
        }

        getNearByPlayers();

//        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, listUsers);
//        bluetoothDeviceAdapter.setDeviceClickListener(this);

        deviceLayout = (RecyclerView) findViewById(R.id.list_business);
        setDeviceLayout(deviceLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.searchable, menu);
        // Associate searchable configuration with the SearchView
        searchView = (SearchView) menu.findItem(R.id.item_list_search).getActionView();
        searchView.setOnQueryTextListener(this);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, BusinessCardListActivityUser.class)));
        searchView.setIconifiedByDefault(false);


       /* searchView.setSearchableInfo(searchManager.
                getSearchableInfo(new ComponentName(getApplicationContext(),BusinessCardListActivityUser.class)));
        searchView.setSubmitButtonEnabled(true);
      */


        return true;

    }

    private void getNearByPlayers() {
        DBParams params = new DBParams();
        params.event_ = MobiMix.DBRequest.DB_FIND_NEARBY_PLAYERS;
        params.userId_ = ApplicationSharedPreferences.getInstance(this).getValue("user_id");
        GUIManager.getObject().getNearbyPlayers(params, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1111:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bluetoothService.startDiscovery();
                }
        }
    }

    private void initBluetooth() {
        networkType = NetworkType.BLUETOOTH;

        bluetoothService.setClassName(BusinessCardListActivityUser.class.getSimpleName());

        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, listUsers);
        bluetoothDeviceAdapter.setDeviceClickListener(this);

//        bluetoothService.setNearByBluetoothDeviceAction(new NearByBluetoothDeviceFound() {
//            @Override
//            public void onBluetoothDeviceAvailable(BluetoothRemoteDevice device) {
//                listBluetoothDevices.add(device);
//
//                ChatDataConversation.putUserName(device.getDevice().getAddress(), device.getName());
//
//                UtilsHandler.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        bluetoothDeviceAdapter.notifyDataSetChanged();
//                    }
//                });
//            }
//        });
    }

    private void initWifiDirect() {
        networkType = NetworkType.WIFI_DIRECT;

        WifiDirectService.getInstance(this).setClassName(BusinessCardListActivityUser.class.getSimpleName());

        wifiDeviceAdapter = new WifiP2PDeviceAdapter(this, listUsers);
        wifiDeviceAdapter.setDeviceClickListener(this);

//        WifiDirectService.getInstance(this).setNearByDeviceFoundCallback(new NearByDeviceFound() {
//            @Override
//            public void onDevicesAvailable(Collection<WifiP2PRemoteDevice> devices) {
//                listWifiP2PDevices.clear();
//                listWifiP2PDevices.addAll(devices);
//
//                wifiDeviceAdapter.notifyDataSetChanged();
//            }
//        });
    }

    public void setDeviceLayout(RecyclerView deviceLayout) {
        deviceLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        if( networkType == NetworkType.WIFI_DIRECT ) {
            deviceLayout.setAdapter(wifiDeviceAdapter);
        }
        else {
            deviceLayout.setAdapter(bluetoothDeviceAdapter);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        bluetoothDeviceAdapter.getFilter().filter(newText);
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void sendBusinessCard(String socketAddress) {
        UtilsHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BusinessCardListActivityUser.this, "Sending business card", Toast.LENGTH_SHORT).show();
            }
        });

        WifiDirectService.getInstance(this).getMessageHandler().sendBusinessCard(socketAddress, Modules.BUSINESS_CARD);
    }

    private void setSocketListeners() {
        GUIManager.getObject().setSocketEventListener(new ISocketEventListener() {
            @Override
            public void socketInitialized(String remoteSocketAddress) {
                sendBusinessCard(remoteSocketAddress);
            }

            @Override
            public void socketDiconnected() {
                if (!BusinessCardListActivityUser.this.isFinishing() || !BusinessCardListActivityUser.this.isDestroyed()) {
                    Toast.makeText(BusinessCardListActivityUser.this, "Device is not connected, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onWifiDeviceClick(final MBNearbyPlayer device) {

        WifiP2pDevice localDevice = LocalP2PDevice.getInstance().getLocalDevice();
        if (localDevice.status == WifiP2pDevice.CONNECTED) {
            UtilsHandler.showProgressDialog("Removing previous connection and connecting with " + device.getPlayerName());
            WifiDirectService.getInstance(this).removeConnectionAndReConnect(new IWifiDisconnectionListener() {
                @Override
                public void connectionRemoved(boolean isDisconnected) {
                        UtilsHandler.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UtilsHandler.dismissProgressDialog();
                            }
                        });

                        WifiDirectService.getInstance(BusinessCardListActivityUser.this).connect(device.getEmail(), new DeviceConnectionListener() {
                            @Override
                            public void onDeviceConnected(boolean isConnected) {
                                if (isConnected) {
                                    setSocketListeners();
                                } else {
                                    UtilsHandler.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(BusinessCardListActivityUser.this, "Could not connect with " + device.getPlayerName(), Toast.LENGTH_SHORT);
                                        }
                                    });
                                }
                            }
                        });
                    }
            });
        }
        else {
            UtilsHandler.showProgressDialog("Trying to establish connection with " + device.getPlayerName());
            WifiDirectService.getInstance(BusinessCardListActivityUser.this).connect(device.getEmail(), new DeviceConnectionListener() {
                @Override
                public void onDeviceConnected(boolean isConnected) {
                    UtilsHandler.dismissProgressDialog();
                    if (isConnected) {
                        setSocketListeners();
                    } else {
                        Toast.makeText(BusinessCardListActivityUser.this, "Could not connect with " + device.getPlayerName(), Toast.LENGTH_SHORT);
                    }
                }
            });
        }
    }

    @Override
    public void onBluetoothDeviceClick(MBNearbyPlayer remoteDevice) {
        List<BluetoothRemoteDevice> availableBluetoothDevices = BluetoothService.getInstance().getBluetoothDevices();
        for(BluetoothRemoteDevice device : availableBluetoothDevices){
            if(device.getName().equalsIgnoreCase(remoteDevice.getEmail())) {
                connectedThread = new ConnectedBusinessThread(device.getDevice());
                connectedThread.start();
            }
        }
        NotificationManagerCompat.from(this).cancelAll();
    }

    @Override
    public void onDataAvailable(int resCode, List<?> data) {
        if (resCode == MobiMix.DBResponse.DB_RES_FIND_NEARBY_PLAYERS) {
            List<MBNearbyPlayer> players = (List<MBNearbyPlayer>) data;

            this.listUsers.clear();
            this.listUsers.addAll(players);

            wifiDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDataFailure() {
        Utils.showErrorDialog(this, "Players could not be retrived, Please try after some time.");
    }
}
