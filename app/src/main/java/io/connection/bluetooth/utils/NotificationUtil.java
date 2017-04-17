package io.connection.bluetooth.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.R;

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
}
