package io.connection.bluetooth.Api;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.connection.bluetooth.Api.async.IAPIResponse;
import io.connection.bluetooth.Api.response.entity.NearByPlayer;
import io.connection.bluetooth.Api.response.entity.NearByPlayerResponse;
import io.connection.bluetooth.Api.response.entity.PlayerGame;
import io.connection.bluetooth.Domain.GameConnectionInfo;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.actionlisteners.IUpdateListener;
import io.connection.bluetooth.actionlisteners.ResponseCallback;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.NetworkManager;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
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
    private NetworkManager networkManager_;

    public WSManager(NetworkManager networkManager_) {
        this.networkManager_ = networkManager_;
        initialize();
    }

    public static WSManager getInstance(NetworkManager networkManager_) {
        if (instance == null) {
            instance = new WSManager(networkManager_);
        }
        return instance;
    }

    private void initialize() {
        apiCall = ApiClient.getClient().create(ApiCall.class);
        mContext = MobiMixApplication.getInstance().getContext();
    }

    public void checkIfUserAvailable(User user, final IAPIResponse responseCall) {
        Call<User> name = apiCall.isAvailable(user);
        name.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                if (user != null) {
                    responseCall.onResponseSuccess(user);
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
                    if (updateListener != null) {
                        updateListener.onUpdated();
                    }
                    System.out.println("Remote user is notified");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void getNearbyUsers() {
//        String userId = ApplicationSharedPreferences.getInstance(MobiMixApplication.getInstance().getContext()).
//                getValue("user_id");
//
//        Call<ResponseBody> name = apiCall.findPlayers(userId);
//        name.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
//                try {
//                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                    NearByPlayerResponse response = gson.fromJson(res.body().string(), NearByPlayerResponse.class);
//                    System.out.println("nearby players are found");
//
//                    Message msg = new Message();
//                    msg.obj = response;
//                    msg.what = MobiMix.APIResponse.RESPONSE_GET_NEARBY_PLAYERS;
//                    networkManager_.handleResponse(msg);
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//            }
//        });

        NearByPlayerResponse response = new NearByPlayerResponse();

        List<NearByPlayer> players = new ArrayList<>();

        NearByPlayer myPlayer = new NearByPlayer();
        myPlayer.setPlayerId("c034a6735cdd4d54ba7c7d99ddd5393b");
        myPlayer.setPlayerName("Redmi");
        myPlayer.setIsEngaged(1);
        myPlayer.setGroupOwnerUserId("456");
        myPlayer.setIsGroupOwner(1);
        myPlayer.setActiveGameName("Xender");
        myPlayer.setPlayerImagePath("");
        myPlayer.setMaxPlayers(3);

        NearByPlayer player = new NearByPlayer();
        player.setPlayerId("123");
        player.setPlayerName("Kinjal");
        player.setIsEngaged(1);
        player.setGroupOwnerUserId("456");
        player.setIsGroupOwner(0);
        player.setActiveGameName("Xender");
        player.setPlayerImagePath("");

        NearByPlayer player2 = new NearByPlayer();
        player2.setPlayerId("12345");
        player2.setPlayerName("Shashank");
        player2.setIsEngaged(0);
        player2.setGroupOwnerUserId("456");
        player2.setIsGroupOwner(1);
        player2.setActiveGameName("Chess");
        player2.setPlayerImagePath("");

        PlayerGame playerGame = new PlayerGame();
        playerGame.setGamePackageName("com.xender");
        playerGame.setGameImagePath("");
        playerGame.setGameName("Xender");
        playerGame.setGameId((long) 1);

        PlayerGame playerGame2 = new PlayerGame();
        playerGame2.setGamePackageName("com.chess");
        playerGame2.setGameImagePath("");
        playerGame2.setGameName("Chess");
        playerGame2.setGameId((long) 2);

        PlayerGame playerGame3 = new PlayerGame();
        playerGame3.setGamePackageName("com.cricket");
        playerGame3.setGameImagePath("");
        playerGame3.setGameName("Cricket");
        playerGame3.setGameId((long) 3);

        // Set Player1 games
        List<PlayerGame> playergames = new ArrayList<>();
        playergames.add(playerGame);
        playergames.add(playerGame2);
        player.setPlayerGameList(playergames);
        myPlayer.setPlayerGameList(playergames);

        // Set Player2 games
        List<PlayerGame> playergames2 = new ArrayList<>();
        playergames2.add(playerGame);
        playergames2.add(playerGame3);
        player2.setPlayerGameList(playergames2);

        players.add(myPlayer);
        players.add(player);
        players.add(player2);
        response.setPlayerlist(players);

        Message msg = new Message();
        msg.obj = response;
        msg.what = MobiMix.APIResponse.RESPONSE_GET_NEARBY_PLAYERS;
        networkManager_.handleResponse(msg);
    }
}
