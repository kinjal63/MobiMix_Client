package io.connection.bluetooth.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import io.connection.bluetooth.Api.ApiCall;
import io.connection.bluetooth.Api.ApiClient;
import io.connection.bluetooth.Domain.User;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Services.GPSTracker;
import io.connection.bluetooth.Thread.AcceptBusinessThread;
import io.connection.bluetooth.Thread.AcceptThread;
import io.connection.bluetooth.Thread.GameRequestAcceptThread;
import io.connection.bluetooth.Thread.ThreadConnection;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by songline on 16/10/16.
 */
public class Home_Master extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Home_Master";
    BluetoothAdapter bluetoothAdapter;
    static boolean checkThread = false;
    private Context context;
    ApiCall apiCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_layout);
        getSupportActionBar().setIcon(R.mipmap.ic_logo);
        findViewById(R.id.file_card_id).setOnClickListener(this);
        findViewById(R.id.game_card_id).setOnClickListener(this);
        findViewById(R.id.business_card_id).setOnClickListener(this);
        findViewById(R.id.chat_card_id).setOnClickListener(this);
        findViewById(R.id.nearby_card_id).setOnClickListener(this);
        ImageCache.setContext(this);
        Intent intent = new Intent(this, GPSTracker.class);
        this.startService(intent);
        apiCall = ApiClient.getClient().create(ApiCall.class);
        context = this;

        if (!new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").exists()) {
            new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").mkdir();
            new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/BusinessCard").mkdir();
            new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/MediaFiles").mkdir();
        }

        SharedPreferences preferences = this.getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
//        if (preferences.getString(Constants.LOGIN_KEY, "").equals("")) {
//            Intent intent1 = new Intent(this, Login_Register.class);
//            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            startActivity(intent1);
//            finish();
//        }
       /* editor.putString(Constants.LOGIN_KEY, "c2da8e95138a4bd596a683e201f2a49f");
        editor.commit();
*/

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//        if (preferences.getBoolean("is_login", false)) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
                bluetoothAdapter.setName(preferences.getString(Constants.NAME_KEY, ""));
                Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBlueTooth, 1);
            } else {
                bluetoothEnabled();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1113);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1111);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1112);


            List<String> InstalledpackageName = Utils.getAllInstalledGames(this);
            if (Utils.isConnected(context)) {
                Call<User> userCall = apiCall.updateGameProfiles(preferences.getString(Constants.LOGIN_KEY, ""), InstalledpackageName);
                userCall.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {

                        if (response.body() == null) {
                            Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                        } else {
                            System.out.println(response.body().getId() + "    " + response.body().getName());
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(context, Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(getBaseContext(), Constants.INTERNET_ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
//        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case 1111:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1111);
                }
                return;
            case 1112:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1111);
                }
                return;
            case 1113:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1111);
                }
                return;

        }

    }


    @Override
    public void onBackPressed() {
        finish();
        //super.finish();
    }

    void bluetoothEnabled() {

        if (!checkThread) {
            ThreadConnection connection = new ThreadConnection(this);
            connection.start();

            AcceptBusinessThread acceptBusinessThread = new AcceptBusinessThread(BluetoothAdapter.getDefaultAdapter(), this);
            acceptBusinessThread.start();

            AcceptThread thread = new AcceptThread(BluetoothAdapter.getDefaultAdapter(), this);
            thread.start();

            GameRequestAcceptThread gameRequestAcceptThread = new GameRequestAcceptThread(BluetoothAdapter.getDefaultAdapter(), Home_Master.this);
            gameRequestAcceptThread.start();

            checkThread = true;

        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: " + resultCode);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK)
                bluetoothEnabled();

        } else {

            Toast.makeText(getApplicationContext(), "Please Turn On Bluetooth ", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_user_profile:
                Intent intent = new Intent(this, UserProfile.class);
                this.startActivity(intent);
                break;
        }

        return true;
    }


    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: " + v.getId());
        switch (v.getId()) {

            case R.id.file_card_id:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.chat_card_id:
                Intent chatIntent = new Intent(this, DeviceListActivityChat.class);
                startActivity(chatIntent);
                break;
            case R.id.business_card_id:
                Intent businessIntent = new Intent(this, BusinessCard.class);
                startActivity(businessIntent);
                break;
            case R.id.game_card_id:
                Intent gameIntent = new Intent(this, GameProfileActivity.class);
                startActivity(gameIntent);
                break;
            case R.id.nearby_card_id:
                Intent nearByIntent = new Intent(this, UserNearByWithGames.class);
                startActivity(nearByIntent);
                break;

        }


    }


    public void ReceiveMessage(String Message, final BluetoothSocket socket) {
        System.out.println(Message);
        String[] response = Message.split(":");
        if (Message.startsWith("Response")) {


        } else if (Message.startsWith("Request")) {
            System.out.println("here");
            System.out.println(Arrays.toString(response));
            Intent intent = new Intent(ImageCache.getContext(), UserResponseDialog.class);
            intent.putExtra("displayString", "Are You Want To Play " + response[1].split("\\$\\#\\$")[0] + " With " + response[2] + " ? ");
            intent.putExtra("device", socket.getRemoteDevice());
            intent.putExtra("packageName", response[1].split("\\$\\#\\$")[1]);
            ImageCache.getContext().startActivity(intent);



            /*final AlertDialog alertDialog;
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ImageCache.getContext());
            alertDialogBuilder.setMessage("Are You Want To Play " + response[1] + " With " + response[2] + " ? ");
            alertDialogBuilder.setPositiveButton("yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Toast.makeText(ImageCache.getContext(), "You clicked yes button", Toast.LENGTH_LONG).show();
                            try {
                                SharedPreferences prefs = ImageCache.getContext().getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
                                String name = prefs.getString(Constants.NAME_KEY, "");
                                String msg = "Response:1" + ":" + name;
                                gameRequestConnectThread = new GameRequestConnectThread(socket.getRemoteDevice(), 0);
                                gameRequestConnectThread.setResponse(msg);
                                gameRequestConnectThread.start();


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        SharedPreferences prefs = ImageCache.getContext().getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
                        String name = prefs.getString(Constants.NAME_KEY, "");
                        String msg = "Response:0" + ":" + name;
                        gameRequestConnectThread = new GameRequestConnectThread(socket.getRemoteDevice(), 0);
                        gameRequestConnectThread.setResponse(msg);
                        gameRequestConnectThread.start();
                        //socket.getOutputStream().write(msg.getBytes());
                        //socket.getOutputStream().flush();
                        // alertDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            alertDialog = alertDialogBuilder.create();
            alertDialog.show();*/
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }


}
