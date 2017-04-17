package io.connection.bluetooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import io.connection.bluetooth.Services.BluetoothService;

/**
 * Created by KP49107 on 17-04-2017.
 */
public class BaseActivity extends AppCompatActivity {
    BluetoothService bluetoothService;

    public static String getName() {
        return BaseActivity.class.getName();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        bluetoothService = BluetoothService.getInstance();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getName(), "onActivityResult: " + resultCode);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                bluetoothService.initiateDiscovery();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please Turn On Bluetooth ", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
