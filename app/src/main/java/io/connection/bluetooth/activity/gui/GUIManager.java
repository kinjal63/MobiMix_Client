package io.connection.bluetooth.activity.gui;

import android.content.Context;

import java.util.List;

import io.connection.bluetooth.Database.DBParams;
import io.connection.bluetooth.Database.action.IDatabaseActionListener;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.activity.IDBResponse;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.NetworkManager;

/**
 * Created by Kinjal on 10/13/2017.
 */

public class GUIManager {
    private NetworkManager  networkManager_;
    private Context context;
    private static GUIManager guiManager_ = null;

    GUIManager() {
        networkManager_ = NetworkManager.getInstance();
    }

    public static GUIManager getObject() {
        if(guiManager_ == null) {
            guiManager_ = new GUIManager();
        }
        return guiManager_;
    }

    public void getNearbyPlayers(DBParams params, final IDBResponse IDBResponseListener) {
        networkManager_.getNearByPlayersFromDB(params, new IDatabaseActionListener() {
            @Override
            public void onDataReceived(final List<?> data) {
                MobiMixApplication.getInstance().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IDBResponseListener.onDataAvailable(MobiMix.DBResponse.DB_RES_FIND_NEARBY_PLAYERS, data);
                    }
                });
            }

            @Override
            public void onDataError() {
                IDBResponseListener.onDataFailure();
            }
        });
    }

    public void getMutualGames(DBParams params, final IDBResponse IDBResponseListener) {
        networkManager_.getMutualGames(params, new IDatabaseActionListener() {
            @Override
            public void onDataReceived(final List<?> data) {
                MobiMixApplication.getInstance().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        IDBResponseListener.onDataAvailable(MobiMix.DBResponse.DB_RES_FIND_MUTUAL_GAMES, data);
                    }
                });
            }

            @Override
            public void onDataError() {
                IDBResponseListener.onDataFailure();
            }
        });
    }
}
