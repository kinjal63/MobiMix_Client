package io.connection.bluetooth.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.GameLibrary;
import io.connection.bluetooth.Domain.GameProfile;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Thread.GameEventConnectThread;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by songline on 23/11/16.
 */
public class UserNearByWithGames extends AppCompatActivity {

    Map<String, Drawable> iconDrawable = new HashMap<>();
    List<String> bluetoothAddress = new ArrayList<>();
    List<User> userListOut = new ArrayList<>();
    List<BluetoothDevice> deviceList = new ArrayList<>();
    List<String> installedPackageList = new ArrayList<>();
    List<BluetoothDevice> finalDeviceListConnection = new ArrayList<>();
    List<List<GameProfile>> finalGameList = new ArrayList<>();
    private static final String TAG = "UserNearByWithGames";
    UserListAdapter userListAdapter;
    RecyclerView userRecyclerView;
    BluetoothAdapter bluetoothAdapter;
    ApiCall apiCall;
    SharedPreferences preference;
    String userId;
    Button buttonPair;
    private RecyclerView finalRecyclerView;
    private FinalGameListAdapter finalGameListAdapter;
    private View dialogRecyclerInflater;
    private static Context mContext;
    private String name;
    private ProgressDialog mProgressDlg;
    PopupWindow popupWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_search_nearby_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        apiCall = ApiClient.getClient().create(ApiCall.class);
        preference = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
        userId = preference.getString(Constants.LOGIN_KEY, "");
        name = preference.getString(Constants.NAME_KEY, "");
        PackageManager packageManager = this.getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        buttonPair = (Button) findViewById(R.id.userCreatePairButton);
        mContext = this;

        for (ApplicationInfo applicationInfo : applicationInfoList) {

            if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                //it's a system app, not interested
            } else if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //Discard this one
                //in this case, it should be a user-installed app
            } else if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {

                Drawable icon;
                try {
                    icon = packageManager.getApplicationIcon(applicationInfo.packageName.toString());
                } catch (Exception e) {
                    icon = getResources().getDrawable(R.drawable.logo);
                }

                iconDrawable.put(applicationInfo.packageName, icon);
                installedPackageList.add(applicationInfo.packageName);


            }

        }

        userRecyclerView = (RecyclerView) findViewById(R.id.game_user_search_nearby_recyclerview);
        userListAdapter = new UserListAdapter(this, deviceList);
        dialogRecyclerInflater = getLayoutInflater().inflate(R.layout.nearbyfinal_gamelist_user, null);
        finalRecyclerView = (RecyclerView) dialogRecyclerInflater.findViewById(R.id.nearby_final_recyclerview);
        finalGameListAdapter = new FinalGameListAdapter(this);
        setDeviceLayout(userRecyclerView);
        setFinalGameListLayout(finalRecyclerView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        popupWindow = new PopupWindow(dialogRecyclerInflater, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, 1);
        } else {
            bluetoothEnabled();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


        registerReceiver(bluetoothDeviceFoundReceiver, filter);
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setIcon(R.drawable.ic_bluetooth_discover);
        mProgressDlg.setMessage("Scanning Devices ..");
        mProgressDlg.setCancelable(false);
       /* mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                bluetoothAdapter.cancelDiscovery();
            }
        });*/


        buttonPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<GameLibrary, Integer> gameCommonMap = new HashMap<GameLibrary, Integer>();
                List<GameLibrary> gameLibraryList = new ArrayList<GameLibrary>();
                for (List<GameProfile> gameProfiles : finalGameList) {
                    for (GameProfile gameProfile : gameProfiles) {
                        if (gameCommonMap.containsKey(gameProfile.getGameLibrary())) {
                            gameCommonMap.put(gameProfile.getGameLibrary(), gameCommonMap.get(gameProfile.getGameLibrary()) + 1);
                        } else
                            gameCommonMap.put(gameProfile.getGameLibrary(), 1);

                    }
                }

                for (GameLibrary library : gameCommonMap.keySet()) {
                    if (finalDeviceListConnection.size() == gameCommonMap.get(library)) {
                        gameLibraryList.add(library);
                    }
                }


                if (gameLibraryList.size() > 0) {

                    finalGameListAdapter.setFinalGameLibraryListData(gameLibraryList);

                    //  popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                    Button btnDismiss = (Button) dialogRecyclerInflater.findViewById(R.id.nearby_final_popup_closed_button);
                    btnDismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            removeActivityBackGroundColor();
                        }
                    });

                    popupWindow.showAsDropDown(dialogRecyclerInflater, 0, 0);
                    setActivityBackgroundColor();


                } else {
                    // No Common Game Found In Selected Users.


                }


            }
        });


    }

    void bluetoothEnabled() {
        //AcceptThread thread = new AcceptThread(BluetoothAdapter.getDefaultAdapter(), mContext);
        //thread.start();

        if (Build.VERSION.SDK_INT >= 21 && checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1111);
        else {
            if (bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        }

        System.out.println("after already device");
    }

    public void setActivityBackgroundColor() {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(getResources().getColor(R.color.mdtp_transparent_black));
        view.findViewById(R.id.cardview_nearbygame).setBackgroundColor(getResources().getColor(R.color.mdtp_transparent_black));
        // view.findViewById(R.id.game_user_search_nearby_recyclerview).setBackgroundColor(getResources().getColor(R.color.mdtp_transparent_black));
    }

    public void removeActivityBackGroundColor() {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(getResources().getColor(R.color.white));
        view.findViewById(R.id.cardview_nearbygame).setBackgroundColor(getResources().getColor(R.color.white));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth_discover, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.item_discover_bluetooth:
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                    Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBlueTooth, 1);
                } else {
                    bluetoothEnabled();
                }

                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try {
            unregisterReceiver(bluetoothDeviceFoundReceiver);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        try {
            unregisterReceiver(bluetoothDeviceFoundReceiver);
        } catch (Exception e) {

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

    public final BroadcastReceiver bluetoothDeviceFoundReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            Log.d("bluetoothSearch", action);
            if (action.equals(BluetoothDevice.ACTION_FOUND) && Utils.isConnected(context)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println(" Device Found " + device.getAddress() + "     " + device.getName());
                if (device != null && device.getAddress() != null && !device.getAddress().isEmpty() && !deviceList.contains(device) && !bluetoothAddress.contains(device.getAddress())) {

                    String deviceMacAddress = device.getAddress().trim();
                    bluetoothAddress.add(deviceMacAddress);
                    Call<User> result = apiCall.getNearByUserGames(userId, deviceMacAddress, installedPackageList);
                    result.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            // System.out.println("Size "+response.body().getGameProfiles().size());
                            if (response.code() == 200 && response.body() != null) {
                                User user = response.body();
                                //deviceList.add(device);
                                if (user.getGameProfiles().size() > 0) {
                                    userListAdapter.userAddWithDevice(device, user);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                        }
                    });


                }


            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {

                Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {

                deviceList.clear();
                bluetoothAddress.clear();
                userListAdapter.removeAll();
                mProgressDlg.show();
                alreadyBondedDevice();

                //  userListAdapter = new UserListAdapter(context, deviceList);

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mProgressDlg.dismiss();
            }


        }


    };

    public void alreadyBondedDevice() {
        Set<BluetoothDevice> listdevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (Utils.isConnected(mContext)) {
            for (final BluetoothDevice deviceSet : listdevice) {
                System.out.println("1 " + deviceSet.getAddress());
                if (deviceSet != null && deviceSet.getAddress() != null && !deviceSet.getAddress().isEmpty() && !deviceList.contains(deviceSet) && !bluetoothAddress.contains(deviceSet.getAddress())) {
                    System.out.println("2 " + deviceSet.getAddress());
                    String deviceMacAddress = deviceSet.getAddress().trim();
                    bluetoothAddress.add(deviceMacAddress);

                    Call<User> result = apiCall.getNearByUserGames(userId, deviceMacAddress, installedPackageList);
                    result.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            //  System.out.println("Already Bonded "+response.body().getName());
                            // System.out.println(response.body());
                            if (response.code() == 200 && response.body() != null) {
                                User user = response.body();
                                //deviceList.add(deviceSet);
                                if (user.getGameProfiles().size() > 0) {
                                    userListAdapter.userAddWithDevice(deviceSet, user);
                                }
                            } else {
                                System.out.println(" no found here in else");
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            t.getMessage();
                            t.printStackTrace();
                            Toast.makeText(getApplicationContext(), Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                        }
                    });
                }


            }
        } else {
            Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }

    }


    class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyUserViewHolder> {

        Context context = null;
        List<BluetoothDevice> devices = new ArrayList<>();
        List<User> users = new ArrayList<>();
        private RecyclerView horizontalList;
        List<Boolean> checkBoxvalue = new ArrayList<>();

        public void userAddWithDevice(BluetoothDevice device, User user) {
            System.out.println(devices.contains(device) + "    " + user.getName());
            if (!devices.contains(device)) {
                devices.add(device);
                users.add(user);
                checkBoxvalue.add(false);
                notifyDataSetChanged();

            }
        }

        public void removeAll() {
            devices.clear();
            users.clear();
            checkBoxvalue.clear();
            notifyDataSetChanged();
        }

        public void removeCheckBoxValue() {
            int n = checkBoxvalue.size();
            checkBoxvalue = new ArrayList<>(Collections.nCopies(n, false));
            notifyDataSetChanged();
        }


        public UserListAdapter(Context c, List<BluetoothDevice> deviceList) {
            context = c;
            devices = deviceList;
        }


        public class MyUserViewHolder extends RecyclerView.ViewHolder {
            public ImageView userImage;
            public TextView userName;
            Context context;
            private GameListAdapter gameListAdapter;
            private CheckBox userSelectionCheckBox;


            MyUserViewHolder(View itemView, Context context, int type) {
                super(itemView);
                if (type == 0) {
                    userImage = (ImageView) itemView.findViewById(R.id.user_nearby_image);
                    userName = (TextView) itemView.findViewById(R.id.user_nearby_name);
                    userSelectionCheckBox = (CheckBox) itemView.findViewById(R.id.checkboxNearby);
                    horizontalList = (RecyclerView) itemView.findViewById(R.id.user_search_game_recyclerView);
                    horizontalList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                    gameListAdapter = new GameListAdapter(context);
                    horizontalList.setAdapter(gameListAdapter);
                    this.context = context;
                    //itemView.setOnClickListener(this);
                } else if (type == 1) {
                    ProgressBar ppp = (ProgressBar) itemView.findViewById(R.id.searching_devices_progressbar);
                    ppp.setVisibility(View.GONE);
                    TextView ttt = (TextView) itemView.findViewById(R.id.searching_devices_message);
                    ttt.setText("No User Found !! ");
                }
            }
        }

        @Override
        public int getItemCount() {
            return devices.isEmpty() ? 1 : devices.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 && devices.isEmpty() ? 1 : 0;
        }


        @Override
        public MyUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyUserViewHolder(LayoutInflater.from(context).inflate(viewType == 0 ? R.layout.user_search_nearby_layout_list : R.layout.searching_devices, parent, false), context, viewType);
        }

        @Override
        public void onBindViewHolder(MyUserViewHolder holder, final int position) {
            System.out.println("Position => " + position);
            System.out.println("Device => " + devices.size() + "   " + Arrays.toString(devices.toArray()));
            if (holder.getItemViewType() == 0) {
                System.out.println("User List " + users.size());
                holder.userImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                holder.userImage.setImageResource(R.drawable.image20160621_121902);
                holder.userName.setText(users.get(position).getName());
                holder.itemView.setTag(devices.get(position));
                System.out.println("Game List " + users.get(position).getGameProfiles().size());
                holder.gameListAdapter.setGameListData(users.get(position).getGameProfiles());
                holder.userSelectionCheckBox.setChecked(checkBoxvalue.get(position));
                holder.userSelectionCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        if (cb.isChecked()) {

                            finalDeviceListConnection.add(devices.get(position));
                            finalGameList.add(users.get(position).getGameProfiles());
                            //Toast.makeText(context, finalDeviceListConnection.size() + "   " + finalGameList.size(), Toast.LENGTH_LONG).show();
                            buttonPair.setVisibility(View.VISIBLE);
                        } else {

                            finalDeviceListConnection.remove(devices.get(position));
                            finalGameList.remove(users.get(position).getGameProfiles());
                            //Toast.makeText(context, finalDeviceListConnection.size() + "   " + finalGameList.size(), Toast.LENGTH_LONG).show();
                            if (finalDeviceListConnection.size() == 0) {
                                buttonPair.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }


        }
    }

    static class GameListAdapter extends RecyclerView.Adapter<GameListAdapter.MyGameViewHolder> {

        private Context mContext;
        private List<GameProfile> gameProfileList = new ArrayList<>();
        PackageManager packageManager;

        public GameListAdapter(Context context) {
            mContext = context;
            packageManager = mContext.getPackageManager();
        }

        public void setGameListData(List<GameProfile> gameProfiles) {
            if (gameProfiles != null) {
                gameProfileList = gameProfiles;
            }
            notifyDataSetChanged();
        }


        @Override
        public MyGameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.user_search_nearby_game_image, parent, false);
            return new MyGameViewHolder(view);
        }

        public static class MyGameViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;

            public MyGameViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.user_search_game_ImageList);
            }
        }

        @Override
        public void onBindViewHolder(MyGameViewHolder holder, int position) {
            String packageName = gameProfileList.get(position).getGameLibrary().getPackageName();
            System.out.println(packageName);

            Drawable icon;
            try {
                icon = packageManager.getApplicationIcon(packageName.toString());
            } catch (Exception e) {
                e.printStackTrace();
                icon = mContext.getResources().getDrawable(R.drawable.ic_user);
            }
            holder.imageView.setImageDrawable(icon);
            holder.itemView.setTag(packageName);
        }

        @Override
        public int getItemCount() {
            return gameProfileList.size();
        }
    }


    public void setDeviceLayout(RecyclerView deviceLayout) {

        deviceLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        deviceLayout.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        deviceLayout.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        deviceLayout.setAdapter(userListAdapter);

    }

    // ======================== FINAL GAME LIST ======================
    class FinalGameListAdapter extends RecyclerView.Adapter<FinalGameListAdapter.FinalMyGameViewHolder> {

        private Context mContext;
        private List<GameLibrary> gameLibraries = new ArrayList<>();
        PackageManager packageManager;

        public FinalGameListAdapter(Context context) {
            mContext = context;
            packageManager = mContext.getPackageManager();
        }

        public void setFinalGameLibraryListData(List<GameLibrary> gameLibraryList) {
            if (gameLibraryList != null) {
                gameLibraries = gameLibraryList;
            }
            notifyDataSetChanged();
        }


        @Override
        public FinalMyGameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.user_search_nearby_game_image, parent, false);
            return new FinalMyGameViewHolder(view);
        }

        public class FinalMyGameViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;

            public FinalMyGameViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.user_search_game_ImageList);
            }
        }

        @Override
        public void onBindViewHolder(FinalGameListAdapter.FinalMyGameViewHolder holder, int position) {
            String packageName = gameLibraries.get(position).getPackageName();
            System.out.println(packageName);
            String gameNameValue = "";
            Drawable icon;
            try {
                icon = packageManager.getApplicationIcon(packageName.toString());
            } catch (Exception e) {
                e.printStackTrace();
                icon = mContext.getResources().getDrawable(R.drawable.ic_user);
            } finally {
                try {
                    gameNameValue = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).loadLabel(packageManager).toString();
                } catch (Exception e) {

                }
            }
            final String gameNamePackageName = gameNameValue + " $#$ " + packageName;
            holder.imageView.setImageDrawable(icon);
            holder.itemView.setTag(gameNameValue);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendMessageRequestForPlayingGame(finalDeviceListConnection, gameNamePackageName);
                    popupWindow.dismiss();
                    userListAdapter.removeCheckBoxValue();
                    removeActivityBackGroundColor();
                    final ProgressDialog ProgressDlg = new ProgressDialog(mContext);
                    ProgressDlg.setMessage("Waiting For User Response...");
                    ProgressDlg.setCancelable(false);
                    ProgressDlg.show();
                    ProgressDlg.setIndeterminate(true);
                    ProgressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            ProgressDlg.dismiss();
                        }
                    }, 5000);

                }
            });
        }


        @Override
        public int getItemCount() {
            return gameLibraries.size();
        }
    }


    public void setFinalGameListLayout(RecyclerView deviceLayout) {

        deviceLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
       /* deviceLayout.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        deviceLayout.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));*/
        deviceLayout.setAdapter(finalGameListAdapter);

    }


    void sendMessageRequestForPlayingGame(List<BluetoothDevice> deviceList, String gameName) {


        for (BluetoothDevice bluetoothDevice : deviceList) {
            GameEventConnectThread thread = new GameEventConnectThread(bluetoothDevice, 1);
            thread.start();
            thread.setGame(name, gameName);


        }


    }


}
