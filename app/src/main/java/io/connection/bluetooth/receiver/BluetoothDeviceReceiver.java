package io.connection.bluetooth.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.connection.bluetooth.Api.WSManager;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.Services.BluetoothService;
import io.connection.bluetooth.actionlisteners.NearByBluetoothDeviceFound;
import io.connection.bluetooth.actionlisteners.NearByDeviceFound;
import io.connection.bluetooth.actionlisteners.ResponseCallback;
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
    private Context mContext;

    private BluetoothService bluetoothService;

    public BluetoothDeviceReceiver() {
        bluetoothService = BluetoothService.getInstance();
        mContext = MobileMeasurementApplication.getInstance().getContext();
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

                User userAvailable = new User();
                userAvailable.setMacAddress(remoteDevice.getAddress());
                userAvailable.setEmail(remoteDevice.getName());

                WSManager.getInstance().checkIfUserAvailable(userAvailable, new ResponseCallback() {
                    @Override
                    public void onResponceSuccess(Call call, Response response) {
                        BluetoothRemoteDevice device = new BluetoothRemoteDevice(remoteDevice, remoteDevice.getName());
                        bluetoothService.setRemoteBluetoothDevice(device);
                    }

                    @Override
                    public void onResponseFailure(Call call) {

                    }
                });

                if (remoteDevice.getName() != null) {
                    Log.d(TAG, "onReceive Remote device name: " + remoteDevice.getName().trim());
                    if (remoteDevice.getName().trim().equalsIgnoreCase(remoteUserName)) {
                        String deviceMacAddressToPair = remoteDevice.getAddress().trim();
                        Utils.pairWithBluetooth(deviceMacAddressToPair);
                    }
                }
            }
        }
    }

    public void findAlreadyBondedDevice() {
        Set<BluetoothDevice> listdevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (Utils.isConnected(mContext)) {
            for (final BluetoothDevice deviceSet : listdevice) {
                User userAvailable = new User();
                userAvailable.setMacAddress(deviceSet.getAddress());
                userAvailable.setEmail(deviceSet.getName());

                WSManager.getInstance().checkIfUserAvailable(userAvailable, new ResponseCallback<User>() {
                    @Override
                    public void onResponceSuccess(Call<User> call, Response<User> response) {
                        User user = response.body();
                        BluetoothRemoteDevice device = new BluetoothRemoteDevice(deviceSet, user.getName());
                        bluetoothService.setRemoteBluetoothDevice(device);
                    }

                    @Override
                    public void onResponseFailure(Call call) {
                        Toast.makeText(mContext, "No users are found.", Toast.LENGTH_SHORT);
                    }
                });

            }
        } else {
            Toast.makeText(mContext, Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }
    }

    public void setUserId(String remoteUserName) {
        this.remoteUserName = remoteUserName;
    }

}
