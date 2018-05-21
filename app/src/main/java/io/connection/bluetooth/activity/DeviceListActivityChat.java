package io.connection.bluetooth.activity;

import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.connection.bluetooth.Database.DBParams;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.activity.gui.GUIManager;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.actionlisteners.DeviceClickListener;
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
import io.connection.bluetooth.utils.GameConstants;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 26/08/16.
 */
public class DeviceListActivityChat extends BaseActivity implements SearchView.OnQueryTextListener,
        DeviceClickListener, IDBResponse {
    private static final String TAG = "MainActivity";

    BluetoothDeviceAdapter bluetoothDeviceAdapter;
    WifiP2PDeviceAdapter wifiDeviceAdapter;

    RecyclerView deviceLayout;
    private ArrayList<MBNearbyPlayer> listBluetoothDevices = new ArrayList<>();

    private ArrayList<MBNearbyPlayer> listUsers = new ArrayList<>();

    static Context mContext;
    private SearchView searchView;

    private NetworkType networkType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.device_layout_list_chat);

        MobiMixApplication.getInstance().registerActivity(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
        ImageCache.setContext(mContext);


        if( getIntent().getStringExtra("networkType") != null &&
                getIntent().getStringExtra("networkType").equalsIgnoreCase(NetworkType.BLUETOOTH.name()))
        {
            initBluetooth();
        }
        else if( getIntent().getStringExtra("networkType") != null &&
                getIntent().getStringExtra("networkType").equalsIgnoreCase(NetworkType.WIFI_DIRECT.name()))
        {
            initWifiDirect();
        }
        getNearByPlayers();

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
                    bluetoothService.startDiscovery();
                }
        }
    }

    private void getNearByPlayers() {
        DBParams params = new DBParams();
        params.event_ = MobiMix.DBRequest.DB_FIND_NEARBY_PLAYERS;
        params.userId_ = ApplicationSharedPreferences.getInstance(this).getValue("user_id");
        GUIManager.getObject().getNearbyPlayers(params, this);
    }

    private void initBluetooth() {
        networkType = NetworkType.BLUETOOTH;

        bluetoothService.setClassName(DeviceListActivityChat.class.getSimpleName());

        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, listBluetoothDevices);
        bluetoothDeviceAdapter.setDeviceClickListener(this);

//        bluetoothService.setNearByBluetoothDeviceAction(new NearByBluetoothDeviceFound() {
//            @Override
//            public void onBluetoothDeviceAvailable(MBNearbyPlayer device) {
//                listBluetoothDevices.add(device);
//
//                ChatDataConversation.putUserName(device.getEmail(), device.getPlayerName());
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

        WifiDirectService.getInstance(this).setModule(Modules.CHAT);

        wifiDeviceAdapter = new WifiP2PDeviceAdapter(this, listUsers);
        wifiDeviceAdapter.setDeviceClickListener(this);

        WifiDirectService.getInstance(this).setClassName(DeviceListActivityChat.class.getSimpleName());
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
    public void onWifiDeviceClick(MBNearbyPlayer device) {
        Intent intent = new Intent();

        intent.setClass(mContext, WifiP2PChatActivity.class);
        intent.putExtra("device", device);
        intent.putExtra("networkType", NetworkType.WIFI_DIRECT.name());
        startActivity(intent);
    }

    @Override
    public void onBluetoothDeviceClick(MBNearbyPlayer device) {
        Intent intent = new Intent();

        intent.setClass(mContext, DeviceChatActivity.class);
        intent.putExtra("device", device);
        intent.putExtra("networkType", NetworkType.BLUETOOTH.name());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        closeWifiP2PSocketsIfAny();
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

