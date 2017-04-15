package io.connection.bluetooth.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.Services.BluetoothService;
import io.connection.bluetooth.actionlisteners.NearByBluetoothDeviceFound;
import io.connection.bluetooth.actionlisteners.NearByDeviceFound;
import io.connection.bluetooth.activity.ChatDataConversation;
import io.connection.bluetooth.adapter.model.BluetoothRemoteDevice;
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
    private List<BluetoothRemoteDevice> bluetoothRemoteDevices = new ArrayList<BluetoothRemoteDevice>();
    private static BluetoothDeviceReceiver mBluetoothDeviceReceiver = null;

    private BluetoothService bluetoothService;

    public BluetoothDeviceReceiver() {
        bluetoothService = BluetoothService.getInstance();
    }

    public static BluetoothDeviceReceiver getInstance() {
        if( mBluetoothDeviceReceiver == null ) {
            mBluetoothDeviceReceiver = new BluetoothDeviceReceiver();
        }
        return mBluetoothDeviceReceiver;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: bluetooth receiver here");
        Log.d("bluetooth", action);
        if (action.equals(BluetoothDevice.ACTION_FOUND)) {
            final BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (remoteDevice != null) {
                Log.d(TAG, "onReceive: " + remoteDevice.getAddress().trim());

//                User userAvailable = new User();
//                userAvailable.setMacAddress(deviceMacAddress);
//                userAvailable.setEmail(deviceBroadcast.getName());
//                Call<User> name = apiCall.isAvailable(userAvailable);
//                name.enqueue(new Callback<User>() {
//                    @Override
//                    public void onResponse(Call<User> call, Response<User> response) {
//                        User user = response.body();

                        BluetoothRemoteDevice device = new BluetoothRemoteDevice(remoteDevice, remoteDevice.getName());
                        bluetoothService.setRemoteBluetoothDevice(device);

//                        if (user != null) {
//                            Log.d(TAG, "onResponse: " + user.getName());
//                        }
            }

//                    @Override
//                    public void onFailure(Call<User> call, Throwable t) {
//                        Log.d(TAG, "onFailure: " + t.getMessage());
//                        Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
//
//                    }
//                });

                if(remoteDevice.getName() != null) {
                    Log.d(TAG, "onReceive Remote device name: " + remoteDevice.getName().trim());
                    if (remoteDevice.getName().trim().equalsIgnoreCase(remoteUserName)) {
                        String deviceMacAddressToPair = remoteDevice.getAddress().trim();
                        Utils.pairWithBluetooth(deviceMacAddressToPair);
                    }
                }
            }
    }

    public void setUserId(String remoteUserName) {
        this.remoteUserName = remoteUserName;
    }

}
