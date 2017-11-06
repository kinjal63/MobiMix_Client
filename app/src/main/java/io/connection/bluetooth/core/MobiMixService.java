package io.connection.bluetooth.core;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import io.connection.bluetooth.MobiMixApplication;

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
        initJobScheular();
//        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
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

    // Database sync initilization
    private void initJobScheular() {
        ComponentName componentName = new ComponentName(MobiMixApplication.getInstance().getContext(), DBSyncService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, componentName);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setMinimumLatency(1000);

        JobScheduler jobSchedular = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobSchedular.schedule(builder.build());
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
        bluetoothService.unregisterReceiver();
        wifiDirectService.unRegisterReceiver();
        System.out.println("Destroying service3");
        wifiDirectService.closeSocket();

        System.out.println("Destroying service4");
    }
}
