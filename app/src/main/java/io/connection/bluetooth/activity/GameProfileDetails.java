package io.connection.bluetooth.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;

import java.util.Calendar;

import io.connection.bluetooth.Domain.GameLibrary;
import io.connection.bluetooth.Domain.GameProfile;
import io.connection.bluetooth.R;

/**
 * Created by songline on 09/10/16.
 */
public class GameProfileDetails extends AppCompatActivity {
    private static final String TAG = "GameProfileDetails";
    Context context;
    EditText playingTime;
    TextView gameName;
    TextView gamePublisher;
    TextView gameRating;
    TextView gameAgeRating;
    TextView gameGenre;
    TextView gameCategory;
    TextView gameVersion;
    TextView gameStudio;
    ImageView imageLogo;
    Button gameProfileGooglePlay;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.game_profile_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        GameProfile gameProfile = (GameProfile) getIntent().getSerializableExtra("gameProfile");
        imageLogo = (ImageView) findViewById(R.id.game_profile_details_image);
        gameName = (TextView) findViewById(R.id.game_profile_details_gameName);
        gamePublisher = (TextView) findViewById(R.id.game_profile_details_publisher);
        gameRating = (TextView) findViewById(R.id.game_profile_details_game_rating);
        gameAgeRating = (TextView) findViewById(R.id.game_profile_details_age_rating);
        gameGenre = (TextView) findViewById(R.id.game_profile_genre_value);
        gameCategory = (TextView) findViewById(R.id.game_profile_category_value);
        gameVersion = (TextView) findViewById(R.id.game_profile_version_value);
        gameStudio = (TextView) findViewById(R.id.game_profile_studio_value);
        playingTime = (EditText) findViewById(R.id.game_profile_play_timing);
        playingTime.setInputType(InputType.TYPE_NULL);
        gameProfileGooglePlay = (Button) findViewById(R.id.game_profile_google_play_button);

        final GameLibrary game = gameProfile.getGameLibrary();

        gameName.setText(game.getGameName());
        gamePublisher.setText(game.getGamePublisher());
        gameRating.setText(game.getGameRating().toString());
        gameAgeRating.setText(game.getAgeRating().toString() + "+");
        gameGenre.setText(game.getAgeRating() + "+");
        gameCategory.setText(game.getGameCategoryId().getCategoryName());
        gameVersion.setText(game.getGameVersion());
        gameStudio.setText(game.getGameCategoryId().getCategoryName());

        try {
            imageLogo.setImageDrawable(getPackageManager().getApplicationIcon(game.getPackageName()));

        } catch (Exception e) {
            e.printStackTrace();
        }


        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        final TimePickerDialog dialog;
        dialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {


                Toast.makeText(context, hourOfDay + " : " + minute + "  -   " + hourOfDayEnd + ":" + minuteEnd, Toast.LENGTH_LONG);

                playingTime.setText(hourOfDay + ":" + minute + " - " + hourOfDayEnd + ":" + minuteEnd);
            }
        }, hour, minute, true);

        dialog.setTitle("SELECT GAME PLAYING TIME");
        playingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show(getFragmentManager(), "TimeDialog");


            }
        });

        gameProfileGooglePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = game.getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
