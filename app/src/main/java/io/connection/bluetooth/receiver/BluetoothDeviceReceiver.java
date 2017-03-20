package io.connection.bluetooth.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.activity.ChatDataConversation;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kinjal on 3/20/2017.
 */

public class BluetoothDeviceReceiver extends BroadcastReceiver {
    private String TAG = "BluetoothDeviceReceiver";
    private String remoteUserName = "";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: bluetooth receiver here");
        Log.d("bluetooth", action);
        if (action.equals(BluetoothDevice.ACTION_FOUND)) {
            final BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (remoteDevice != null) {
                Log.d(TAG, "onReceive: " + remoteDevice.getAddress().trim());
                if(remoteDevice.getName() != null) {
                    Log.d(TAG, "onReceive Remote device name: " + remoteDevice.getName().trim());
                    if (remoteDevice.getName().trim().equalsIgnoreCase(remoteUserName)) {
                        String deviceMacAddressToPair = remoteDevice.getAddress().trim();
                        Utils.pairWithBluetooth(deviceMacAddressToPair);
                    }
                }
            }
        }
    }

    public void setUserId(String remoteUserName) {
        this.remoteUserName = remoteUserName;
    }
}
