package io.connection.bluetooth.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Services.GPSTracker;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.GPSTrackerUtil;
import io.connection.bluetooth.utils.Utils;

/**
 * Created by Kinjal on 11/14/2016.
 */
public class MobileDataUsageActivity extends Activity {
    private Button indiaButton, usButton, ukButton, franceButton, collectButton, timeButton, getAppsButtons, findPlayersButton;
    public static String country = "IN";
    private Handler mHandler;
    private GPSTrackerUtil gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_data);

        indiaButton = (Button) this.findViewById(R.id.india_usage_button);
        usButton = (Button) this.findViewById(R.id.us_usage_button);
        ukButton = (Button) this.findViewById(R.id.uk_usage_button);
        franceButton = (Button) this.findViewById(R.id.France_usage_button);
        collectButton = (Button) this.findViewById(R.id.collect_usage_button);

        getAppsButtons = (Button) this.findViewById(R.id.apps_button);

        indiaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.country = "IN";
            }
        });

        usButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.country = "US";
            }
        });

        ukButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.country = "UK";
            }
        });

        franceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.country = "FR";
            }
        });

        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aggregateCalllogDuration();
            }
        });

        getAppsButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MobileDataUsageActivity.this, CallLogActivity.class);
                intent.putExtra("simno", 1);
                startActivity(intent);
            }
        });
    }

    private void aggregateCalllogDuration() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    URL url = new URL(Constants.endPointAddress + "aggregateCallDuration");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Method", "POST");
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sim1_call_duration", ApplicationSharedPreferences.getInstance(MobileDataUsageActivity.this).getLongValue("callLog1Duration"));
                    jsonObject.put("sim2_call_duration", 0);
                    jsonObject.put("userId", ApplicationSharedPreferences.getInstance(MobileDataUsageActivity.this).getValue("user_id"));

                    OutputStream os = conn.getOutputStream();
                    DataOutputStream wr = new DataOutputStream(
                            conn.getOutputStream());
                    wr.writeBytes(jsonObject.toString());
                    wr.flush();
                    wr.close();
                    os.close();

                    conn.connect();

                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {

                        Snackbar.make(MobileDataUsageActivity.this.findViewById(android.R.id.content),
                                "Call logs are recorded.",
                                Snackbar.LENGTH_LONG)
                                .show();
//                        callLog1Duration = 0;
//                        callLog2Duration = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
