package io.connection.bluetooth.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.Thread.ThreadConnection;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "MainActivity";
    BluetoothAdapter bluetoothAdapter;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    DeviceAdapter deviceAdapter;
    RecyclerView deviceLayout;
    private ArrayList<BluetoothDevice> tempbluetoothDevices = new ArrayList<>();
    ApiCall apiCall;
    private SearchView searchView;
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private static BottomSheetBehavior mBottomSheetBehavior;
    Context mContext;
    Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onStart();
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
        activity = this;
        ImageCache.setContext(mContext);

        deviceLayout = (RecyclerView) findViewById(R.id.footer);
        deviceAdapter = new DeviceAdapter(this, bluetoothDevices);
        setDeviceLayout(deviceLayout);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, 1);
        } else {
            bluetoothEnabled();
        }
        final View footerView = findViewById(R.id.footer_device);


       /* Set<BluetoothDevice> listdevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();

        for (BluetoothDevice deviceSet : listdevice) {
            bluetoothDevices.add(deviceSet);
            deviceAdapter.add(deviceSet.getName());
        }*/

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        apiCall = ApiClient.getClient().create(ApiCall.class);

        WifiDirectService.getInstance(this).setModule(Modules.FILE_SHARING);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(bluetoothDeviceFoundReceiver, filter);

        Button buttonsend = (Button) findViewById(R.id.buttonsend);
        buttonsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior = BottomSheetBehavior.from(footerView);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                View view = findViewById(R.id.footerSend);
                mBottomSheetBehavior = BottomSheetBehavior.from(view);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                alreadyBondedDevice();
                if (Build.VERSION.SDK_INT >= 21 && checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1111);
                else {
                    if (bluetoothAdapter.isDiscovering())
                        bluetoothAdapter.cancelDiscovery();
                    bluetoothAdapter.startDiscovery();
                }
            }
        });

        ImageView image = (ImageView) findViewById(R.id.cancelbutton);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = findViewById(R.id.footerSend);
                mBottomSheetBehavior = BottomSheetBehavior.from(view);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });


        ImageView imagedelete = (ImageView) findViewById(R.id.deletebutton);
        imagedelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.final_dialog_box);
                dialog.setTitle("Delete Files ... ");


                TextView textViewName = (TextView) dialog.getWindow().findViewById(R.id.sendmessgae);
                textViewName.setText("Are You Sure want to delete files ?");

                if (!ImageCache.canShowImage()) {
                    LinearLayout layout = (LinearLayout) dialog.getWindow().findViewById(R.id.layout_image_final_checkbox);
                    TextView textview = (TextView) dialog.getWindow().findViewById(R.id.imageText);
                    layout.setEnabled(false);
                    textview.setEnabled(false);
                    layout.setVisibility(View.GONE);
                    textview.setVisibility(View.GONE);
                } else {
                    CheckBox checkBox = (CheckBox) dialog.getWindow().findViewById(R.id.image_final_checkbox);
                    checkBox.setChecked(true);
                }
                if (!ImageCache.canShowMusic()) {
                    LinearLayout layout = (LinearLayout) dialog.getWindow().findViewById(R.id.layout_audio_final_checkbox);
                    TextView textview = (TextView) dialog.getWindow().findViewById(R.id.audioText);
                    layout.setVisibility(View.GONE);
                    textview.setVisibility(View.GONE);
                    layout.setEnabled(false);
                    textview.setEnabled(false);

                } else {
                    CheckBox checkBox = (CheckBox) dialog.getWindow().findViewById(R.id.audio_final_checkbox);
                    checkBox.setChecked(true);
                }
                if (!ImageCache.canShowVideo()) {
                    LinearLayout layout = (LinearLayout) dialog.getWindow().findViewById(R.id.layout_video_final_checkbox);
                    TextView textview = (TextView) dialog.getWindow().findViewById(R.id.videoText);
                    layout.setEnabled(false);
                    textview.setEnabled(false);
                    layout.setVisibility(View.GONE);
                    textview.setVisibility(View.GONE);
                } else {
                    CheckBox checkBox = (CheckBox) dialog.getWindow().findViewById(R.id.video_final_checkbox);
                    checkBox.setChecked(true);
                }

                Button imSure = (Button) dialog.getWindow().findViewById(R.id.imSure);
                imSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox imageCheckbox = (CheckBox) dialog.getWindow().findViewById(R.id.image_final_checkbox);
                        CheckBox audioCheckbox = (CheckBox) dialog.getWindow().findViewById(R.id.audio_final_checkbox);
                        CheckBox videoCheckbox = (CheckBox) dialog.getWindow().findViewById(R.id.video_final_checkbox);

                        if (imageCheckbox.isChecked()) {
                            int i = 0;
                            Set<String> strings = ImageCache.getImageCheckBox().keySet();
                            for (String check : strings) {
                                Log.d(TAG, "onClick: " + check);
                                if (ImageCache.getImageCheckBox(check)) {
                                    ImageCache.addDeleteImage("image", check);
                                    ImagesFragment.removeImages(i, check);
                                }
                                i++;
                            }

                        }
                        if (audioCheckbox.isChecked()) {
                            int i = 0;
                            for (String check : ImageCache.getAudioCheckBox().keySet()) {
                                if (ImageCache.getAudioCheckBox(check)) {
                                    ImageCache.addDeleteImage("audio", check);
                                    AudioFragment.removeAudio(check);
                                }
                                i++;
                            }


                        }
                        if (videoCheckbox.isChecked()) {
                            int i = 0;
                            for (String check : ImageCache.getVideoCheckBox().keySet()) {
                                if (ImageCache.getVideoCheckBox(check)) {
                                    ImageCache.addDeleteImage("video", check);
                                    VideosFragment.removeVideo(check);
                                }
                                i++;
                            }
                        }


                        ImageCache.setContext(getBaseContext());
                        for (String str : ImageCache.getDeleteImages().keySet()) {

                            if (str.equals("image")) {
                                for (String check : ImageCache.getDeleteImages().get(str)) {
                                    Log.d(TAG, "onClick: " + ImageCache.getImageCheckBox().remove(check));
                                    mContext.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + "='" + check + "'", null);
                                    deleteFiles(check);
                                }
                            }
                            if (str.equals("audio")) {
                                for (String check : ImageCache.getDeleteImages().get(str)) {
                                    Log.d(TAG, "onClick: " + ImageCache.getAudioCheckBox().remove(check));
                                    mContext.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + "='" + check + "'", null);
                                    deleteFiles(check);
                                }
                            }
                            if (str.equals("video")) {
                                for (String check : ImageCache.getDeleteImages().get(str)) {
                                    Log.d(TAG, "onClick: " + ImageCache.getVideoCheckBox().remove(check));
                                    mContext.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + "='" + check + "'", null);
                                    deleteFiles(check);
                                }
                            }


                        }

                        ImageCache.getDeleteImages().clear();

                        dialog.dismiss();
                    }
                });

                Button notSure = (Button) dialog.getWindow().findViewById(R.id.notSure);
                notSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();

                    }
                });

                dialog.show();


            }
        });
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED}, 1112);

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
                break;
            case 1112:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
        }
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

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ImagesFragment(), "Images");
        adapter.addFragment(new AudioFragment(), "Music");
        adapter.addFragment(new VideosFragment(), "Videos");
        //  adapter.addFragment(new FilesFragment(), "Files");

        viewPager.setAdapter(adapter);
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

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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


    void bluetoothEnabled() {

        //ThreadConnection connection = new ThreadConnection(mContext);
        //connection.start();
        //  AcceptThread accept = new AcceptThread(BluetoothAdapter.getDefaultAdapter(), this);
        // accept.start();

        // getSupportFragmentManager().beginTransaction().replace(R.id.content, new ImagesFragment()).commit();

    }

    public static class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> implements Filterable {
        List<String> names = new ArrayList<>();
        Context mContext;
        List<BluetoothDevice> devices = new ArrayList<>();
        FriendFilter friendFilter;
        List<String> listName = new ArrayList<>();
        List<BluetoothDevice> listDevice = new ArrayList<>();

        public DeviceAdapter(Context mContext, List<BluetoothDevice> devices) {
            this.mContext = mContext;
            this.devices = devices;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(viewType == 0 ? R.layout.device_layout : R.layout.searching_devices, parent, false), mContext, viewType);
        }

        public void add(String name, BluetoothDevice device) {
            names.add(name);
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
                    nameTV = (TextView) itemView.findViewById(R.id.input_name);
                    this.context = context;
                    itemView.setOnClickListener(this);
                }
            }

            @Override
            public void onClick(View v) {


                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.final_dialog_box);
                dialog.setTitle("Transfer File ... ");
                final BluetoothDevice device = (BluetoothDevice) v.getTag();

                TextView textViewName = (TextView) dialog.getWindow().findViewById(R.id.sendmessgae);
                textViewName.setText("Are You Sure Want to Send Below Files to  " + nameTV.getText() + " ?");

                if (!ImageCache.canShowImage()) {
                    LinearLayout layout = (LinearLayout) dialog.getWindow().findViewById(R.id.layout_image_final_checkbox);
                    TextView textview = (TextView) dialog.getWindow().findViewById(R.id.imageText);
                    layout.setEnabled(false);
                    textview.setEnabled(false);
                    layout.setVisibility(View.GONE);
                    textview.setVisibility(View.GONE);
                } else {
                    CheckBox checkBox = (CheckBox) dialog.getWindow().findViewById(R.id.image_final_checkbox);
                    checkBox.setChecked(true);
                }
                if (!ImageCache.canShowMusic()) {
                    LinearLayout layout = (LinearLayout) dialog.getWindow().findViewById(R.id.layout_audio_final_checkbox);
                    TextView textview = (TextView) dialog.getWindow().findViewById(R.id.audioText);
                    layout.setVisibility(View.GONE);
                    textview.setVisibility(View.GONE);
                    layout.setEnabled(false);
                    textview.setEnabled(false);

                } else {
                    CheckBox checkBox = (CheckBox) dialog.getWindow().findViewById(R.id.audio_final_checkbox);
                    checkBox.setChecked(true);
                }
                if (!ImageCache.canShowVideo()) {
                    LinearLayout layout = (LinearLayout) dialog.getWindow().findViewById(R.id.layout_video_final_checkbox);
                    TextView textview = (TextView) dialog.getWindow().findViewById(R.id.videoText);
                    layout.setEnabled(false);
                    textview.setEnabled(false);
                    layout.setVisibility(View.GONE);
                    textview.setVisibility(View.GONE);
                } else {
                    CheckBox checkBox = (CheckBox) dialog.getWindow().findViewById(R.id.video_final_checkbox);
                    checkBox.setChecked(true);
                }

                Button imSure = (Button) dialog.getWindow().findViewById(R.id.imSure);
                imSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CheckBox imageCheckbox = (CheckBox) dialog.getWindow().findViewById(R.id.image_final_checkbox);
                        CheckBox audioCheckbox = (CheckBox) dialog.getWindow().findViewById(R.id.audio_final_checkbox);
                        CheckBox videoCheckbox = (CheckBox) dialog.getWindow().findViewById(R.id.video_final_checkbox);

                        if (imageCheckbox.isChecked()) {
                            for (String check : ImageCache.getImageCheckBox().keySet()) {
                                if (ImageCache.getImageCheckBox(check)) {
                                    ImageCache.putUri(device.getAddress(), Uri.parse(check));
                                    ImageCache.setImageCheckBoxValue(check, false);
                                }
                            }
                        } else {
                            for (String check : ImageCache.getImageCheckBox().keySet()) {
                                ImageCache.setImageCheckBoxValue(check, false);
                            }
                        }
                        ImagesFragment.updateChekBox();

                        if (audioCheckbox.isChecked()) {
                            for (String check : ImageCache.getAudioCheckBox().keySet()) {
                                if (ImageCache.getAudioCheckBox(check)) {
                                    ImageCache.putUri(device.getAddress(), Uri.parse(check));
                                    ImageCache.setAudioCheckBoxValue(check, false);
                                }
                            }
                        } else {
                            for (String check : ImageCache.getAudioCheckBox().keySet()) {
                                ImageCache.setAudioCheckBoxValue(check, false);
                            }
                        }

                        AudioFragment.updateCheckbox();


                        if (videoCheckbox.isChecked()) {
                            for (String check : ImageCache.getVideoCheckBox().keySet()) {
                                if (ImageCache.getVideoCheckBox(check)) {
                                    ImageCache.putUri(device.getAddress(), Uri.parse(check));
                                    ImageCache.setVideoCheckBoxValue(check, false);
                                }
                            }
                        } else {
                            for (String check : ImageCache.getVideoCheckBox().keySet()) {
                                ImageCache.setVideoCheckBoxValue(check, false);
                            }

                        }
                        VideosFragment.updateCheckbox();
                        ImagesFragment.count = 0;
                        ImagesFragment.countText.setText("");
                        // ImagesFragment.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        //ImagesFragment.mBottomSheetBehaviorforFooter.setState(BottomSheetBehavior.STATE_COLLAPSED);


                        ImageCache.setContext(context);
                        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            Log.d(TAG, "onClick:  size of file " + ImageCache.getUriList(device.getAddress()));
                            List<Uri> listSendFiless = new ArrayList<Uri>();
                            for (Uri uri : ImageCache.getUriList(device.getAddress())) {
                                listSendFiless.add(uri);
                            }
                            ThreadConnection connection = new ThreadConnection(context);
                            connection.connect(device, true, listSendFiless);
                            // ConnectedThread connectedThread = new ConnectedThread(device, listSendFiless);
                            Log.d(TAG, "onDrag: Started Without sending Request");
                            // connectedThread.start();
                            ImageCache.getUriList(device.getAddress()).clear();
                        } else {
                            System.out.println(" here pairing");
                            createPairing(device);

                        }

                        NotificationManagerCompat.from(context).cancelAll();
                        dialog.dismiss();
                    }
                });

                Button notSure = (Button) dialog.getWindow().findViewById(R.id.notSure);
                notSure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();

                    }
                });

                dialog.show();

            }

        }
    }

    public void setDeviceLayout(RecyclerView deviceLayout) {
        deviceLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        deviceLayout.setAdapter(deviceAdapter);
    }

    public static void createPairing(BluetoothDevice device) {
        device.createBond();
        device.setPairingConfirmation(true);
    }

    public final BroadcastReceiver bluetoothDeviceFoundReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            Log.d("bluetooth", action);
            if (action.equals(BluetoothDevice.ACTION_FOUND) && Utils.isConnected(mContext)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceMacAddress = device.getAddress().trim();
                    Log.d(TAG, "onReceive: " + deviceMacAddress);
                    if (!tempbluetoothDevices.contains(device)) {
                        User userAvailable = new User();
                        userAvailable.setMacAddress(deviceMacAddress);
                        userAvailable.setEmail(device.getName());
                        Call<User> name = apiCall.isAvailable(userAvailable);
                        name.enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                User user = response.body();

                                if (user != null) {
                                    Log.d(TAG, "onResponse: " + user.getName());
                                    tempbluetoothDevices.add(device);
                                    deviceAdapter.add(user.getName(), device);
                                }

                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {
                                Log.d(TAG, "onFailure: " + t.getMessage());
                                Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();

                            }
                        });
                    }

                }
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                Toast.makeText(mContext, Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothDeviceFoundReceiver);
    }


    public void deleteFiles(String str) {
        Log.d(TAG, "deleteFiles: " + str);
        Uri uri = Uri.parse(str);
        File file = new File(uri.toString());
        file.delete();
        if (file.exists()) {
            try {
                file.getCanonicalFile().delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (file.exists()) {
                getApplicationContext().deleteFile(file.getName());
            }
        }

    }

    public void alreadyBondedDevice() {
        Set<BluetoothDevice> listdevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (Utils.isConnected(mContext)) {
            for (final BluetoothDevice deviceSet : listdevice) {

                if (!tempbluetoothDevices.contains(deviceSet)) {
                    User userAvailable = new User();
                    userAvailable.setMacAddress(deviceSet.getAddress());
                    Call<User> name = apiCall.isAvailable(userAvailable);
                    name.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {

                            User user = response.body();

                            if (user != null) {
                                Log.d(TAG, "onResponse: " + user.getName());
                                tempbluetoothDevices.add(deviceSet);
                                deviceAdapter.add(user.getName(), deviceSet);
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


}
