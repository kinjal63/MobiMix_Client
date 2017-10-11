package io.connection.bluetooth.Api;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Api.response.entity.NearByPlayer;
import io.connection.bluetooth.Api.response.entity.NearByPlayerResponse;
import io.connection.bluetooth.Database.entity.MBNearbyPlayers;
import io.connection.bluetooth.Domain.GameConnectionInfo;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.actionlisteners.IUpdateListener;
import io.connection.bluetooth.actionlisteners.ResponseCallback;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kinjal on 4/15/2017.
 */

public class WSManager {
    private ApiCall apiCall;
    private static WSManager instance;
    private Context mContext;
    private Handler handler_;

    public WSManager(Handler handler_) {
        this.handler_ = handler_;
        initialize();
    }

//    public static WSManager getInstance() {
//        if(instance == null) {
//            instance = new WSManager();
//        }
//        return instance;
//    }

    private void initialize() {
        apiCall = ApiClient.getClient().create(ApiCall.class);
        mContext = MobiMixApplication.getInstance().getContext();
    }

    public void checkIfUserAvailable(User user, final ResponseCallback responseCall) {
        Call<User> name = apiCall.isAvailable(user);
        name.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                User user = response.body();

                if (user != null) {
                    responseCall.onResponceSuccess(call, response);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                responseCall.onResponseFailure(call);
            }
        });
    }

    public void updateConnectionInfo(GameConnectionInfo connectionInfo, final IUpdateListener updateListener) {
        Call<ResponseBody> name = apiCall.updateConnectionInfo(connectionInfo);
        name.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String resData = response.body().string();
                    if(updateListener != null) {
                        updateListener.onUpdated();
                    }
                    System.out.println("Remote user is notified");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void getNearbyUsers() {
        NearByPlayerResponse response = new NearByPlayerResponse();

        List<NearByPlayer> players = new ArrayList<>();

        NearByPlayer player = new NearByPlayer();
        player.setPlayerId("123");
        player.setPlayerName("Kinjal");
        player.setIsEngaged(1);
        player.setActiveGameName("Xender");
        player.setPlayerImagePath("");

        NearByPlayer player2 = new NearByPlayer();
        player2.setPlayerId("12345");
        player2.setPlayerName("Shashank");
        player2.setIsEngaged(0);
        player2.setActiveGameName("Chess");
        player2.setPlayerImagePath("");


        players.add(player);
        players.add(player2);
        response.setPlayerlist(players);

        Message msg = new Message();
        msg.obj = response;
        handler_.sendMessage(msg);
    }
}
