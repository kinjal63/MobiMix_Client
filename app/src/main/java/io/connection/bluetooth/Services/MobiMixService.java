package io.connection.bluetooth.Services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import io.connection.bluetooth.receiver.BluetoothDeviceReceiver;

/**
 * Created by KP49107 on 28-03-2017.
 */
public class MobiMixService extends Service {
    private LocalBinder mBinder = new LocalBinder();
    private BluetoothDeviceReceiver mBluetoothDeviceFoundReceiver;
    private WifiDirectService wifiDirectService;

    public class LocalBinder extends Binder {
        public MobiMixService getService() {
            return MobiMixService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initBluetooth();
        initWifiDirect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void initBluetooth() {
        mBluetoothDeviceFoundReceiver = BluetoothDeviceReceiver.getInstance();
        registerReceiver(mBluetoothDeviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    private void initWifiDirect() {
        wifiDirectService = WifiDirectService.getInstance(this);
        wifiDirectService.registerReceiver();
        wifiDirectService.initiateDiscovery();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBluetoothDeviceFoundReceiver);
        wifiDirectService.unRegisterReceiver();
        super.onDestroy();
    }
}
