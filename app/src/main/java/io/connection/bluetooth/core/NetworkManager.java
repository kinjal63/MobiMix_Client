package io.connection.bluetooth.core;

import android.os.Handler;
import android.os.Message;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.connection.bluetooth.Api.WSManager;
import io.connection.bluetooth.Api.response.GenericResponse;
import io.connection.bluetooth.Api.response.entity.NearByPlayer;
import io.connection.bluetooth.Api.response.entity.NearByPlayerResponse;
import io.connection.bluetooth.Database.DatabaseManager;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class NetworkManager {
    private WSManager wsManager_ = null;
    private ExecutorService executorService = null;

    NetworkManager() {
        wsManager_ = WSManager.getInstance(handler);
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MobiMix.DatabaseSyncOperation.RESPONSE_GET_NEARBY_PLAYERS:
                    if(msg.obj != null && msg.obj instanceof NearByPlayerResponse) {
                        List<NearByPlayer> players = ((NearByPlayerResponse)msg.obj).getPlayerlist();
                        DatabaseManager.getInstance().insert(players);
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
