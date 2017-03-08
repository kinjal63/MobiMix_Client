package io.connection.bluetooth.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.connection.bluetooth.R;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by songline on 30/11/16.
 */
public class UserProfile extends AppCompatActivity{
    SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        preferences = this.getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();

        ((TextView)findViewById(R.id.input_name_profile)).setText(preferences.getString(Constants.NAME_KEY, ""));
        ((TextView)findViewById(R.id.input_email_profile)).setText(preferences.getString(Constants.EMAIL_KEY, ""));
        java.text.DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        try {

            Date date = new Date(preferences.getLong(Constants.DOB_KEY, Long.MAX_VALUE));
            String dateText = df.format(date);
            ((TextView)findViewById(R.id.input_dob_profile)).setText(dateText);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }
}
