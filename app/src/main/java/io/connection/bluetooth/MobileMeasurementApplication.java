package io.connection.bluetooth.;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class MobileMeasurementApplication extends Application {
    private ApiCall service;
    private static MobileMeasurementApplication mApplication;
    private Activity mActivity;
    //    public static String BASE_URL = "http://192.168.43.28:8080/SpringRestfulWebServicesWithJSONExample/";
    public static String BASE_URL = "http://192.168.0.104:8080/SpringRestfulWebServicesWithJSONExample/";

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        init();
    }

    public static MobileMeasurementApplication getInstance() {
        return mApplication;
    }

    public void registerActivity(Activity ac) {
        mActivity = ac;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public ApiCall getService() {
        return service;
    }

    public Context getContext() {
        return getApplicationContext();
    }

    private void init() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).
                readTimeout(60, TimeUnit.SECONDS).
                connectTimeout(60, TimeUnit.SECONDS).build();

// Change base URL to your upload server URL.
        service = new Retrofit.Builder()
                .baseUrl(Constants.endPointAddress)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client).build().create(ApiCall.class);

    }
}
