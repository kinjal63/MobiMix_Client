package io.connection.bluetooth.activity;

import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;
import java.util.Collection;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.R;
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
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 07/09/16.
 */
public class BusinessCardListActivityUser extends BaseActivity implements SearchView.OnQueryTextListener, DeviceClickListener {
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

    private ArrayList<WifiP2PRemoteDevice> listWifiP2PDevices = new ArrayList<>();
    private ArrayList<BluetoothRemoteDevice> listBluetoothDevices = new ArrayList<>();
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

        listBluetoothDevices.addAll(bluetoothService.getBluetoothDevices());
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, listBluetoothDevices);
        bluetoothDeviceAdapter.setDeviceClickListener(this);

        bluetoothService.setNearByBluetoothDeviceAction(new NearByBluetoothDeviceFound() {
            @Override
            public void onBluetoothDeviceAvailable(BluetoothRemoteDevice device) {
                listBluetoothDevices.add(device);

                ChatDataConversation.putUserName(device.getDevice().getAddress(), device.getName());

                UtilsHandler.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothDeviceAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void initWifiDirect() {
        networkType = NetworkType.WIFI_DIRECT;

        listWifiP2PDevices.addAll(WifiDirectService.getInstance(this).getWifiP2PDeviceList());
        wifiDeviceAdapter = new WifiP2PDeviceAdapter(this, listWifiP2PDevices);
        wifiDeviceAdapter.setDeviceClickListener(this);

        WifiDirectService.getInstance(this).setClassName(BusinessCardListActivityUser.class.getSimpleName());
        WifiDirectService.getInstance(this).setNearByDeviceFoundCallback(new NearByDeviceFound() {
            @Override
            public void onDevicesAvailable(Collection<WifiP2PRemoteDevice> devices) {
                listWifiP2PDevices.clear();
                listWifiP2PDevices.addAll(devices);

                wifiDeviceAdapter.notifyDataSetChanged();
            }
        });
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

    private void sendBusinessCard() {
        UtilsHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BusinessCardListActivityUser.this, "Sending business card to " + p2pDevice.deviceName, Toast.LENGTH_SHORT).show();
            }
        });
        WifiDirectService.getInstance(this).getMessageHandler().sendBusinessCard();
    }

    private void setSocketListeners() {
        WifiDirectService.getInstance(this).setSocketConnectionListener(new SocketConnectionListener() {
            @Override
            public void socketConnected(final boolean isClient, final String remoteDeviceAddress) {
                sendBusinessCard();
                WifiDirectService.getInstance(BusinessCardListActivityUser.this).setSocketConnectionListener(null);
            }

            @Override
            public void socketClosed() {

            }
        });
    }

    @Override
    public void onWifiDeviceClick(WifiP2PRemoteDevice device) {
        p2pDevice = device.getDevice();

        if( !WifiDirectService.getInstance(this).isSocketConnectedWithHost(p2pDevice.deviceName) ) {
            Toast.makeText(this, "Connecting to " + p2pDevice.deviceName, Toast.LENGTH_SHORT).show();

            WifiDirectService.getInstance(this).connectWithWifiAddress(p2pDevice.deviceAddress, new DeviceConnectionListener() {
                @Override
                public void onDeviceConnected(boolean isConnected) {

                    if (isConnected) {
                        setSocketListeners();
                    } else {
                        Toast.makeText(BusinessCardListActivityUser.this, "Could not connect with " + p2pDevice.deviceName, Toast.LENGTH_SHORT);
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Connected to " + p2pDevice.deviceName, Toast.LENGTH_SHORT).show();
            sendBusinessCard();
        }
    }

    @Override
    public void onBluetoothDeviceClick(BluetoothRemoteDevice... devices) {
        for(BluetoothRemoteDevice remoteDevice : devices) {
            connectedThread = new ConnectedBusinessThread(remoteDevice.getDevice());
            connectedThread.start();
        }
        NotificationManagerCompat.from(this).cancelAll();
    }

    private void closeWifiP2PSocketsIfAny() {
        WifiDirectService.getInstance(this).getMessageHandler().sendMessage(new String("NowClosing").getBytes());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WifiDirectService.getInstance(BusinessCardListActivityUser.this).getMessageHandler().closeSocket();
            }
        }, 1000);
    }

}
