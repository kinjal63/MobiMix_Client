package io.connection.bluetooth.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;

import io.connection.bluetooth.Api.WSManager;
import io.connection.bluetooth.Api.async.IAPIResponse;
import io.connection.bluetooth.Api.async.IResponseHandler;
import io.connection.bluetooth.Api.response.entity.NearByPlayer;
import io.connection.bluetooth.Api.response.entity.NearByPlayerResponse;
import io.connection.bluetooth.Database.DBParams;
import io.connection.bluetooth.Database.DatabaseManager;
import io.connection.bluetooth.Database.action.IActionUpdateListener;
import io.connection.bluetooth.Database.action.IDatabaseActionListener;
import io.connection.bluetooth.Domain.DataUsageModel;
import io.connection.bluetooth.Domain.GameConnectionInfo;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.actionlisteners.IUpdateListener;
import io.connection.bluetooth.actionlisteners.ResponseCallback;
import io.connection.bluetooth.activity.IDBResponse;
import io.connection.bluetooth.utils.Constants;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by KP49107 on 11-10-2017.
 */
public class NetworkManager {
    private static NetworkManager networkManager_ = null;
    private WSManager wsManager_ = null;
    private ExecutorService executorService = null;

    NetworkManager() {
        wsManager_ = WSManager.getInstance(this);
    }

    public static NetworkManager getInstance() {
        if (networkManager_ == null) {
            networkManager_ = new NetworkManager();
        }
        return networkManager_;
    }

    public void sendRequestTofetchNearbyPlayers(IResponseHandler handler) {
        wsManager_.getNearbyUsers(handler);
    }

    public void sendDataUsageToServer(DataUsageModel dataUsageModel, IResponseHandler handler) {
        wsManager_.sendDataUsage(dataUsageModel, handler);
    }

    public void updateConnectionInfo(GameConnectionInfo connectionInfo, IUpdateListener listener) {
        wsManager_.updateConnectionInfo(connectionInfo, listener);
    }

    public void checkIfUserAvailable(User user, IAPIResponse<User> listener) {
        wsManager_.checkIfUserAvailable(user, listener);
    }

    public void setAvailabilityForWifiDirectDevices() {
        DatabaseManager.getInstance().setAvailabilityForWifiDirectDevices();
    }

    public void setAvailabilityForBluetoothDevice(String deviceName) {
        DatabaseManager.getInstance().setAvailabilityForBluetoothDevice(deviceName);
    }

    // GUI to update after fetching data from DB
    public void getNearByPlayersFromDB(DBParams params, IDatabaseActionListener dbActionListener) {
        DatabaseManager.getInstance().findPlayers(params, dbActionListener);
    }

    public void getPlayerInfo(DBParams params, IDatabaseActionListener dbActionListener) {
        DatabaseManager.getInstance().getPlayerInfo(params, dbActionListener);
    }

    public void getMutualGames(DBParams params, IDatabaseActionListener dbActionListener) {
        DatabaseManager.getInstance().findMutualGames(params, dbActionListener);
    }

    public void updateGameTable(DBParams params) {
        DatabaseManager.getInstance().updateGameTable(params);
    }

    public void deleteGameParticipants(DBParams params) {
        DatabaseManager.getInstance().deleteGameParticipants(params);
    }

    public void updateGameTableInBatch(DBParams params, IDatabaseActionListener dbActionListener) {
        DatabaseManager.getInstance().updateGameTableInBatch(params, dbActionListener);
    }

    public void getGameTableData(DBParams params, IDatabaseActionListener dbActionListner) {
        DatabaseManager.getInstance().getGameTableData(params, dbActionListner);
    }

    // Check internet connectivity
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                MobiMixApplication.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public void handleResponse(Message msg) {
        switch (msg.what) {
            case MobiMix.APIResponse.RESPONSE_GET_NEARBY_PLAYERS:
                if (msg.obj != null && msg.obj instanceof NearByPlayerResponse) {
                    List<NearByPlayer> players = ((NearByPlayerResponse) msg.obj).getPlayers();
                    DatabaseManager.getInstance().insertPlayers(players);
                }
                break;
            default:
                break;
        }
    }
}
