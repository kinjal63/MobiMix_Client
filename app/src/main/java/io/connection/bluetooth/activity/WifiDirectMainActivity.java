package io.connection.bluetooth.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Database.DBParams;
import io.connection.bluetooth.Database.entity.MBNearbyPlayer;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.actionlisteners.DeviceClickListener;
import io.connection.bluetooth.actionlisteners.DeviceConnectionListener;
import io.connection.bluetooth.actionlisteners.ISocketEventListener;
import io.connection.bluetooth.actionlisteners.NearByDeviceFound;
import io.connection.bluetooth.activity.gui.GUIManager;
import io.connection.bluetooth.adapter.WifiP2PDeviceAdapter;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.core.IWifiDisconnectionListener;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;

public class WifiDirectMainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        DeviceClickListener, IDBResponse {
    private static final String TAG = "MainActivity";
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    WifiP2PDeviceAdapter deviceAdapter;
    RecyclerView deviceLayout;
    ApiCall apiCall;
    private SearchView searchView;
    private ArrayList<MBNearbyPlayer> listUsers = new ArrayList<>();
    private static BottomSheetBehavior mBottomSheetBehavior;
    Context mContext;
    Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onStart();
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        MobiMixApplication.getInstance().registerActivity(this);

        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mContext = this;
        activity = this;
        ImageCache.setContext(mContext);

        deviceLayout = (RecyclerView) findViewById(R.id.footer);

        WifiDirectService.getInstance(this).initiateDiscovery();

        getNearByPlayers();
        deviceAdapter = new WifiP2PDeviceAdapter(this, listUsers);
        deviceAdapter.setDeviceClickListener(this);

        WifiDirectService.getInstance(this).setClassName(WifiDirectMainActivity.class.getSimpleName());
//        WifiDirectService.getInstance(this).setNearByDeviceFoundCallback(new NearByDeviceFound() {
//            @Override
//            public void onDevicesAvailable(Collection<WifiP2PRemoteDevice> devices) {
//                listWifiP2PDevices.clear();
//                listWifiP2PDevices.addAll(devices);
//
//                deviceAdapter.notifyDataSetChanged();
//            }
//        });

        setDeviceLayout(deviceLayout);

        final View footerView = findViewById(R.id.footer_device);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        apiCall = ApiClient.getClient().create(ApiCall.class);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Button buttonsend = (Button) findViewById(R.id.buttonsend);
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

    private void getNearByPlayers() {
        DBParams params = new DBParams();
        params.event_ = MobiMix.DBRequest.DB_FIND_NEARBY_PLAYERS;
        params.userId_ = ApplicationSharedPreferences.getInstance(this).getValue("user_id");
        GUIManager.getObject().getNearbyPlayers(params, this);
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
    }

    @Override
    public void onBluetoothDeviceClick(MBNearbyPlayer device) {

    }

    @Override
    public void onWifiDeviceClick(final MBNearbyPlayer remoteDevice) {
        final Dialog dialog = new Dialog(WifiDirectMainActivity.this);
        dialog.setContentView(R.layout.final_dialog_box);
        dialog.setTitle("Transfer File ... ");
//        final WifiP2pDevice device = remoteDevice.getDevice();

        TextView textViewName = (TextView) dialog.getWindow().findViewById(R.id.sendmessgae);
        textViewName.setText("Are You Sure Want to Send Below Files to  " + remoteDevice.getPlayerName() + " ?");

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
                            ImageCache.putUri(remoteDevice.getEmail(), Uri.parse(check));
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
                            ImageCache.putUri(remoteDevice.getEmail(), Uri.parse(check));
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
                            ImageCache.putUri(remoteDevice.getEmail(), Uri.parse(check));
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


                ImageCache.setContext(WifiDirectMainActivity.this);

                Log.d(TAG, "onClick:  size of file " + ImageCache.getUriList(remoteDevice.getEmail()));
                final List<Uri> listSendFiless = new ArrayList<Uri>();
                for (Uri uri : ImageCache.getUriList(remoteDevice.getEmail())) {
                    listSendFiless.add(uri);
                }

                SenderThread sender = new SenderThread(remoteDevice, listSendFiless);
                sender.connect();

                // ConnectedThread connectedThread = new ConnectedThread(device, listSendFiless);
                Log.d(TAG, "onDrag: Started Without sending Request");
                // connectedThread.start();
                ImageCache.getUriList(remoteDevice.getEmail()).clear();

                NotificationManagerCompat.from(WifiDirectMainActivity.this).cancelAll();
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

    private class SenderThread {
        private List<Uri> filesToSend;
        private MBNearbyPlayer device;

        SenderThread(MBNearbyPlayer device, List<Uri> filesToSend) {
            this.filesToSend = filesToSend;
            this.device = device;
        }

        public void connect() {
            UtilsHandler.showProgressDialog("Removing previous connection and reconnecting with " + device.getPlayerName());

            WifiDirectService.getInstance(WifiDirectMainActivity.this).removeConnectionAndReConnect(new IWifiDisconnectionListener() {
                @Override
                public void connectionRemoved(boolean isDisconnected) {
                    UtilsHandler.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UtilsHandler.dismissProgressDialog();
                        }
                    });
                    WifiDirectService.getInstance(WifiDirectMainActivity.this).connect(device.getEmail(), new DeviceConnectionListener() {
                        @Override
                        public void onDeviceConnected(boolean isConnected) {
                            if (isConnected) {
                                setSocketListeners(filesToSend);
                            } else {
                                UtilsHandler.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(WifiDirectMainActivity.this, "Could not connect with " + device.getPlayerName(), Toast.LENGTH_SHORT);
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }

    private void setSocketListeners(final List<Uri> listSendFiless) {
        GUIManager.getObject().setSocketEventListener(new ISocketEventListener() {
            @Override
            public void socketInitialized(String remoteSocketAddress) {
                WifiDirectService.getInstance(WifiDirectMainActivity.this).getMessageHandler().sendFiles(remoteSocketAddress, Modules.FILE_SHARING, listSendFiless);
            }

            @Override
            public void socketDiconnected() {
                if (!WifiDirectMainActivity.this.isFinishing() || !WifiDirectMainActivity.this.isDestroyed()) {
                    Toast.makeText(WifiDirectMainActivity.this, "Device is not connected, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void setDeviceLayout(RecyclerView deviceLayout) {
        deviceLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        deviceLayout.setAdapter(deviceAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void closeWifiP2PSocketsIfAny() {
//        WifiDirectService.getInstance(this).getMessageHandler().sendMessage(new String("NowClosing").getBytes());
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                WifiDirectService.getInstance(WifiDirectMainActivity.this).getMessageHandler().closeWifiSocket();
//            }
//        }, 1000);
    }

    private void removeWifiP2PConnection() {
        WifiDirectService.getInstance(this).removeGroup();
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
    public void onDataAvailable(int resCode, List<?> data) {
        if (resCode == MobiMix.DBResponse.DB_RES_FIND_NEARBY_PLAYERS) {
            List<MBNearbyPlayer> players = (List<MBNearbyPlayer>) data;

            this.listUsers.clear();
            this.listUsers.addAll(players);

            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDataFailure() {
        Utils.showErrorDialog(this, "Players could not be retrived, Please try after some time.");
    }
}
