package io.connection.bluetooth.Api;

import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.actionlisteners.ResponseCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kinjal on 4/15/2017.
 */

public class WSManager {
    private ApiCall apiCall;
    private static WSManager instance;

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
}
