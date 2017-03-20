package io.connection.bluetooth.Services;

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

import org.json.JSONObject;

import java.lang.reflect.Method;

import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.activity.Home_Master;
import io.connection.bluetooth.activity.UserList;
import io.connection.bluetooth.adapter.GameAdapter;

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
                int hasInvite = jsonObject.optInt("connection_invite");
                final String toUserId = jsonObject.optString("remote_user_id");

                if (hasInvite == 1) {
                    String bluetoothAddress = jsonObject.optString("bluetooth_address");
                    generateBluetoothNotification(toUserId, bluetoothAddress);
                } else if (hasInvite == 2) {
                    String wifiAddress = jsonObject.optString("wifi_address");
                    generateWifiNotification(wifiAddress, toUserId);
                } else if (jsonObject.optInt("notification_message") == 1) {
                    generateNearByUserNotification("User " + toUserId + " is nearby", toUserId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pairWithBluetooth(String bluetoothAddress) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
        pairDevice(device);
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");

            MobileMeasurementApplication.getInstance().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog alertbox = new AlertDialog.Builder(getBaseContext())
                            //.setIcon(R.drawable.no)
                            .setTitle("Open Game")
                            .setMessage("Bluetooth connection is established. Do you want to open the game " + GameAdapter.gameName)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                                // do something when the button is clicked
                                public void onClick(DialogInterface arg0, int arg1) {

                                    Intent LaunchIntent = MobileMeasurementApplication.getInstance().getContext().getPackageManager().
                                            getLaunchIntentForPackage(GameAdapter.gamePackageName);
                                    MobileMeasurementApplication.getInstance().getContext().startActivity(LaunchIntent);
                                }
                            })
                            .show();

                }
            });
        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
        }
    }

    private void generateNearByUserNotification(String messageBody, String toUserId) {
        if (!isNotificationVisible()) {
            Intent intent = new Intent(this, UserList.class);
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

    private void generateWifiNotification(String wifiAddress, String toUserId) {
        Intent intent = new Intent(this, Home_Master.class);
        intent.putExtra("wifi_address", wifiAddress);
        intent.putExtra("toUserId", toUserId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Wifi Direct connection")
                .setContentText("Do you want to make wifi direct connection with " + toUserId)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }

    private void generateBluetoothNotification(String toUserId, String bluetoothAdress) {
        if (!isNotificationVisible()) {
            Intent intent = new Intent(this, Home_Master.class);
            intent.putExtra("bluetooth_address", bluetoothAdress);
            intent.putExtra("toUserId", toUserId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Bluetooth connection")
                    .setContentText("Do you want to make bluetooth Connection with " + toUserId)
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
    


}

