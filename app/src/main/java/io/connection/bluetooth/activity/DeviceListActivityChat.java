package io.connection.bluetooth.activity;

import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Services.BluetoothService;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.Thread.ConnectedThread;
import io.connection.bluetooth.actionlisteners.NearByBluetoothDeviceFound;
import io.connection.bluetooth.actionlisteners.NearByDeviceFound;
import io.connection.bluetooth.adapter.BluetoothDeviceAdapter;
import io.connection.bluetooth.adapter.WifiP2PDeviceAdapter;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by songline on 26/08/16.
 */
public class DeviceListActivityChat extends BaseActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "MainActivity";
    BluetoothAdapter bluetoothAdapter;
    private Toolbar toolbar;
    static ConnectedThread connectedThread;

    BluetoothDeviceAdapter bluetoothDeviceAdapter;
    WifiP2PDeviceAdapter wifiDeviceAdapter;

    RecyclerView deviceLayout;
    private ArrayList<BluetoothRemoteDevice> listBluetoothDevices = new ArrayList<>();

    private ArrayList<WifiP2PRemoteDevice> listWifiP2PDevices = new ArrayList<>();

    static Context mContext;
    static BluetoothDevice device;
    private SearchView searchView;

    private NetworkType networkType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.device_layout_list_chat);

        MobileMeasurementApplication.getInstance().registerActivity(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
        ImageCache.setContext(mContext);

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

        deviceLayout = (RecyclerView) findViewById(R.id.list_chat);
        setDeviceLayout(deviceLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

                    if (bluetoothAdapter.isDiscovering())
                        bluetoothAdapter.cancelDiscovery();
                    bluetoothAdapter.startDiscovery();
                }
        }
    }

    private void initBluetooth() {
        networkType = NetworkType.BLUETOOTH;

        bluetoothService = BluetoothService.getInstance();

        listBluetoothDevices.addAll(bluetoothService.getBluetoothDevices());
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, listBluetoothDevices);

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
        WifiDirectService.getInstance(this).setModule(Modules.CHAT);
        networkType = NetworkType.WIFI_DIRECT;

        listWifiP2PDevices.addAll(WifiDirectService.getInstance(this).getWifiP2PDeviceList());
        wifiDeviceAdapter = new WifiP2PDeviceAdapter(this, listWifiP2PDevices);

        WifiDirectService.getInstance(this).setClassName(DeviceListActivityChat.class.getSimpleName());
        WifiDirectService.getInstance(this).setNearByDeviceFoundCallback(new NearByDeviceFound() {
            @Override
            public void onDevicesAvailable(Collection<WifiP2PRemoteDevice> devices) {
                listWifiP2PDevices.clear();
                listWifiP2PDevices.addAll(devices);

                wifiDeviceAdapter.notifyDataSetChanged();
            }
        });
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

    public void setDeviceLayout(RecyclerView deviceLayout) {

        deviceLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        deviceLayout.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        if( networkType == NetworkType.WIFI_DIRECT ) {
            deviceLayout.setAdapter(wifiDeviceAdapter);
        }
        else {
            deviceLayout.setAdapter(bluetoothDeviceAdapter);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        closeWifiP2PSocketsIfAny();
    }

    private void closeWifiP2PSocketsIfAny() {
        WifiDirectService.getInstance(this).getMessageHandler().sendMessage(new String("NowClosing").getBytes());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WifiDirectService.getInstance(DeviceListActivityChat.this).getMessageHandler().socketClosed();
            }
        }, 1000);
    }
}

