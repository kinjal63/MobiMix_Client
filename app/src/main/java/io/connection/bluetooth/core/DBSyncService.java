package io.connection.bluetooth.core;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class DBSyncService extends JobService {
    private NetworkManager networkManager_ = null;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        syncDB();
        return true;
    }

    private void syncDB() {
        networkManager_.sendRequestTofetchNearbyPlayers();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        networkManager_ = NetworkManager.getInstance();
        return START_STICKY;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
