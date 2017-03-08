package io.connection.bluetooth.Services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by songline on 03/10/16.
 */
public class PushReceiveService extends FirebaseMessagingService {
    private static final String TAG = "PushReceiveService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getData() + "  "+remoteMessage.getData().get("message"));

    }
    


}

