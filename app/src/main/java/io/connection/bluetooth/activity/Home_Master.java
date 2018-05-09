package io.connection.bluetooth.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.core.GPSTracker;
import io.connection.bluetooth.core.MobiMixService;
import io.connection.bluetooth.Thread.AcceptBusinessThread;
import io.connection.bluetooth.Thread.AcceptThread;
import io.connection.bluetooth.Thread.GameEventAcceptThread;
import io.connection.bluetooth.Thread.ThreadConnection;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.GPSTrackerUtil;
import io.connection.bluetooth.utils.NotificationUtil;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by songline on 16/10/16.
 */
public class Home_Master extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "Home_Master";
    BluetoothAdapter bluetoothAdapter;
    static boolean checkThread = false;
    private Context context;
    ApiCall apiCall;
    private GPSTrackerUtil gpsTrackerUtil;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_layout);

        MobiMixApplication.getInstance().registerActivity(this);
        NotificationUtil.initialize(this);

        requestPermissions();

        getSupportActionBar().setIcon(R.mipmap.ic_logo);
        findViewById(R.id.file_card_id).setOnClickListener(this);
        findViewById(R.id.game_card_id).setOnClickListener(this);
        findViewById(R.id.business_card_id).setOnClickListener(this);
        findViewById(R.id.chat_card_id).setOnClickListener(this);
        findViewById(R.id.user_availability_id).setOnClickListener(this);
        findViewById(R.id.data_usage_card_id).setOnClickListener(this);
        ImageCache.setContext(this);
        Intent intent = new Intent(this, GPSTracker.class);

        startMobiMixService();

        this.startService(intent);
        apiCall = ApiClient.getClient().create(ApiCall.class);
        context = this;

        if (!new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").exists()) {
            new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").mkdir();
            new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/BusinessCard").mkdir();
            new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/MediaFiles").mkdir();
        }

        SharedPreferences preferences = this.getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//        if (preferences.getBoolean("is_login", false)) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
                bluetoothAdapter.setName(preferences.getString(Constants.NAME_KEY, ""));
                Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBlueTooth, 1);
            } else {
                bluetoothEnabled();
            }

            List<String> InstalledpackageName = Utils.getAllInstalledGames(this);
            if (Utils.isConnected(context)) {
                Call<User> userCall = apiCall.updateGameProfiles(preferences.getString(Constants.LOGIN_KEY, ""), InstalledpackageName);
                userCall.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {

                        if (response.body() == null) {
                            Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                        } else {
                            System.out.println(response.body().getId() + "    " + response.body().getName());
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
//        }

        this.mHandler = new Handler();
        gpsTrackerUtil = new GPSTrackerUtil(this, this.mHandler);

        sendAllAppdetail();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1112);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, 1113);
    }

    private void startMobiMixService() {
        Intent serviceIntent = new Intent(this, MobiMixService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setBluetoothAdapterName();
        Utils.makeDeviceDiscoverable(context);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1112:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            case 1113:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsTrackerUtil.getLocation();
                }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    void bluetoothEnabled() {

        if (!checkThread) {
//            ThreadConnection connection = new ThreadConnection(this);
//            connection.start();
//
//            AcceptBusinessThread acceptBusinessThread = new AcceptBusinessThread(BluetoothAdapter.getDefaultAdapter(), this);
//            acceptBusinessThread.start();
//
//            AcceptThread thread = new AcceptThread(BluetoothAdapter.getDefaultAdapter(), this);
//            thread.start();

            // start listening for game events
            GameEventAcceptThread gameEventAcceptThread = new GameEventAcceptThread(BluetoothAdapter.getDefaultAdapter() );
            gameEventAcceptThread.start();

            checkThread = true;
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_user_profile:
                Intent intent = new Intent(this, UserProfile.class);
                this.startActivity(intent);
                break;
        }

        return true;
    }


    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: " + v.getId());
        switch (v.getId()) {
            case R.id.file_card_id:
                Intent fileIntent = new Intent(this, DialogActivity.class);
                fileIntent.putExtra("module", Modules.FILE_SHARING.name());
                startActivity(fileIntent);
                break;
            case R.id.chat_card_id:
                Intent chatIntent = new Intent(this, DialogActivity.class);
                chatIntent.putExtra("module", Modules.CHAT.name());
                startActivity(chatIntent);
                break;
            case R.id.business_card_id:
                Intent businessIntent = new Intent(this, BusinessCard.class);
                startActivity(businessIntent);
                break;
            case R.id.game_card_id:
                Intent gameIntent = new Intent(this, PlayerListActivity.class);
                startActivity(gameIntent);
                break;
            case R.id.user_availability_id:
                Intent userAvailabilityIntent = new Intent(this, TimeAvailabilityActivity.class);
                startActivity(userAvailabilityIntent);
                break;
            case R.id.data_usage_card_id:
                Intent dataUsageIntent = new Intent(this, MobileDataUsageActivity.class);
                startActivity(dataUsageIntent);
                break;

        }
    }

    public void ReceiveMessage(String Message, final BluetoothSocket socket) {
        System.out.println(Message);
        String[] response = Message.split(":");
        if (Message.startsWith("Response")) {


        } else if (Message.startsWith("Request")) {
            System.out.println("here");
            System.out.println(Arrays.toString(response));
            Intent intent = new Intent(ImageCache.getContext(), UserResponseDialog.class);
            intent.putExtra("displayString", "Are You Want To Play " + response[1].split("\\$\\#\\$")[0] + " With " + response[2] + " ? ");
            intent.putExtra("device", socket.getRemoteDevice());
            intent.putExtra("packageName", response[1].split("\\$\\#\\$")[1]);
            ImageCache.getContext().startActivity(intent);
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAllAppdetail() {
        List<PackageInfo> packList = getPackageManager().getInstalledPackages(0);
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < packList.size(); i++) {
            PackageInfo packInfo = packList.get(i);
            if ((packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                String packageName = packInfo.applicationInfo.packageName;

                System.out.println(packageName);
                String versionName = String.valueOf(packInfo.versionCode);

                try {
                    JSONObject jsonObjecvt = new JSONObject();
                    jsonObjecvt.put("appName", appName);
                    jsonObjecvt.put("appPackageName", packageName);
                    jsonObjecvt.put("appVersion", versionName);

                    jsonArray.put(jsonObjecvt);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("App № " + Integer.toString(i), packageName);
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    URL url = new URL(Constants.endPointAddress + "saveAppData");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Method", "POST");
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("appDetail", jsonArray);
                    jsonObject.put("latitude", gpsTrackerUtil.getLatitude());
                    jsonObject.put("longitude", gpsTrackerUtil.getLongitude());
                    jsonObject.put("userId", ApplicationSharedPreferences.getInstance(Home_Master.this).getValue("user_id"));

                    OutputStream os = conn.getOutputStream();
                    DataOutputStream wr = new DataOutputStream(
                            conn.getOutputStream());
                    wr.writeBytes(jsonObject.toString());
                    wr.flush();
                    wr.close();
                    os.close();

                    conn.connect();

                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {

                        Snackbar.make(Home_Master.this.findViewById(android.R.id.content),
                                "Call logs are recorded.",
                                Snackbar.LENGTH_LONG)
                                .show();
//                        callLog1Duration = 0;
//                        callLog2Duration = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //Close connection if any
        WifiDirectService.getInstance(this).closeConnection();
        super.onDestroy();
    }
}
