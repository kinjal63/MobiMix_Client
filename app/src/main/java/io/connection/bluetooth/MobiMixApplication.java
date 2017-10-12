package io.connection.bluetooth;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import org.greenrobot.greendao.database.Database;

import java.util.concurrent.TimeUnit;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Database.entity.DaoMaster;
import io.connection.bluetooth.Database.entity.DaoSession;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Kinjal on 11/24/2016.
 */
public class MobiMixApplication extends Application {
    private ApiCall service;
    private static MobiMixApplication mApplication;
    private Activity mActivity;
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        init();
    }

    public static MobiMixApplication getInstance() {
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

        service = ApiClient.getClient().create(ApiCall.class);

        // Green DAO initialization
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "mobimix.db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
