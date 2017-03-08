package io.connection.bluetooth.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.GameProfile;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.R;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by songline on 28/09/16.
 */
public class GameProfileActivity extends AppCompatActivity {
    List<GameProfile> gameProfileList = null;
    Context context;
    RecyclerView gameProfileLayout;
    GameGridAdapter gameGridAdapter;
    GridLayoutManager gridLayoutManager;
    ApiCall apiCall;
    SharedPreferences preferences;
    EditText playingTime;
    private ProgressBar spinner;
    private Spinner multiSpinner;
    private static String gameTimePlay;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_profile_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        gameProfileList = new ArrayList<>();
        context = this;
        apiCall = ApiClient.getClient().create(ApiCall.class);
        PackageManager packageManager = this.getPackageManager();
        preferences = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
        final String userId = preferences.getString(Constants.LOGIN_KEY, "");
        playingTime = (EditText) findViewById(R.id.game_profile_play_timing);
        playingTime.setInputType(InputType.TYPE_NULL);
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        multiSpinner = (Spinner) findViewById(R.id.planets_spinner);
        List<String> categories = new ArrayList<String>();
        categories.add("Morning");
        categories.add("AfterNoon");
        categories.add("Evening");
        multiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item = parent.getItemAtPosition(position).toString();
                gameTimePlay = item;


                final String gameTime = preferences.getString("game_time_" + gameTimePlay.toLowerCase(), "");
                playingTime.setText(gameTime);
                // Showing selected spinner item
                Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        multiSpinner.setAdapter(dataAdapter);


        spinner.getIndeterminateDrawable().setColorFilter(Color.rgb(136, 136, 136), PorterDuff.Mode.MULTIPLY);

        preferences = getSharedPreferences(Constants.GAME, MODE_PRIVATE);
        final String gameTime = preferences.getString(Constants.GAME_TIME_MORNING, "");

        if (!gameTime.isEmpty()) {
            playingTime.setText(gameTime);
        }

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        final TimePickerDialog dialog;
        dialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {

                spinner.setVisibility(View.VISIBLE);
                Date date = new Date();
                long startTiming = TimeUnit.HOURS.toMillis(hourOfDay) + TimeUnit.MINUTES.toMillis(minute);
                date.setTime(startTiming);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date gmt = new Date(sdf.format(date));
                startTiming = gmt.getTime();

                long endTiming = TimeUnit.HOURS.toMillis(hourOfDayEnd) + TimeUnit.MINUTES.toMillis(minuteEnd);
                date.setTime(endTiming);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                gmt = new Date(sdf.format(date));
                endTiming = gmt.getTime();
                System.out.println(startTiming + "    " + endTiming);
                /*Toast.makeText(context, hourOfDay + " : " + minute + "  -   " + hourOfDayEnd + ":" + minuteEnd, Toast.LENGTH_LONG).show();*/
                User user = new User();
                user.setId(userId);
                user.setStartTime(startTiming);
                user.setEndTime(endTiming);
                user.setName(gameTimePlay);

                final int startHour = hourOfDay;
                final int startMinute = minute;
                final int endHour = hourOfDayEnd;
                final int endMinute = minuteEnd;

                if (Utils.isConnected(context)) {
                    Call<User> getUser = apiCall.updateGamePlayingTime(user);
                    getUser.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if (response.code() == 200) {
                                String gameTime = startHour + ":" + startMinute + " - " + endHour + ":" + endMinute;
                                playingTime.setText(gameTime);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("game_time_" + gameTimePlay.toLowerCase(), gameTime);
                                editor.apply();
                                editor.commit();
                                spinner.setVisibility(View.GONE);
                                Toast.makeText(context, "Game Playing Time Successfully Set ", Toast.LENGTH_LONG).show();

                            } else {
                                System.out.println(response.body());
                                spinner.setVisibility(View.GONE);
                                Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            t.printStackTrace();
                            spinner.setVisibility(View.GONE);
                            Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                }


            }
        }, hour, minute, true);

        dialog.setTitle("SELECT GAME PLAYING TIME");
        playingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show(getFragmentManager(), "TimeDialog");


            }
        });

        if (Utils.isConnected(context)) {
            Call<List<GameProfile>> resultList = apiCall.getGameProfileByUserId(userId);
            resultList.enqueue(new Callback<List<GameProfile>>() {
                @Override
                public void onResponse(Call<List<GameProfile>> call, Response<List<GameProfile>> response) {

                    if (response.code() == 200) {
                        gameProfileList = response.body();


                        List<String> tempPackageName = new ArrayList<String>();
                        PackageManager packageManager = context.getPackageManager();
                        List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
                        for (ApplicationInfo applicationInfo : applicationInfoList) {

                            if ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                                //it's a system app, not interested
                            } else if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                //Discard this one
                                //in this case, it should be a user-installed app
                            } else if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {

                                tempPackageName.add(applicationInfo.packageName);
                            }

                        }


                        Iterator<GameProfile> gameProfile = gameProfileList.iterator();

                        while (gameProfile.hasNext()) {

                            GameProfile gameProfile1 = gameProfile.next();
                            if (!tempPackageName.contains(gameProfile1.getGameLibrary().getPackageName())) {
                                gameProfile.remove();
                            }
                        }

                        gameProfileLayout = (RecyclerView) findViewById(R.id.game_profile_gridview);
                        gameGridAdapter = new GameGridAdapter(gameProfileList, context);
                        setgameProfileLayout(gameProfileLayout);

                    }
                }

                @Override
                public void onFailure(Call<List<GameProfile>> call, Throwable t) {
                    Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                    t.printStackTrace();
                }
            });

        } else {
            Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_profile_reference, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_game_profile) {
            Intent intent = new Intent(context, GameProfileAddActivity.class);
            startActivity(intent);
        } else if (id == android.R.id.home) {
           /* Intent intent1 = new Intent(this, io.connection.bluetooth.activity.Home_Master.class);
            startActivity(intent1);*/
            onBackPressed();
        }


        return true;

    }

    public void setgameProfileLayout(RecyclerView gameProfileLayout) {
        gameProfileLayout.setHasFixedSize(true);
        gridLayoutManager = (GridLayoutManager) gameProfileLayout.getLayoutManager();
        //  gameProfileLayout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        gameProfileLayout.setLayoutManager(gridLayoutManager);
        gameProfileLayout.setAdapter(gameGridAdapter);

    }

    public class GameGridAdapter extends RecyclerView.Adapter<GameGridAdapter.ViewHolder> implements View.OnClickListener {

        List<GameProfile> gameProfileList = null;
        Context context = null;
        PackageManager packageManager;

        public GameGridAdapter(List<GameProfile> gameProfiles, Context context) {

            gameProfileList = gameProfiles;
            this.context = context;
            packageManager = context.getPackageManager();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                GameProfile gameProfile = gameProfileList.get(position);
                Drawable icon;
                try {
                    icon = packageManager.getApplicationIcon(gameProfile.getGameLibrary().getPackageName().toString());
                } catch (Exception e) {
                    icon = getResources().getDrawable(R.drawable.ic_user);
                }
                holder.app_logo.setImageDrawable(icon);
                holder.app_name.setText(gameProfile.getGameLibrary().getGameName());

               /* if (!gameProfile.getGameLibrary().getApproved()) {
                    holder.playing_time.setText("Under Review");
                    holder.playing_time.setTextColor(Color.BLUE);
                } else if (gameProfile.getGameLibrary().getApproved() && gameProfile.getStartTime() == null && gameProfile.getEndTime() == null) {
                    String mystring = "Set Playing Time";
                    *//*SpannableString content = new SpannableString(mystring);
                    content.setSpan(new UnderlineSpan(), 0, mystring.length(), 0);*//*
                    holder.playing_time.setText(mystring);
                    holder.playing_time.setTextColor(Color.RED);
                    // holder.playing_time.setTypeface(null, Typeface.BOLD);

                } else if (gameProfile.getGameLibrary().getApproved() && gameProfile.getAvailable()) {
                    holder.playing_time.setText(gameProfile.getStartTime() + " -  " + gameProfile.getEndTime());
                    holder.playing_time.setTextColor(Color.GREEN);
                } else if (!gameProfile.getAvailable()) {
                    holder.playing_time.setText("Profile Diasabled");
                    holder.playing_time.setTextColor(Color.BLACK);
                }
                if (!holder.playing_time.getText().toString().contains("Under Review"))
                    holder.itemView.setOnClickListener(this);
                else
                    holder.itemView.setEnabled(false);*/

                holder.itemView.setTag(gameProfile);

            }

        }

        @Override
        public void onClick(View v) {
            GameProfile gameProfile = (GameProfile) v.getTag();
            Intent intent = new Intent(context, GameProfileDetails.class);
            intent.putExtra("gameProfile", gameProfile);
            startActivity(intent);


        }

        @Override
        public int getItemCount() {
            return gameProfileList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(viewType == 0 ? R.layout.game_profile_single_card : R.layout.searching_devices, parent, false), context, viewType);
        }


        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView app_logo;
            TextView app_name;
            TextView playing_time;


            public ViewHolder(View itemView, Context context, int type) {
                super(itemView);
                if (type == 0) {
                    app_logo = (ImageView) itemView.findViewById(R.id.gameImage);
                    app_name = (TextView) itemView.findViewById(R.id.gameTitle);
                    playing_time = (TextView) itemView.findViewById(R.id.gameTime);
                    itemView.setOnClickListener(this);
                }
            }

            @Override
            public void onClick(View v) {

            }
        }


    }


}
