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
import io.connection.bluetooth.actionlisteners.BluetoothPairCallback;
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
                userAvailable.setName(remoteDevice.getName());
                userAvailable.setMacAddress(remoteDevice.getAddress());
                userAvailable.setEmail(remoteDevice.getName());

                WSManager.getInstance().checkIfUserAvailable(userAvailable, new ResponseCallback<User>() {
                    @Override
                    public void onResponceSuccess(Call<User> call, Response<User> response) {
                        User user = response.body();
                        BluetoothRemoteDevice device = new BluetoothRemoteDevice(remoteDevice, user.getName());
                        bluetoothService.setRemoteBluetoothDevice(device);
                    }

                    @Override
                    public void onResponseFailure(Call call) {

                    }
                });
            }
        }
    }

    public void findAlreadyBondedDevice() {
        Set<BluetoothDevice> listdevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
//        if (Utils.isConnected(mContext)) {
            for (final BluetoothDevice device : listdevice) {
                User userAvailable = new User();
                userAvailable.setName(device.getName());
                userAvailable.setMacAddress(device.getAddress());
                userAvailable.setEmail(device.getName());

                WSManager.getInstance().checkIfUserAvailable(userAvailable, new ResponseCallback<User>() {
                    @Override
                    public void onResponceSuccess(Call<User> call, Response<User> response) {
                        User user = response.body();
                        BluetoothRemoteDevice remoteDevice = new BluetoothRemoteDevice(device, user.getName());
                        bluetoothService.setRemoteBluetoothDevice(remoteDevice);
                    }

                    @Override
                    public void onResponseFailure(Call call) {
                        Toast.makeText(mContext, "No users are found.", Toast.LENGTH_SHORT);
                    }
                });

            }
//        } else {
//            Toast.makeText(mContext, Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
//        }
    }

    public void pairWithDevice(String remoteUserName, BluetoothPairCallback bluetoothPairResult) {
        List<BluetoothRemoteDevice> bluetoothDevices = bluetoothService.getBluetoothDevices();
        for(BluetoothRemoteDevice device : bluetoothDevices) {
            if( device.getDevice().getName().equalsIgnoreCase(remoteUserName) ) {
                Utils.pairWithBluetooth(device.getDevice().getAddress(), bluetoothPairResult);
            }
        }
    }

}
