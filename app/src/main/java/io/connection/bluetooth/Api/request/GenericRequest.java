package io.connection.bluetooth.Api.request;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import retrofit2.http.Body;

/**
 * Created by KP49107 on 11-10-2017.
 */
public abstract class GenericRequest<T> {
    public void buildRequest(@Body T t, String apiMethod) {

    }
}
