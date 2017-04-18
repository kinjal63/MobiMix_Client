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
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MobiMixService getService() {
            return MobiMixService.this;
        }
    }

    public void init() {
        initBluetooth();
        initWifiDirect();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void initBluetooth() {
        bluetoothService = BluetoothService.getInstance();
        bluetoothService.init();
    }

    private void initWifiDirect() {
        wifiDirectService = WifiDirectService.getInstance(this);
        wifiDirectService.registerReceiver();
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("MobiMix task removed-stopping service");
        stopSelf();
    }

    public void destroy() {
        bluetoothService.destroy();

        wifiDirectService.unRegisterReceiver();
        wifiDirectService.closeSocket();
    }
}
