package io.connection.bluetooth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.receiver.BluetoothDeviceReceiver;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by Kinjal on 27-03-2017.
 */
public class DialogActivity extends Activity {
    private String toUserId;
    private Modules module;
    private BluetoothDeviceReceiver mBluetoothDeviceFoundReceiver;
    BluetoothAdapter bluetoothAdapter;
    private WifiDirectService wifiDirectService;

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
        else if( intent1 != null && intent1.getStringExtra("wifi_address") != null) {
            this.toUserId = intent1.getStringExtra("toUserId");
            showWifiDialog(intent1.getStringExtra("wifi_address"), toUserId);
        }
        else if( intent1 != null && intent1.getStringExtra("bluetooth_address") != null) {
            this.toUserId = intent1.getStringExtra("toUserId");
            showBluetoothDialog(intent1.getStringExtra("bluetooth_address"), toUserId);
        }
    }

    private void showBluetoothDialog(final String bluetoothName, final String toUserId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Bluetooth Connection Invite");

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you want to make bluetooth connection with " + bluetoothName)
                .setCancelable(false)
                .setIcon(R.drawable.bluetooth)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        if(Utils.getBluetoothAdapter() != null) {
                            mBluetoothDeviceFoundReceiver.setUserId(bluetoothName);
                            bluetoothAdapter.startDiscovery();
                        }
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showWifiDialog(final String wifiDirectName, final String toUserId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Wifi Direct Connection Invite");

        alertDialogBuilder
                .setMessage("Do you want to make wifi direct connection with " + wifiDirectName)
                .setCancelable(false)
                .setIcon(R.drawable.bluetooth)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        WifiDirectService.getInstance(MobileMeasurementApplication.getInstance().getActivity())
                                .setWifiDirectDeviceName(wifiDirectName);
                        WifiDirectService.getInstance(MobileMeasurementApplication.getInstance().getActivity())
                                .initiateDiscovery();
                        UtilsHandler.showProgressDialog("Connecting with " + wifiDirectName);
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
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
                        NetworkType networkType = which == 1 ? NetworkType.BLUETOOTH : NetworkType.WIFI_DIRECT;
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
                                break;
                            default:
                        }
                    }
                });
        builder.create().show();
    }
}
