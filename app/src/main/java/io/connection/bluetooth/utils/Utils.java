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

import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.actionlisteners.BluetoothPairCallback;
import io.connection.bluetooth.actionlisteners.DialogActionListener;

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

    public static void setBluetoothAdapterName() {
        boolean isNameSet = mBluetoothAdapter.setName(ApplicationSharedPreferences.getInstance(
                MobiMixApplication.getInstance().getContext()).getValue("email"));
        Log.d("BluetoothAdapter:", "Is Name Set::" + isNameSet + ", BluetoothAdapter Name:" + Utils.getBluetoothAdapter().getName());
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

    public static void pairWithBluetooth(String bluetoothAddress, BluetoothPairCallback bluetoothPairCallback) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);

        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");

            bluetoothPairCallback.devicePaired(true);
        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
            bluetoothPairCallback.devicePaired(false);
        }
    }

    public static void showAlertMessage(Context context, String title, String message, final DialogActionListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.dialogPositiveButtonPerformed();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.dialogNegativeButtonPerformed();
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void showErrorDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle("Error");

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public static void makeDeviceDiscoverable(Context context) {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
            context.startActivity(discoverableIntent);
        }
//            try {
//                IBlueTooth mBtService = getIBluetooth();
//                Log.d("TESTE", "Ensuring bluetoot is discoverable");
//                if(mBtService.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//                    Log.e("TESTE", "Device was not in discoverable mode");
//                    try {
//                        mBtService.setDiscoverableTimeout(100);
//                        // mBtService.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 1000);
//                    } catch(Exception e) {
//                        Log.e("TESTE", "Error setting bt discoverable",e);
//                    }
//                    Log.i("TESTE", "Device must be discoverable");
//                } else {
//                    Log.e("TESTE", "Device already discoverable");
//                }
//            } catch(Exception e) {
//                Log.e("TESTE", "Error ensuring BT discoverability", e);
//            }
    }
}
