package io.connection.bluetooth.core;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.connection.bluetooth.Api.WSManager;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class NetworkManager {
    private WSManager wsManager_ = null;
    private ExecutorService executorService = null;

    NetworkManager() {
        wsManager_ = WSManager.getInstance();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MobiMix.DatabaseSyncOperation.GET_NEARBY_PLAYERS:
                    executorService.submit()
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
