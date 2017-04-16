package io.connection.bluetooth.actionlisteners;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Kinjal on 4/15/2017.
 */

public interface ResponseCallback<T> {
    public void onResponceSuccess(Call<T> call, Response<T> response);
    void onResponseFailure(Call<T> call);
}
