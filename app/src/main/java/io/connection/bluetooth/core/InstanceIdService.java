package io.connection.bluetooth.core;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.TimeZone;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.DeviceDetails;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by songline on 02/10/16.
 */
public class InstanceIdService extends FirebaseInstanceIdService {


    private static final String TAG = "InstanceIdService";
    SharedPreferences prefs;

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh: " + token);
        prefs = getSharedPreferences(Constants.TOKEN, MODE_PRIVATE);
        sendTokenToServer(token);

    }


    private void sendTokenToServer(final String token) {

        final boolean result = isTokenRegister();
        if (result) {

            DeviceDetails deviceDetails = new DeviceDetails();
            deviceDetails.setDeviceId(Utils.getDeviceId(getApplicationContext()));
            PackageManager packageManager = getApplicationContext().getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(getApplicationContext().getPackageName(), 0);
                deviceDetails.setAppVersion(packageInfo.versionName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            deviceDetails.setManufacturer(Build.MANUFACTURER);
            deviceDetails.setModel(Build.MODEL);
            deviceDetails.setModelName(Build.DEVICE);
            deviceDetails.setOsName("Android");
            deviceDetails.setOsVersion(Build.VERSION.RELEASE);
            deviceDetails.setPushToken(token);
            // deviceDetails.setTimeZone(Utils.toISO8601Date(new Date()));
            deviceDetails.setTimeZone(TimeZone.getDefault().getID());


            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    TelephonyManager phoneDetails = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
                    if (phoneDetails != null) {
                        deviceDetails.setImei(phoneDetails.getDeviceId());
                    }
                } catch (Exception var5) {
                    ;
                }

            }
            if (Utils.isConnected(getBaseContext())) {

                ApiCall apiCall = ApiClient.getClient().create(ApiCall.class);
                Call<Object> resultPush = apiCall.sendPushTokenToServer(deviceDetails);
                resultPush.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if (response.code() >= 200 && response.code() <= 300) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(Constants.TOKEN, token);
                            editor.putInt(Constants.APP_VERSION, Utils.getAppVersion(getApplicationContext()));
                            editor.commit();
                        } else {
                            Log.d(TAG, "onResponse: " + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            } else {
                Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        }

        Log.d(TAG, "Token: " + token);

    }


    private boolean isTokenRegister() {

        String registrationId = prefs.getString(Constants.TOKEN_KEY, "");
        int version_code = prefs.getInt(Constants.APP_VERSION, Integer.MIN_VALUE);
        if (registrationId.isEmpty()) {
            Log.d(TAG, "isTokenRegister: Registration not found ");
            return true;

        } else if (Utils.getAppVersion(getApplicationContext()) != version_code) {
            return true;
        }
        return false;

    }


}
