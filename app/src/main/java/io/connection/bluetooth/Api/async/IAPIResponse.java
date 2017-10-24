package io.connection.bluetooth.Api.async;

import retrofit2.Call;

/**
 * Created by Kinjal on 10/16/2017.
 */

public interface IAPIResponse<T> {
    void onResponseSuccess(T body);
    void onResponseFailure(Call<T> call);
}
