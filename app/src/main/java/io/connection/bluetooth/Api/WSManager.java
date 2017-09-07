package io.connection.bluetooth.Api;

import android.content.Context;

import java.io.IOException;

import io.connection.bluetooth.Domain.GameConnectionInfo;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.MobileMeasurementApplication;
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

    public WSManager() {
        initialize();
    }

    public static WSManager getInstance() {
        if(instance == null) {
            instance = new WSManager();
        }
        return instance;
    }

    private void initialize() {
        apiCall = ApiClient.getClient().create(ApiCall.class);
        mContext = MobileMeasurementApplication.getInstance().getContext();
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
}
