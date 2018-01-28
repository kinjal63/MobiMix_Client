package io.connection.bluetooth.core;

/**
 * Created by songline on 04/12/16.
 */

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Api.async.IResponseHandler;
import io.connection.bluetooth.Domain.DataUsageModel;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.R;
import io.connection.bluetooth.activity.UserNearByWithGames;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPSTracker extends JobService implements LocationListener {
    SharedPreferences preferences;
    ApiCall apiCall;

    private Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;
    private JobParameters params;

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (isNetworkEnabled) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }

                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
/*
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }*/

    /**
     * Function to get latitude
     */

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

  /*  */

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     *//*

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
*/
    @Override
    public void onLocationChanged(Location location) {

        // Code Here for Update Location;
        this.location = location;

        if(ApplicationSharedPreferences.getInstance(this).getBooleanValue(Constants.PREF_IS_DATA_USAGE_TRACKING_ON)) {

        }

        preferences = this.getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
        String userId = ApplicationSharedPreferences.getInstance(this).getValue("user_id");

        apiCall = ApiClient.getClient().create(ApiCall.class);
        if (Utils.isConnected(this)) {
            if (!userId.equals("")) {
                User user = new User();
                user.setId(userId);
                if (location != null) {
                    user.setLatitude(location.getLatitude());
                    user.setLongitude(location.getLongitude());
                    Call<HashSet> userCall = apiCall.updateUserLocation(user);
                    userCall.enqueue(new Callback<HashSet>() {
                        @Override
                        public void onResponse(Call<HashSet> call, Response<HashSet> response) {
                            if (response.code() == 200 && response.body() != null && sendNotification(preferences)) {


                            }
                        }

                        @Override
                        public void onFailure(Call<HashSet> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(mContext, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                        }
                    });


                    // Pass User Call to set value of location in database.
                }
            }
        } else {
            Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        this.params = jobParameters;
        sendDataUsageInfo();
        return true;
    }

    private void sendDataUsageInfo() {
        TelephonyManager telephonyManager =((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE));
        String operatorName = telephonyManager.getNetworkOperatorName();

        NetworkManager networkManager_ = NetworkManager.getInstance();
        if(networkManager_.isNetworkConnected()) {
            DataUsageModel dataUsageInfo = new DataUsageModel();

            dataUsageInfo.setUserId(ApplicationSharedPreferences.getInstance(this).getValue("user_id"));
            dataUsageInfo.setNetworkOperatorId(operatorName);
            dataUsageInfo.setLatitude(location.getLatitude());
            dataUsageInfo.setLongitude(location.getLongitude());
            dataUsageInfo.setTimeStamp(System.currentTimeMillis());

            networkManager_.sendDataUsageToServer(dataUsageInfo, new IResponseHandler() {
                @Override
                public void onResponse() {
                    jobFinished(params, true);
                }
            });
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        preferences = this.getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
        String userId = ApplicationSharedPreferences.getInstance(this).getValue("user_id");

        apiCall = ApiClient.getClient().create(ApiCall.class);
        if (Utils.isConnected(this)) {
            if (!userId.equals("")) {
                Location location = getLocation();
                User user = new User();
                user.setId(userId);
                if (location != null) {
                    user.setLatitude(location.getLatitude());
                    user.setLongitude(location.getLongitude());

                    Call<HashSet> userCall = apiCall.updateUserLocation(user);
                    userCall.enqueue(new Callback<HashSet>() {
                        @Override
                        public void onResponse(Call<HashSet> call, Response<HashSet> response) {
                            if (response.code() == 200 && response.body() != null && sendNotification(preferences)) {

                                System.out.println(" --> " + Arrays.toString(response.body().toArray()));

                                HashSet hashSet = response.body();

                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext).setAutoCancel(true);
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
                                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(mBuilder);
                                Intent resultIntent = new Intent(mContext, UserNearByWithGames.class);


                                if (hashSet.size() > 0) {
                                    for (Object game : hashSet)
                                        style.addLine(game.toString());
                                    style.addLine("");
                                    style.setBigContentTitle("See Below Common Games");
                                    // mBuilder.setContentText("We Found That " + response.body() + " User With Similar Game Interest. Click Me for Find Them");
                                    mBuilder.setContentTitle("User Found With Common Game Interest ...");
                                    stackBuilder.addParentStack(UserNearByWithGames.class);
                                    // Adds the Intent that starts the Activity to the top of the stack
                                    stackBuilder.addNextIntent(resultIntent);
                                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                    mBuilder.setContentIntent(resultPendingIntent);
                                    Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_logo);
                                    mBuilder.setLargeIcon(largeIcon);
                                    mBuilder.setColor(getResources().getColor(R.color.black));
                                    mBuilder.setSmallIcon(R.drawable.ic_game_profile);
                                    mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                                    mBuilder.setLights(Color.BLUE, 3000, 3000);
                                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    mBuilder.setSound(uri);
                                    mBuilder.setStyle(style);
                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    // notificationID allows you to update the notification later on.
                                    mNotificationManager.cancel(hashSet.size());
                                    mNotificationManager.notify(hashSet.size(), mBuilder.build());

                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<HashSet> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(mContext, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                        }
                    });

                }

            }
        } else {
            Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }


    }

    public boolean sendNotification(SharedPreferences preferences) {
        long checkTime = preferences.getLong("time_notification", 0);
        if (System.currentTimeMillis() > checkTime) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("time_notification", System.currentTimeMillis() + 600000);
            editor.apply();
            editor.commit();
            return true;
        }
        return false;
    }
}


