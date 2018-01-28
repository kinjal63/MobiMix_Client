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
import io.connection.bluetooth.Api.async.IResponseHandler;
import io.connection.bluetooth.Api.response.entity.NearByPlayer;
import io.connection.bluetooth.Api.response.entity.NearByPlayerResponse;
import io.connection.bluetooth.Api.response.entity.PlayerGame;
import io.connection.bluetooth.Domain.DataUsageModel;
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

    public void getNearbyUsers(final IResponseHandler responseHandler) {
        String userId = ApplicationSharedPreferences.getInstance(MobiMixApplication.getInstance().getContext()).
                getValue("user_id");

        Call<ResponseBody> name = apiCall.findPlayers(userId);
        name.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                try {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    NearByPlayerResponse response = gson.fromJson(res.body().string(), NearByPlayerResponse.class);
                    System.out.println("nearby players are found");

                    Message msg = new Message();
                    msg.obj = response;
                    msg.what = MobiMix.APIResponse.RESPONSE_GET_NEARBY_PLAYERS;
                    networkManager_.handleResponse(msg);

                    if (responseHandler != null) {
                        responseHandler.onResponse();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void sendDataUsage(final DataUsageModel dataUsageModel, final IResponseHandler responseHandler) {
        String userId = ApplicationSharedPreferences.getInstance(MobiMixApplication.getInstance().getContext()).
                getValue("user_id");

        Call<ResponseBody> name = apiCall.senDataUsage(dataUsageModel);
        name.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                if (res.code() == 200) {
                    System.out.println("Data usage information is sent.");
                }
                if (responseHandler != null) {
                    responseHandler.onResponse();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
