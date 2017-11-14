package io.connection.bluetooth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.R;
import io.connection.bluetooth.actionlisteners.BluetoothPairCallback;
import io.connection.bluetooth.actionlisteners.DeviceConnectionListener;
import io.connection.bluetooth.actionlisteners.IUpdateListener;
import io.connection.bluetooth.activity.gui.GUIManager;
import io.connection.bluetooth.core.BluetoothService;
import io.connection.bluetooth.core.EventData;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.receiver.BluetoothDeviceReceiver;
import io.connection.bluetooth.utils.Utils;
import io.connection.bluetooth.utils.UtilsHandler;
import io.connection.bluetooth.utils.cache.MobiMixCache;

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
            if( request != null && request.getConnectionType() == 2 ) {
                showWifiDialog(request);
            }
            else if( request != null && request.getConnectionType() == 1 ) {
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
        final String gamePackageName = gameRequest.getGamePackageName();

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
                                        BluetoothService.getInstance().updateConnectionInfo(gameRequest, true, 1, new IUpdateListener() {
                                            @Override
                                            public void onUpdated() {
                                                UtilsHandler.launchGame(gameRequest.getGamePackageName());
                                            }
                                        });
                                        finish();
                                    }
                                }
                            });
                            bluetoothAdapter.startDiscovery();
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
        final String remoteUserName = gameRequest.getRemoteUserName();
        final String gameName = gameRequest.getGameName();
        final String gamePackageName = gameRequest.getGamePackageName();
        final String remoteUserId = gameRequest.getRemoteUserId();

        final WifiDirectService wifiDirectService = WifiDirectService.getInstance(DialogActivity.this);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Wifi Direct Connection Invite");
        alertDialogBuilder
                .setMessage("Do you want to make wifi direct connection with " + remoteUserName +
                            " to play game " + gameName)
                .setCancelable(false)
                .setIcon(R.drawable.wifidirect)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        MobiMixCache.putGameInCache(remoteUserId, gameRequest);

                        EventData eventData = new EventData();
                        eventData.event_ = MobiMix.GameEvent.EVENT_GAME_LAUNCHED;
                        eventData.userId_ = remoteUserId;
                        GUIManager.getObject().sendEvent(eventData);

                        UtilsHandler.launchGame(gamePackageName);

//                        boolean isDeviceFound = false;
//
//                        wifiDirectService.setWifiDirectDeviceName(wifiDirectName);
//                        List<WifiP2PRemoteDevice> devices = wifiDirectService.getWifiP2PDeviceList();
//                        for(WifiP2PRemoteDevice remoteDevice : devices) {
//                            if( remoteDevice.getDevice().deviceName.equalsIgnoreCase(wifiDirectName) ) {
//                                isDeviceFound = true;
//
//                                UtilsHandler.addGameInStack(gameRequest);
//
//                                wifiDirectService
//                                        .connectWithWifiAddress(remoteDevice.getDevice().deviceAddress, new DeviceConnectionListener() {
//                                            @Override
//                                            public void onDeviceConnected(boolean isConnected) {
//                                                Toast.makeText(DialogActivity.this, "Wait a moment, Game is launching...", Toast.LENGTH_LONG).show();
////                                                updateConnectionInfo(gameRequest);
//                                                System.out.println("Updating connection info + before");
////                                                WifiDirectService.getInstance(DialogActivity.this).updateConnectionInfo(gameRequest, true, new IUpdateListener() {
////                                                    @Override
////                                                    public void onUpdated() {
////                                                        System.out.println("Updating connection info + Updated");
////                                                        UtilsHandler.removeGameFromStack();
////                                                        finish();
////                                                    }
////                                                });
//                                            }
//                                        });
//                            }
//                        }
//
//                        if(!isDeviceFound) {
//                            Toast.makeText(DialogActivity.this, "Device " + wifiDirectName + " is not found", Toast.LENGTH_LONG).show();
//                            finish();
////                            wifiDirectService.getWifiP2PManager().requestConnectionInfo(wifiDirectService.getWifiP2PChannel(),
////                                    new WifiP2pManager.ConnectionInfoListener() {
////                                        @Override
////                                        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
////                                            if(wifiP2pInfo != null) {
////                                                updateConnectionInfo(gameRequest);
////                                            }
////                                        }
////                                    });
////                            wifiDirectService.getWifiP2PManager().requestGroupInfo(wifiDirectService.getWifiP2PChannel(),
////                                    new WifiP2pManager.GroupInfoListener() {
////                                        @Override
////                                        public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
////                                            if (wifiP2pGroup != null) {
////                                                if(wifiP2pGroup.getOwner().deviceName.equalsIgnoreCase(wifiDirectName)) {
////                                                    connectWithDevice(wifiP2pGroup.getOwner().deviceAddress, gameRequest);
////                                                }
////                                                else {
////                                                    Collection<WifiP2pDevice> devices = wifiP2pGroup.getClientList();
////                                                    for (WifiP2pDevice device : devices) {
////                                                        if (device.deviceName.equalsIgnoreCase(wifiDirectName)) {
////                                                            connectWithDevice(device.deviceAddress, gameRequest);
////                                                        }
////                                                    }
////                                                }
////                                            }
////                                        }
////                                    });
//                        }
//                        WifiDirectService.getInstance(MobiMixApplication.getInstance().getActivity())
//                                .initiateDiscovery();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        wifiDirectService.closeConnection();
                        wifiDirectService.removeGroup();
                        finish();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void connectWithDevice(String deviceAddress, final GameRequest gameRequest) {
        WifiDirectService.getInstance(getApplicationContext())
                .connectWithWifiAddress(deviceAddress, new DeviceConnectionListener() {
                    @Override
                    public void onDeviceConnected(boolean isConnected) {
                        updateConnectionInfo(gameRequest);
                    }
                });
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

    private void updateConnectionInfo(final GameRequest gameRequest) {
        System.out.println("Updating connection info + before");
        WifiDirectService.getInstance(this).updateConnectionInfo(gameRequest, true, new IUpdateListener() {
            @Override
            public void onUpdated() {
                System.out.println("Updating connection info + Updated");
//                UtilsHandler.launchGame(gameRequest.getGamePackageName());
            }
        });
    }
}
