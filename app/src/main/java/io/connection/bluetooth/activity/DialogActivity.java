package io.connection.bluetooth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import io.connection.bluetooth.Api.WSManager;
import io.connection.bluetooth.Domain.GameConnectionInfo;
import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.actionlisteners.BluetoothPairCallback;
import io.connection.bluetooth.actionlisteners.DeviceConnectionListener;
import io.connection.bluetooth.adapter.GameAdapter;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.receiver.BluetoothDeviceReceiver;
import io.connection.bluetooth.socketmanager.WifiP2PClientHandler;
import io.connection.bluetooth.socketmanager.WifiP2PServerHandler;
import io.connection.bluetooth.utils.ApplicationSharedPreferences;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by Kinjal on 27-03-2017.
 */
public class DialogActivity extends Activity{
    private Modules module;
    private BluetoothDeviceReceiver mBluetoothDeviceFoundReceiver;
    BluetoothAdapter bluetoothAdapter;
    private String TAG = DialogActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothDeviceFoundReceiver = BluetoothDeviceReceiver.getInstance();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent1 = getIntent();
        if(intent1 != null && intent1.getStringExtra("module") != null &&
                intent1.getStringExtra("module").equalsIgnoreCase(Modules.CHAT.name())) {
            module = Modules.CHAT;
            showSendViaDialog("Initiate chat via");
        }
        else if(intent1 != null && intent1.getStringExtra("module") != null &&
                intent1.getStringExtra("module").equalsIgnoreCase(Modules.BUSINESS_CARD.name())) {
            module = Modules.BUSINESS_CARD;
            showSendViaDialog("Send business card via");
        }
        else if(intent1 != null && intent1.getStringExtra("module") != null &&
                intent1.getStringExtra("module").equalsIgnoreCase(Modules.FILE_SHARING.name())) {
            module = Modules.FILE_SHARING;
            showSendViaDialog("Send file(s) via");
        }
        else if( intent1 != null && intent1.getParcelableExtra("game_request") != null) {
            GameRequest request = intent1.getParcelableExtra("game_request");
            if( request != null && request.getNotificationType() == 2 ) {
                showWifiDialog(request);
            }
            else if( request != null && request.getNotificationType() == 1 ) {
                showBluetoothDialog(request);
            }
        }

//        else if( intent1 != null && intent1.getParcelableExtra("game_request") != null) {
//            this.toUserId = intent1.getStringExtra("toUserId");
//            showWifiDialog(intent1.getStringExtra("wifi_address"), toUserId);
//        }
//        else if( intent1 != null && intent1.getStringExtra("bluetooth_address") != null) {
//            this.toUserId = intent1.getStringExtra("toUserId");
//            showBluetoothDialog(intent1.getStringExtra("bluetooth_address"), toUserId);
//        }
    }

    private void showBluetoothDialog(final GameRequest gameRequest) {
        final String bluetoothName = gameRequest.getBluetoothAddress();
        final String gameName = gameRequest.getGameName();
        final String gamePackageName = gameRequest.getGamePackageName();
        final long gameId = gameRequest.getGameId();
        final String toUserId = gameRequest.getRemoteUserId();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Bluetooth Connection Invite");
        alertDialogBuilder
                .setMessage("Do you want to make bluetooth connection with " + bluetoothName)
                .setCancelable(false)
                .setIcon(R.drawable.bluetooth)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        if(Utils.getBluetoothAdapter() != null) {
                            mBluetoothDeviceFoundReceiver.pairWithDevice(bluetoothName, new BluetoothPairCallback() {
                                @Override
                                public void devicePaired(boolean isPaired) {
                                    if(isPaired) {
                                        UtilsHandler.launchGame(gamePackageName);
                                        updateConnectionInfo(gameRequest);
//                                        notifyRequester(gameId, toUserId, NetworkType.BLUETOOTH.ordinal());
                                    }
                                }
                            });
                            bluetoothAdapter.startDiscovery();
                            finish();
                        }
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showWifiDialog(final GameRequest gameRequest) {
        final String userName = gameRequest.getRemoteUserName();
        final String wifiDirectName = gameRequest.getWifiAddress();
        final String gameName = gameRequest.getGameName();
        final String gamePackageName = gameRequest.getGamePackageName();

        final WifiDirectService wifiDirectService = WifiDirectService.getInstance(DialogActivity.this);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Wifi Direct Connection Invite");
        alertDialogBuilder
                .setMessage("Do you want to make wifi direct connection with " + userName +
                            " to play game " + gameName)
                .setCancelable(false)
                .setIcon(R.drawable.wifidirect)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                        wifiDirectService.setWifiDirectDeviceName(wifiDirectName);
                        List<WifiP2PRemoteDevice> devices = wifiDirectService.getWifiP2PDeviceList();
                        for(WifiP2PRemoteDevice remoteDevice : devices) {
                            if( remoteDevice.getDevice().deviceName.equalsIgnoreCase(wifiDirectName) ) {
                                wifiDirectService
                                        .connectWithWifiAddress(remoteDevice.getDevice().deviceAddress, new DeviceConnectionListener() {
                                            @Override
                                            public void onDeviceConnected(boolean isConnected) {
                                                UtilsHandler.launchGame(gamePackageName);
                                                updateConnectionInfo(gameRequest);
                                            }
                                        });
                            }
                        }
                        WifiDirectService.getInstance(MobileMeasurementApplication.getInstance().getActivity())
                                .initiateDiscovery();
                        finish();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    public void showSendViaDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(message)
                .setItems(R.array.wifi_bluetooth_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        NetworkType networkType = which == 0 ? NetworkType.BLUETOOTH : NetworkType.WIFI_DIRECT;
                        switch (module) {
                            case CHAT:
                                Intent chatIntent = new Intent(DialogActivity.this, DeviceListActivityChat.class);
                                chatIntent.putExtra("networkType", networkType.name());
                                startActivity(chatIntent);
                                break;
                            case BUSINESS_CARD:
                                Intent businessIntent = new Intent(DialogActivity.this, BusinessCardListActivityUser.class);
                                businessIntent.putExtra("networkType", networkType.name());
                                startActivity(businessIntent);
                                break;
                            case FILE_SHARING:
                                Class className = networkType == NetworkType.BLUETOOTH ? MainActivity.class : WifiDirectMainActivity.class;
                                Intent intent = new Intent();
                                intent.setClass(DialogActivity.this, className);
                                startActivity(intent);
                                break;
                            default:
                        }
                        finish();
                    }
                });
        builder.create().show();
    }

    private void updateConnectionInfo(GameRequest gameRequest) {
        WifiDirectService.getInstance(this).updateConnectionInfo(gameRequest, true);
    }
}
