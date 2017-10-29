package io.connection.bluetooth.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import io.connection.bluetooth.Domain.GameRequest;
import io.connection.bluetooth.R;
import io.connection.bluetooth.activity.DialogActivity;

/**
 * Created by KP49107 on 17-04-2017.
 */
public class NotificationUtil {
    private static NotificationManager notificationManager;
    private static Context mContext;

    public static void initialize(Context context) {
        mContext = context;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void sendChatNotification(Intent intent, String message, String userName) {
        if(!ApplicationSharedPreferences.getInstance(mContext).getBooleanValue(Constants.PREF_CHAT_ACTIVITY_OPEN)) {
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                    0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_logo)
                    .setContentTitle(userName)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            notificationManager.notify(1, notificationBuilder.build());
        }
    }

    public static void generateNotificationForGameRequest(GameRequest gameRequest) {
        Intent intent = new Intent(mContext, DialogActivity.class);
        intent.putExtra("game_request", gameRequest);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPER_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentTitle("Wifi Direct connection")
                .setContentText("Do you want to make wifi direct connection with " + gameRequest.getRemoteUserName())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }
}
