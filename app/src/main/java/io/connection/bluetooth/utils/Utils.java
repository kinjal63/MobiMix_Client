package io.connection.bluetooth.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.adapter.GameAdapter;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Created by songline on 02/10/16.
 */
public class Utils {

    @SuppressLint("SimpleDateFormat") private static final DateFormat ISO_8601_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
    private static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static String country = "IN";

    public static BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public static int getAppVersion(Context context) {
        int versionCode = 0;
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return versionCode;
    }


    public static String getDeviceId(Context context){

        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId;
    }

    public static String toISO8601Date(Date date) {
        return ISO_8601_DATE_FORMAT.format(date);
    }


    public  static List<String> getAllInstalledGames(Context context){
        List<String> installedApps = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : applicationInfoList) {

            if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                //it's a system app, not interested
            } else if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //Discard this one
                //in this case, it should be a user-installed app
            } else if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                installedApps.add(applicationInfo.packageName);

            }

        }

        return installedApps;

    }


    public static boolean hasPermission(Context context, String permission) {
        return context.checkCallingOrSelfPermission(permission) == PERMISSION_GRANTED;
    }

    public static boolean isConnected(Context context) {
        if (!hasPermission(context, ACCESS_NETWORK_STATE)) {
            return true; // assume we have the connection and try to upload
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void pairWithBluetooth(String bluetoothAddress) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
        pairDevice(device);
    }

    private static void pairDevice(BluetoothDevice device) {
        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");

            MobileMeasurementApplication.getInstance().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertbox = new AlertDialog.Builder(MobileMeasurementApplication.getInstance().getContext())
                            //.setIcon(R.drawable.no)
                            .setTitle("Open Game")
                            .setMessage("Bluetooth connection is established. Do you want to open the game " + GameAdapter.gameName)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                                // do something when the button is clicked
                                public void onClick(DialogInterface arg0, int arg1) {

                                    Intent LaunchIntent = MobileMeasurementApplication.getInstance().getContext().getPackageManager().
                                            getLaunchIntentForPackage(GameAdapter.gamePackageName);
                                    MobileMeasurementApplication.getInstance().getContext().startActivity(LaunchIntent);
                                }
                            })
                            .show();

                }
            });
        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
        }
    }

    public static void makeDeviceDiscoverable() {
            try {
                IBluetooth mBtService = getIBluetooth();
                Log.d("TESTE", "Ensuring bluetoot is discoverable");
                if(mBtService.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Log.e("TESTE", "Device was not in discoverable mode");
                    try {
                        mBtService.setDiscoverableTimeout(100);
                        // mBtService.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 1000);
                    } catch(Exception e) {
                        Log.e("TESTE", "Error setting bt discoverable",e);
                    }
                    Log.i("TESTE", "Device must be discoverable");
                } else {
                    Log.e("TESTE", "Device already discoverable");
                }
            } catch(Exception e) {
                Log.e("TESTE", "Error ensuring BT discoverability", e);
            }
        }

    }
}
