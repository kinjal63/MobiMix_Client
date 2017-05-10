package io.connection.bluetooth.Api;

import android.content.Context;

import java.io.IOException;

import io.connection.bluetooth.Domain.GameRequestConnection;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.actionlisteners.ResponseCallback;
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

    public void notifyConnectionEstablished(long gameId, String remoteUserId, int connectionType) {
        String userId = ApplicationSharedPreferences.getInstance(mContext).getValue("user_id");

        GameRequestConnection requestConnectionObj = new GameRequestConnection();
        requestConnectionObj.setGameId(gameId);
        requestConnectionObj.setRemoteUserId(remoteUserId);
        requestConnectionObj.setUserId(userId);
        requestConnectionObj.setConnectionType(connectionType);

        Call<ResponseBody> name = apiCall.updateConnectionInfo(requestConnectionObj);
        name.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String resData = response.body().string();
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
