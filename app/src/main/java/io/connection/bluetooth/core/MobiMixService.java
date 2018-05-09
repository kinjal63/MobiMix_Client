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

import java.util.Timer;
import java.util.TimerTask;

import io.connection.bluetooth.Api.async.IResponseHandler;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.MobileDataUsageActivity;

/**
 * Created by KP49107 on 28-03-2017.
 */
public class MobiMixService extends Service {
    private Context context;

    private BluetoothService bluetoothService;
    private WifiDirectService wifiDirectService;
    private MessageHandler handler;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MobiMixService getService() {
            return MobiMixService.this;
        }
    }

    public void init() {
        initRadioService();
    }

    private void initRadioService() {
        bluetoothService = BluetoothService.getInstance();
        bluetoothService.init();

        wifiDirectService = WifiDirectService.getInstance(this);
        wifiDirectService.initialize();

        handler = new MessageHandler(context, wifiDirectService);
        wifiDirectService.setMessageHandler(handler);
        bluetoothService.setHandler(handler);
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

    // Database sync initilization
    private void initJobScheular() {
        ComponentName component1 = new ComponentName(MobiMixApplication.getInstance().getContext(), DBSyncService.class);
        JobInfo.Builder builder1 = new JobInfo.Builder(0, component1);
        builder1.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder1.setPeriodic(10000);
        JobScheduler schedular1 = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        schedular1.schedule(builder1.build());

        ComponentName component2 = new ComponentName(MobiMixApplication.getInstance().getContext(), GPSTracker.class);
        JobInfo.Builder builder2 = new JobInfo.Builder(0, component2);
        builder2.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder2.setPeriodic(30000);
        JobScheduler schedular2 = (JobScheduler)this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        schedular2.schedule(builder2.build());

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                NetworkManager networkManager_ = NetworkManager.getInstance();
                if(networkManager_.isNetworkConnected()) {
                    networkManager_.sendRequestTofetchNearbyPlayers(new IResponseHandler() {
                        @Override
                        public void onResponse() {

                        }
                    });
                }
            }
        }, 1000, 30000);
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
