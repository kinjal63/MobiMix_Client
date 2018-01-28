package io.connection.bluetooth.core;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import io.connection.bluetooth.Api.async.IResponseHandler;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class DBSyncService extends JobService {
    JobParameters params = null;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        syncDB();
        return true;
    }

    private void syncDB() {
        NetworkManager networkManager_ = NetworkManager.getInstance();
        if(networkManager_.isNetworkConnected()) {
            networkManager_.sendRequestTofetchNearbyPlayers(new IResponseHandler() {
                @Override
                public void onResponse() {
                    jobFinished(params, true);
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
