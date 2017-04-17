package io.connection.bluetooth.Services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import io.connection.bluetooth.receiver.BluetoothDeviceReceiver;

/**
 * Created by KP49107 on 28-03-2017.
 */
public class MobiMixService extends Service {
    private BluetoothService bluetoothService;
    private WifiDirectService wifiDirectService;

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
        return null;
    }

    private void initBluetooth() {
        bluetoothService = BluetoothService.getInstance();
        bluetoothService.init();
    }

    private void initWifiDirect() {
        wifiDirectService = WifiDirectService.getInstance(this);
        wifiDirectService.registerReceiver();
        new Timer().schedule(new DiscoveryTask(), 500, 30000);
        wifiDirectService.initiateDiscovery();
    }

    @Override
    public void onDestroy() {
        bluetoothService.destroy();

        wifiDirectService.unRegisterReceiver();
        wifiDirectService.closeSocket();

        super.onDestroy();
    }

    private class DiscoveryTask extends TimerTask {
        @Override
        public void run() {
            wifiDirectService.initiateDiscovery();
        }
    }
}
