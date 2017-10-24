package io.connection.bluetooth.core;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.lang.reflect.Method;

import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.actionlisteners.IUpdateListener;
import io.connection.bluetooth.activity.DialogActivity;
import io.connection.bluetooth.activity.Home_Master;
import io.connection.bluetooth.activity.PlayerListActivity;
import io.connection.bluetooth.adapter.GameAdapter;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 03/10/16.
 */
public class PushReceiveService extends FirebaseMessagingService {
    private static final String TAG = "PushReceiveService";
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    String contentText = "";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData().get("message"));

        String message = remoteMessage.getData().get("message");
        //Calling method to generate notification
        if (message != null) {
            try {
                JSONObject jsonObject = new JSONObject(message);

//                int hasInvite = jsonObject.optInt("connection_invite");
//                final String toUserId = jsonObject.optString("remote_user_id");
//                final String toUserName = jsonObject.optString("remote_user_name");
//                final String gameId = jsonObject.optString("game_id");
//                final String gameName = jsonObject.optString("game_name");

//                if (hasInvite == 1) {
//                    String bluetoothName = jsonObject.optString("bluetooth_address");
//                    generateBluetoothNotification(bluetoothName, toUserId);
//                } else if (hasInvite == 2) {
//                    String wifiDirectName = jsonObject.optString("wifi_address");
//                    generateWifiNotification(wifiDirectName, toUserId);
//                } else if (jsonObject.optInt("notification_message") == 1) {
//                    generateNearByUserNotification("User " + toUserId + " is nearby", toUserId);
//                }

                GameRequest request = new Gson().fromJson(message, GameRequest.class);

                if (request.getNotificationType() == 1) {
//                    String bluetoothName = jsonObject.optString("bluetooth_address");
                    generateBluetoothNotification(request);
                } else if (request.getNotificationType() == 2) {
//                    String wifiDirectName = jsonObject.optString("wifi_address");
                    generateWifiNotification(request);
                } else if (request.getNotificationType() == 3) {
//                    String wifiDirectName = jsonObject.optString("wifi_address");
                    launchGameAndUpdateConnectionInfo(request);
                } else if (request.getNotificationType() == 4) {
                    generateNearByUserNotification("User " + request.getRemoteUserName() + " is nearby");
                }
                else if (request.getNotificationType() == 5) {
                    launchGame(request);
                }
                else if (request.getNotificationType() == 6) {
                    acceptRequest(getBaseContext(), request);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");

            MobiMixApplication.getInstance().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertbox = new AlertDialog.Builder(getBaseContext())
                            //.setIcon(R.drawable.no)
                            .setTitle("Open Game")
                            .setMessage("Bluetooth connection is established. Do you want to open the game " + GameAdapter.gameName)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                                // do something when the button is clicked
                                public void onClick(DialogInterface arg0, int arg1) {

                                    Intent LaunchIntent = MobiMixApplication.getInstance().getContext().getPackageManager().
                                            getLaunchIntentForPackage(GameAdapter.gamePackageName);
                                    MobiMixApplication.getInstance().getContext().startActivity(LaunchIntent);
                                }
                            })
                            .show();

                }
            });
        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
        }
    }

    private void generateNearByUserNotification(String messageBody) {
        if (!isNotificationVisible()) {
            Intent intent = new Intent(this, PlayerListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Nearby users")
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        } else {
            contentText += messageBody;
            notificationBuilder.setContentText(contentText);
            notificationManager.notify(1, notificationBuilder.build());
        }
    }

    private void generateWifiNotification(GameRequest gameRequest) {
        Intent intent = new Intent(this, DialogActivity.class);
        intent.putExtra("game_request", gameRequest);
//        intent.putExtra("wifi_address", wifiDirectName);
//        intent.putExtra("toUserId", toUserId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentTitle("Wifi Direct connection")
                .setContentText("Do you want to make wifi direct connection with " + gameRequest.getRemoteUserName())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }

    private void generateBluetoothNotification(GameRequest gameRequest) {
        if (!isNotificationVisible()) {
            Intent intent = new Intent(this, DialogActivity.class);
            intent.putExtra("game_request", gameRequest);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_logo)
                    .setContentTitle("Bluetooth connection")
                    .setContentText("Do you want to make bluetooth Connection with " + gameRequest.getRemoteUserName())
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, notificationBuilder.build());
        } else {
            notificationBuilder.setContentText(contentText);
            notificationManager.notify(1, notificationBuilder.build());
        }
    }

    private boolean isNotificationVisible() {
        Intent notificationIntent = new Intent(this, Home_Master.class);
        PendingIntent test = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        return test != null;
    }

    private void launchGame(final GameRequest request) {
        UtilsHandler.launchGame(request.getGamePackageName());
    }

    private void acceptRequest(Context context, final GameRequest gameRequest) {
        if(gameRequest.getConnectionType() == 1) {
            BluetoothService.getInstance().acceptRequest(gameRequest);
        }
        else {
            WifiDirectService.getInstance(context).acceptRequest(gameRequest);
        }
    }

    private void launchGameAndUpdateConnectionInfo(final GameRequest request) {
        if(request.getConnectionType() == 1) {
            BluetoothService.getInstance().updateConnectionInfo(request, false, 0, new IUpdateListener() {
                @Override
                public void onUpdated() {
                    UtilsHandler.launchGame(request.getGamePackageName());
                }
            });
        }
        else {
            WifiDirectService.getInstance(MobiMixApplication.getInstance().getContext()).updateConnectionInfo(request, false, new IUpdateListener() {
                @Override
                public void onUpdated() {
                    UtilsHandler.launchGame(request.getGamePackageName());
                }
            });
        }
    }
}

