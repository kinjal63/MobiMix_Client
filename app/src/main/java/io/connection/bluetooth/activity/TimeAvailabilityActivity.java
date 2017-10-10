package io.connection.bluetooth.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.io.IOException;
import java.util.Calendar;

import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Kinjal on 12/25/2016.
 */

public class TimeAvailabilityActivity extends Activity {
    private TimePicker timePicker1;
    private Button fromTime, toTime;
    private Button btnSubmit;
    private Calendar calendar;
    private String format = "";
    private boolean isFromTime, isToTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_availability);

        timePicker1 = (TimePicker) findViewById(R.id.timePicker1);
        fromTime = (Button) findViewById(R.id.textView1);
        toTime = (Button) findViewById(R.id.textView5);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        fromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFromTime = true;
                isToTime = false;
            }
        });

        toTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFromTime = false;
                isToTime = true;
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTime();
            }
        });
        calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        showTime(hour, min);
    }

    public void setTime(View view) {
        int hour = timePicker1.getCurrentHour();
        int min = timePicker1.getCurrentMinute();
        showTime(hour, min);
    }

    public void showTime(int hour, int min) {
//        if (hour == 0) {
//            hour += 12;
//            format = "AM";
//        } else if (hour == 12) {
//            format = "PM";
//        } else if (hour > 12) {
//            hour -= 12;
//            format = "PM";
//        } else {
//            format = "AM";
//        }

        if (isFromTime) {
            fromTime.setText(new StringBuilder().append(hour).append(":").append(min));
        } else {
            toTime.setText(new StringBuilder().append(hour).append(":").append(min));
        }
    }

    private void addTime() {
        Call<ResponseBody> call = MobiMixApplication.getInstance().
                getService().addUserTime(ApplicationSharedPreferences.getInstance(TimeAvailabilityActivity.this).getValue("user_id"),
                fromTime.getText().toString(), toTime.getText().toString());
        call.enqueue(new Callback<ResponseBody>() {
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
