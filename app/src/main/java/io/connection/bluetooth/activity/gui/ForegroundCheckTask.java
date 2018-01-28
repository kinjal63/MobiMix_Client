package io.connection.bluetooth.activity.gui;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by Kinjal on 1/27/2018.
 */

public class ForegroundCheckTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private String appPackageName;

        public ForegroundCheckTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            this.appPackageName = params[0];
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = this.appPackageName;
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Use like this:
    boolean foregroud = new ForegroundCheckTask().execute(context).get();
}
