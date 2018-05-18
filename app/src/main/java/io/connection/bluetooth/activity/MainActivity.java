package io.connection.bluetooth.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Thread.ConnectedBusinessThread;
import io.connection.bluetooth.Thread.ConnectedThread;
import io.connection.bluetooth.Thread.ThreadConnection;
import io.connection.bluetooth.actionlisteners.DeviceClickListener;
import io.connection.bluetooth.actionlisteners.NearByBluetoothDeviceFound;
import io.connection.bluetooth.adapter.BluetoothDeviceAdapter;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.core.BluetoothService;
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.utils.UtilsHandler;

public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener, DeviceClickListener {
    private static final String TAG = "MainActivity";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    BluetoothDeviceAdapter deviceAdapter;
    RecyclerView deviceLayout;
    private SearchView searchView;
    private ArrayList<MBNearbyPlayer> listBluetoothDevices = new ArrayList<>();
    private static BottomSheetBehavior mBottomSheetBehavior;
    Context mContext;
    Activity activity;
    private NetworkType networkType;

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
        initBluetooth();
        setDeviceLayout(deviceLayout);

        final View footerView = findViewById(R.id.footer_device);


       /* Set<BluetoothDevice> listdevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();

        for (BluetoothDevice deviceSet : listdevice) {
            bluetoothDevices.add(deviceSet);
            deviceAdapter.add(deviceSet.getName());
        }*/

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Button buttonsend = (Button) findViewById(R.id.buttonsend);
        Button sendFilesButton = (Button)findViewById(R.id.sendFileButton);

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFiles();
            }
        });
        buttonsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior = BottomSheetBehavior.from(footerView);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                View view = findViewById(R.id.footerSend);
                mBottomSheetBehavior = BottomSheetBehavior.from(view);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

    private void initBluetooth() {
        networkType = NetworkType.BLUETOOTH;

        bluetoothService.setClassName(MainActivity.class.getSimpleName());

        deviceAdapter = new BluetoothDeviceAdapter(this, listBluetoothDevices);
        deviceAdapter.setDeviceClickListener(this);

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
//                        deviceAdapter.notifyDataSetChanged();
//                    }
//                });
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1111:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bluetoothService.startDiscovery();
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

    public void sendFiles(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.final_dialog_box);
        dialog.setTitle("Transfer File ... ");

        TextView textViewName = (TextView) dialog.getWindow().findViewById(R.id.sendmessgae);
        textViewName.setText("Are You Sure Want to Send Below Files?");

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

                for(MBNearbyPlayer remoteDevice : deviceAdapter.getSelectedDevices()) {
                    BluetoothDevice device = bluetoothService.getBluetoothDevice(remoteDevice.getEmail()).getDevice();
//                    MBNearbyPlayer device = remoteDevice.getEmail();
                    if (imageCheckbox.isChecked()) {
                        for (String check : ImageCache.getImageCheckBox().keySet()) {
                            if (ImageCache.getImageCheckBox(check)) {
                                ImageCache.putUri(device.getAddress(), Uri.parse(check));
//                                ImageCache.setImageCheckBoxValue(check, false);
                            }
                        }
                    } else {
                        for (String check : ImageCache.getImageCheckBox().keySet()) {
//                            ImageCache.setImageCheckBoxValue(check, false);
                        }
                    }
//                    ImagesFragment.updateChekBox();

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

                    ImageCache.setContext(MainActivity.this);
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "onClick:  size of file " + ImageCache.getUriList(device.getAddress()));
                        List<Uri> listSendFiless = new ArrayList<Uri>();
                        for (Uri uri : ImageCache.getUriList(device.getAddress())) {
                            listSendFiless.add(uri);
                        }
                        ThreadConnection connection = new ThreadConnection(MainActivity.this);
                        connection.connect(device, true, listSendFiless);
                        // ConnectedThread connectedThread = new ConnectedThread(device, listSendFiless);
                        Log.d(TAG, "onDrag: Started Without sending Request");
                        // connectedThread.start();
                        ImageCache.getUriList(device.getAddress()).clear();
                    } else {
                        System.out.println(" here pairing");
                        createPairing(device);

                    }
                }

                ImageCache.clearImageCheckBox();
                ImagesFragment.updateChekBox();

                NotificationManagerCompat.from(MainActivity.this).cancelAll();
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

    public void setDeviceLayout(RecyclerView deviceLayout) {
        deviceLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        deviceLayout.setAdapter(deviceAdapter);
    }

    public static void createPairing(BluetoothDevice device) {
        device.createBond();
        device.setPairingConfirmation(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    @Override
    public void onBluetoothDeviceClick(MBNearbyPlayer remoteDevice) {
        List<BluetoothRemoteDevice> availableBluetoothDevices = BluetoothService.getInstance().getBluetoothDevices();
        for(BluetoothRemoteDevice device : availableBluetoothDevices){
            if(device.getName().equalsIgnoreCase(remoteDevice.getEmail())) {
                ConnectedThread connectedThread = new ConnectedThread(device.getDevice());
                connectedThread.start();
            }
        }
        NotificationManagerCompat.from(this).cancelAll();
    }

    @Override
    public void onWifiDeviceClick(MBNearbyPlayer device) {

    }
}
