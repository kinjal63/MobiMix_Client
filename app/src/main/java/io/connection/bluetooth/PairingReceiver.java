package io.connection.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Thread.ThreadConnection;
import io.connection.bluetooth.activity.ImageCache;

/**
 * Created by songline on 02/08/16.
 */
public class PairingReceiver extends BroadcastReceiver {
    private static final String TAG = "PairingReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + "   pairing request");

        if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (Build.VERSION.SDK_INT < 23) {
                device.setPin("1234".getBytes());
                device.setPairingConfirmation(true);
            }
        } else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {

            Log.d(TAG, "onReceive: checkc bond state " + intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, Integer.MIN_VALUE));
            if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, Integer.MIN_VALUE) == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "onReceive: in bonded state");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                List<Uri> uriList = ImageCache.getUriList(device.getAddress());
                if (uriList != null && uriList.size() > 0) {
                    Log.d(TAG, "onReceive: " + device.getAddress());
                    List<Uri> listSendFiles = new ArrayList<>();
                    // ConnectedThread thread = new ConnectedThread(device, listSendFiles);
                    //thread.start();
                    for (Uri uri : ImageCache.getUriList(device.getAddress())) {
                        listSendFiles.add(uri);
                    }
                    ThreadConnection connection = new ThreadConnection(context);
                    connection.connect(device, true, listSendFiles);
                    ImageCache.getUriList(device.getAddress()).clear();
                }


            }

        }


    }
}