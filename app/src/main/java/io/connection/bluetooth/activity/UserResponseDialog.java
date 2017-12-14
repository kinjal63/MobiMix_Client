package io.connection.bluetooth.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.connection.bluetooth.R;
import io.connection.bluetooth.Thread.GameEventConnectThread;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by songline on 10/12/16.
 */
public class UserResponseDialog extends AppCompatActivity {

    TextView textView;
    Button buttonYES;
    Button buttonNO;
    Context context;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_response_dialog);
        this.setFinishOnTouchOutside(false);
        context = this;

        TextView titleBar = (TextView) getWindow().findViewById(android.R.id.title);
        if (titleBar != null) {
            // set text color, YELLOW as sample
            titleBar.setTextColor(Color.YELLOW);
            // find parent view
            ViewParent parent = titleBar.getParent();
            if (parent != null && (parent instanceof View)) {
                // set background on parent, BRICK as sample
                View parentView = (View) parent;
                parentView.setBackgroundColor(Color.rgb(0x88, 0x33, 0x33));
            }
        }


        Intent intent = this.getIntent();
        String displayString = intent.getStringExtra("displayString");
        final BluetoothDevice device = intent.getParcelableExtra("device");
        final String packageName = intent.getStringExtra("packageName");


        textView = (TextView) findViewById(R.id.user_response_textID);
        textView.setText(displayString);




        buttonYES = (Button) findViewById(R.id.user_response_yes);
        buttonYES.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = ImageCache.getContext().getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
                String name = prefs.getString(Constants.NAME_KEY, "");
                String msg = "Response:1" + ":" + name + " $#$ " + packageName;
                GameEventConnectThread gameEventConnectThread;
//                gameEventConnectThread = new GameEventConnectThread(device, 0);
//                gameEventConnectThread.setResponse(msg);
//                gameEventConnectThread.start();
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName.trim());
                try {
                    context.startActivity(launchIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Game Not Found", Toast.LENGTH_LONG).show();
                }
                finish();


            }
        });


        buttonNO = (Button) findViewById(R.id.user_response_no);
        buttonNO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = ImageCache.getContext().getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
                String name = prefs.getString(Constants.NAME_KEY, "");
                String msg = "Response:0" + ":" + name;
                GameEventConnectThread gameEventConnectThread;
//                gameEventConnectThread = new GameEventConnectThread(device, 0);
//                gameEventConnectThread.setResponse(msg);
//                gameEventConnectThread.start();
                finish();

            }
        });

    }
}
