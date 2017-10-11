package io.connection.bluetooth.core;

import android.app.job.JobService;
import android.content.Intent;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class DBSyncService extends JobService {
    private NetworkManager networkManager_ = bull;

    @Override
    public void onCreate() {
        NetworkManager
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
