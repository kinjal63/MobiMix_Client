package io.connection.bluetooth.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.connection.bluetooth.MobiMixApplication;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPSTrackerUtil extends Service implements LocationListener {

    private final Context mContext;
    private MyPhoneStateListener1 mPhoneStatelistener1;
    private MyPhoneStateListener2 mPhoneStatelistener2;
    private TelephonyManager mTelephonyManager1, mTelephonyManager2;

    // Flag for GPS status
    boolean isGPSEnabled = false;
    int mSignalStrength = 0, mSignalStrength2 = 0;

    // Flag for network status
    boolean isNetworkEnabled = false;

    // Flag for GPS status
    boolean canGetLocation = false;

    Location location; // Location
    double latitude; // Latitude
    double longitude; // Longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;
    private Handler mHandler;

    private long mMobileRx = 0;
    private long mMobileTx = 0;

    private long mWifiRx = 0, mPreviousWifiRx = 0;
    private long mWifiTx = 0, mPreviousWifiTx = 0;

    private String TAG = "CallInfo";

    private String carrierName1, carrierName2;
    private int subId1 = 0, subId2 = 0;
    public static GPSTrackerUtil gpsTrackerInstance = null;

    public GPSTrackerUtil(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
//        getRssi();
//        getDataUsage();
    }

    private void getRssi() {
        mPhoneStatelistener1 = new MyPhoneStateListener1();
        mPhoneStatelistener2 = new MyPhoneStateListener2();

        mTelephonyManager1 = (TelephonyManager) this.mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager1.listen(mPhoneStatelistener1, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void getDataUsage() {

        mMobileRx = TrafficStats.getMobileRxBytes()/1000000 - mMobileRx;
        mMobileTx = TrafficStats.getMobileTxBytes()/1000000 - mMobileTx;

        mWifiRx = TrafficStats.getTotalRxBytes()/1000000 - mMobileRx - mWifiRx;
        mWifiTx = TrafficStats.getTotalTxBytes()/1000000 - mMobileTx - mWifiTx;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendRssiAndDataUsage();
            }
        }, 1000, 10000);
    }

    private void getSubscriptionIdandCarriers() {
        final SubscriptionManager subscriptionManager = SubscriptionManager.from(mContext);
        final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

        if( activeSubscriptionInfoList.size() == 2 ) {
            carrierName1 = activeSubscriptionInfoList.get(0).getCarrierName().toString();
            subId1 = activeSubscriptionInfoList.get(0).getSubscriptionId();

            carrierName2 = activeSubscriptionInfoList.get(1).getCarrierName().toString();
            subId2 = activeSubscriptionInfoList.get(1).getSubscriptionId();
        }
    }

    private void sendRssiAndDataUsage() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try
                {
                    URL url = new URL(Constants.endPointAddress + "recordRSSI");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Method", "POST");
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userId", ApplicationSharedPreferences.getInstance(mContext).getValue("user_id"));
                    jsonObject.put("deviceId", "hgd6356d53t7gcvhenc");
                    jsonObject.put("rssi", mSignalStrength);
                    jsonObject.put("latitude", latitude);
                    jsonObject.put("longitude", longitude);
                    jsonObject.put("operatorName", "IND airtel");

                    OutputStream os = conn.getOutputStream();
                    DataOutputStream wr = new DataOutputStream (
                            conn.getOutputStream ());
                    wr.writeBytes (jsonObject.toString());
                    wr.flush();
                    wr.close();
                    os.close();

                    conn.connect();

                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                    }

                    URL url1 = new URL(Constants.endPointAddress + "saveDataUsage");
                    HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
                    conn1.setReadTimeout(10000);
                    conn1.setConnectTimeout(15000);
                    conn1.setRequestProperty("Content-Type", "application/json");
                    conn1.setRequestProperty("Method", "POST");
                    conn1.setRequestMethod("POST");
                    conn1.setDoInput(true);
                    conn1.setDoOutput(true);

                    mMobileRx = TrafficStats.getMobileRxBytes()/1000 - mMobileRx;
                    mMobileTx = TrafficStats.getMobileTxBytes()/1000 - mMobileTx;


                    mWifiRx = TrafficStats.getTotalRxBytes()/1000 - mMobileRx - mPreviousWifiRx;
                    mWifiTx = TrafficStats.getTotalTxBytes()/1000 - mMobileTx - mPreviousWifiTx;

                    mPreviousWifiRx += mWifiRx;
                    mPreviousWifiTx += mWifiTx;

                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("userId", ApplicationSharedPreferences.getInstance(mContext).getValue("user_id"));
                    jsonObject1.put("deviceId", "hgd6356d53t7gcvhenc");
                    jsonObject1.put("country", Utils.country);
                    jsonObject1.put("latitude", latitude);
                    jsonObject1.put("longitude", longitude);
                    jsonObject1.put("mobileTx", mMobileTx);
                    jsonObject1.put("mobileRx", mMobileRx);
                    jsonObject1.put("wifiTx", mWifiTx);
                    jsonObject1.put("wifiRx", mWifiRx);
                    jsonObject1.put("operatorName", "IND airtel");

                    OutputStream os1 = conn1.getOutputStream();
                    DataOutputStream wr1 = new DataOutputStream (
                            conn1.getOutputStream ());
                    wr1.writeBytes (jsonObject1.toString());
                    wr1.flush();
                    wr1.close();
                    os1.close();

                    conn1.connect();

                    int responseCode1 = conn1.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class MyPhoneStateListener1 extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength.getGsmSignalStrength();
            mSignalStrength = (2 * mSignalStrength) - 113; // -> dBm

            System.out.println("Signal Strengh" + mSignalStrength);
            Message msg = new Message();
            msg.what = 1;
            msg.obj = mSignalStrength;
            mHandler.sendMessage(msg);
        }
    }

    class MyPhoneStateListener2 extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength2 = signalStrength.getGsmSignalStrength();
            mSignalStrength2 = (2 * mSignalStrength2) - 113; // -> dBm

            System.out.println("Signal Strengh" + mSignalStrength2);
        }
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // Getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
            } else {
                this.canGetLocation = true;

                // If GPS enabled, get latitude/longitude using GPS Services
                if (isGPSEnabled && locationManager != null) {
                    if (location == null) {
                        if (ActivityCompat.checkSelfPermission(this.mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");

                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            updateLocation();
                        }
                    }
                }

                // If Network provider is enabled, get latitude/longitude using GPS Services
                else if (isNetworkEnabled && locationManager != null) {
                    if (location == null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            updateLocation();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app.
     */

    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(GPSTrackerUtil.this);
        }
    }


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
     * Function to check GPS/Wi-Fi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    /**
     * Function to show settings alert dialog.
     * On pressing the Settings button it will launch Settings Options.
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onLocationChanged(Location location) {
        updateLocation();
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
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void updateLocation() {
        String userId = ApplicationSharedPreferences.getInstance(mContext).getValue("user_id");
        retrofit2.Call<okhttp3.ResponseBody> req1 = MobiMixApplication.getInstance().
                                                    getService().updateUserLocation(userId, latitude, longitude);

        req1.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String data = response.body().string();
                    System.out.println(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}