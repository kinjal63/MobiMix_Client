package io.connection.bluetooth.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by songline on 10/08/16.
 */
public class UtilsHandler {
    public static void runOnUiThread(Runnable runnable){

        Handler UIHandler = new Handler(
                Looper.getMainLooper());
        UIHandler.post(runnable);
    }

}

