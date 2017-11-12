package io.connection.bluetooth.utils;

import android.util.Log;

/**
 * Created by Kinjal on 11/12/2017.
 */

public class LogUtils {
    private static boolean isShowLog = true;

    public static void printLog(String tag, String message) {
        if(isShowLog) {
            Log.d(tag, message);
        }
    }
}
