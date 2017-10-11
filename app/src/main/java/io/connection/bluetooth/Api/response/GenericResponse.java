package io.connection.bluetooth.Api.response;

import okhttp3.ResponseBody;

/**
 * Created by KP49107 on 11-10-2017.
 */
public abstract class GenericResponse<T> extends ResponseBody {
    public abstract void parseResponse();
}
