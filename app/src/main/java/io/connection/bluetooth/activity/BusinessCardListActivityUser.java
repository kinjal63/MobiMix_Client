package io.connection.bluetooth.activity;

import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.Thread.ConnectedBusinessThread;
import io.connection.bluetooth.actionlisteners.DeviceConnectionListener;
import io.connection.bluetooth.actionlisteners.NearByDeviceFound;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
import io.connection.bluetooth.adapter.WifiP2PDeviceAdapter;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by songline on 07/09/16.
 */
public class BusinessCardListActivityUser extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "BusinessCardListActivit";
    BluetoothAdapter bluetoothAdapter;
    private Toolbar toolbar;
    static ConnectedBusinessThread connectedThread;

    static DeviceAdapter deviceAdapter;
    WifiP2PDeviceAdapter wifiDeviceAdapter;

    private ArrayList<BluetoothDevice> listBluetoothDevices = new ArrayList<>();
    private ArrayList<BluetoothDevice> listTempBluetoothDevices = new ArrayList<>();

    static RecyclerView deviceLayout;
    ApiCall apiCall;
//    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
//    private ArrayList<BluetoothDevice> tempbluetoothDevices = new ArrayList<>();
    static Context mContext;
    static BluetoothDevice device;
    static WifiP2pDevice P2Pdevice;
    private SearchView searchView;

    private ArrayList<WifiP2pDevice> listWifiP2PDevices = new ArrayList<>();
    private NetworkType networkType;

    private boolean isBluetoothReceiverRegistered;


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
//        if( networkType == NetworkType.WIFI_DIRECT ) {
//            closeWifiP2PSocketsIfAny();
//        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: " + resultCode);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK)
                bluetoothEnabled();

        } else {

            Toast.makeText(getApplicationContext(), "Please Turn On Bluetooth ", Toast.LENGTH_LONG).show();
            finish();
        }
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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, 1);
        } else {
            bluetoothEnabled();
        }
        deviceAdapter = new DeviceAdapter(this, listBluetoothDevices);

        registerReceiver(bluetoothDeviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        isBluetoothReceiverRegistered = true;
    }

    private void initWifiDirect() {
        WifiDirectService.getInstance(this).setModule(Modules.BUSINESS_CARD);
        networkType = NetworkType.WIFI_DIRECT;

        listWifiP2PDevices.addAll(WifiDirectService.getInstance(this).getWifiP2PDeviceList());
        wifiDeviceAdapter = new WifiP2PDeviceAdapter(this, listWifiP2PDevices);

        WifiDirectService.getInstance(this).setNearByDeviceFoundCallback(new NearByDeviceFound() {
            @Override
            public void onDevicesAvailable(Collection<WifiP2pDevice> devices) {
                listWifiP2PDevices.clear();
                listWifiP2PDevices.addAll(devices);
            }
        });
    }

    void bluetoothEnabled() {
        alreadyBondedDevice();

        if (Build.VERSION.SDK_INT >= 21)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1111);
        else {
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        deviceAdapter.getFilter().filter(newText);
        return false;
    }

    public static class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> implements Filterable {
        List<String> names = new ArrayList<>();
        List<String> status = new ArrayList<>();
        FriendFilter friendFilter;
        static Context mContext;
        List<BluetoothDevice> devices = new ArrayList<>();
        List<String> listName = new ArrayList<>();
        List<BluetoothDevice> listDevice = new ArrayList<>();

        public DeviceAdapter(Context mContext, List<BluetoothDevice> devices) {
            this.mContext = mContext;
            this.devices = devices;
            getFilter();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(viewType == 0 ? R.layout.business_layout_single_user : R.layout.searching_devices, parent, false), mContext, viewType);
        }

        public void add(String name, String Status, BluetoothDevice device) {
            names.add(name);
            status.add(Status);
            devices.add(device);
            listName.add(name);
            listDevice.add(device);
            notifyDataSetChanged();
        }


        @Override
        public int getItemViewType(int position) {
            return position == 0 && names.isEmpty() ? 1 : 0;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                holder.nameTV.setText(names.get(position));
                holder.itemView.setTag(devices.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return names.isEmpty() ? 1 : names.size();
        }

        @Override
        public Filter getFilter() {
            if (friendFilter == null) {
                friendFilter = new FriendFilter();
            }
            return friendFilter;
        }


        private class FriendFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                Map<BluetoothDevice, String> map = new HashMap<>();
                if (constraint != null && constraint.length() > 0 && constraint.toString().trim().length() > 0) {
                    ArrayList<String> tempList = new ArrayList<String>();
                    int i = 0;
                    // search content in friend list
                    for (String user : listName) {
                        if (user.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            //tempList.add(user);
                            map.put(listDevice.get(i), user);
                            i++;
                        } else {
                            i++;
                        }
                    }
                    filterResults.count = map.size();
                    filterResults.values = map;
                } else {
                    int i = 0;
                    for (String user : listName) {
                        map.put(listDevice.get(i++), user);
                    }
                    filterResults.count = map.size();
                    filterResults.values = map;
                }
                return filterResults;
            }

            /**
             * Notify about filtered list to ui
             *
             * @param constraint text
             * @param results    filtered result
             */
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Map<BluetoothDevice, String> objDeviceMap = (Map) results.values;
                names.clear();
                names.addAll(objDeviceMap.values());
                devices.clear();
                devices.addAll(objDeviceMap.keySet());
                notifyDataSetChanged();

            }
        }


        static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView nameTV;
            ImageView imageView;
            public Context context;


            public ViewHolder(View itemView, Context context, int type) {
                super(itemView);
                if (type == 0) {
                    nameTV = (TextView) itemView.findViewById(R.id.business_user_name);
                    this.context = context;
                    itemView.setOnClickListener(this);
                }
            }

            @Override
            public void onClick(View v) {

                device = (BluetoothDevice) v.getTag();
                Log.d(TAG, "onClick: " + ((BluetoothDevice) v.getTag()).getAddress() + "    " + ((BluetoothDevice) v.getTag()).getName());
                ImageCache.setContext(context);
                connectedThread = new ConnectedBusinessThread(device);
                connectedThread.start();
                NotificationManagerCompat.from(context).cancelAll();
            }


        }

    }


    public void setDeviceLayout(RecyclerView deviceLayout) {

        deviceLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //deviceLayout.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        if( networkType == NetworkType.WIFI_DIRECT ) {
            deviceLayout.setAdapter(wifiDeviceAdapter);
        }
        else {
            deviceLayout.setAdapter(deviceAdapter);
        }

    }

    public static ConnectedBusinessThread getCurrentThread() {
        return connectedThread;
    }

    public final BroadcastReceiver bluetoothDeviceFoundReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: bluetooth receiver here");
            Log.d("bluetooth", action);
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                final BluetoothDevice deviceSet = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


                /*if (!tempbluetoothDevices.contains(device)) {
                    tempbluetoothDevices.add(device);
                    deviceAdapter.add(device.getName(), "Not Connected", device);
                    Log.d(TAG, "onReceive: " + device.getAddress() + "  " + device.getName() + "    ");
                }*/
                if (Utils.isConnected(context)) {
                    if (device != null) {
                        String deviceMacAddress = device.getAddress().trim();
                        Log.d(TAG, "onReceive: " + deviceMacAddress);
                        if (!listTempBluetoothDevices.contains(deviceSet)) {
                            User userAvailable = new User();
                            userAvailable.setMacAddress(deviceSet.getAddress());
                            userAvailable.setEmail(deviceSet.getName());
                            Call<User> name = apiCall.isAvailable(userAvailable);
                            name.enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    User user = response.body();
                                    if (user != null) {
                                        listTempBluetoothDevices.add(deviceSet);
                                        deviceAdapter.add(user.getName(), "Not Connected", deviceSet);
                                    }

                                }

                                @Override
                                public void onFailure(Call<User> call, Throwable t) {
                                    Log.d(TAG, "onFailure: " + t.getMessage());
                                    Toast.makeText(getApplicationContext(), Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();

                                }
                            });
                        }

                    }
                } else {
                    Toast.makeText(context, Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                }
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( isBluetoothReceiverRegistered ) {
            unregisterReceiver(bluetoothDeviceFoundReceiver);
        }
        closeWifiP2PSocketsIfAny();
    }


    public void alreadyBondedDevice() {
        Set<BluetoothDevice> listdevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (Utils.isConnected(mContext)) {
            for (final BluetoothDevice deviceSet : listdevice) {

                if (!listTempBluetoothDevices.contains(deviceSet)) {
                    User userAvailable = new User();
                    userAvailable.setMacAddress(deviceSet.getAddress());
                    userAvailable.setEmail(deviceSet.getName());
                    Call<User> name = apiCall.isAvailable(userAvailable);
                    name.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            User user = response.body();

                            if (user != null) {
                                Log.d(TAG, "onResponse: " + user.getName());
                                listTempBluetoothDevices.add(deviceSet);
                                deviceAdapter.add(user.getName(), "Paired", deviceSet);
                            }

                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t.getMessage());
                            Toast.makeText(getApplicationContext(), Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();

                        }
                    });
                }


            }
        } else {
            Toast.makeText(mContext, Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if( intent != null && intent.getParcelableExtra("device") != null) {
            P2Pdevice = intent.getParcelableExtra("device");

            if( !WifiDirectService.getInstance(this).isSocketConnectedWithHost(P2Pdevice.deviceName) ) {
                Toast.makeText(this, "Connecting to " + P2Pdevice.deviceName, Toast.LENGTH_SHORT).show();

                WifiDirectService.getInstance(this).setSocketConnectionListener(new SocketConnectionListener() {
                    @Override
                    public void socketConnected(final boolean isClient) {
                        UtilsHandler.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(isClient) {
                                    Toast.makeText(BusinessCardListActivityUser.this, "Sending business card to " + P2Pdevice.deviceName, Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(BusinessCardListActivityUser.this, "Receiving business card", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        sendBusinessCard();
                    }
                });
                WifiDirectService.getInstance(this).connectWithWifiAddress(P2Pdevice.deviceAddress, new DeviceConnectionListener() {
                    @Override
                    public void onDeviceConnected(boolean isConnected) {

                        if (isConnected) {
                            Toast.makeText(BusinessCardListActivityUser.this, "Connected with " + P2Pdevice.deviceName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BusinessCardListActivityUser.this, "Could not connect with " + P2Pdevice.deviceName, Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
            else {
                sendBusinessCard();
            }
        }
    }

    private void sendBusinessCard() {
        WifiDirectService.getInstance(this).getMessageHandler().sendBusinessCard();
    }

    private void closeWifiP2PSocketsIfAny() {
        WifiDirectService.getInstance(this).getMessageHandler().sendMessage(new String("NowClosing").getBytes());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WifiDirectService.getInstance(BusinessCardListActivityUser.this).closeSocket();
                removeWifiP2PConnection();
            }
        }, 500);
    }

    private void removeWifiP2PConnection() {
        WifiDirectService.getInstance(this).removeGroup();
    }

}
