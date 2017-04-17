package io.connection.bluetooth.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.connection.bluetooth.R;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;

/**
 * Created by songline on 20/09/16.
 */
public class Login_Register extends AppCompatActivity {
    final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        printWifiAddress();

        setContentView(R.layout.login_register_layout);
        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, 0);

        } else {
            //  AcceptThread accept = new AcceptThread(bluetoothAdapter,getBaseContext());
            //accept.start();
        }
        sharedPref = this.getSharedPreferences("myPref", Context.MODE_PRIVATE);

        ApplicationSharedPreferences.getInstance(this).addValue("email", Build.MODEL);

//        if(sharedPref.getBoolean("is_login", false)) {
            startActivity(new Intent(this, Home_Master.class));
            finish();
//        }
        viewPager = (ViewPager) findViewById(R.id.pager_login_register);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs_login_register);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void printWifiAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
//                if (macBytes == null) {
//                    return;
//                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                System.out.println("Wifi MAC Addess->" + res1.toString());
            }
        } catch (Exception ex) {
            //handle exception
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.finish();

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new LoginActivity(), "SIGN IN");
        adapter.addFragment(new SignupActivity(), "REGISTER");
        viewPager.setAdapter(adapter);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //  AcceptThread accept = new AcceptThread(bluetoothAdapter, getBaseContext());
                //accept.start();
            } else {
                Toast.makeText(getApplicationContext(), "Please Turn On Bluetooth ", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
