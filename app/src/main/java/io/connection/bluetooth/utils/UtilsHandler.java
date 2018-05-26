package io.connection.bluetooth.utils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import io.connection.bluetooth.MobiMixApplication;

/**
 * Created by songline on 10/08/16.
 */
public class UtilsHandler {
    private static ProgressDialog progressDialog = null;

    public static void runOnUiThread(Runnable runnable){
        Handler UIHandler = new Handler(
                Looper.getMainLooper());
        UIHandler.post(runnable);
    }

    public static void showProgressDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(MobiMixApplication.getInstance().getActivity(), "Press back to cancel", message, true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
            }
        });
    }

    public static void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    public static void launchGame(String gamePackageName) {
        Intent launchIntent = MobiMixApplication.getInstance().getContext().getPackageManager().
                getLaunchIntentForPackage(gamePackageName);
        MobiMixApplication.getInstance().getContext().startActivity(launchIntent);
    }

    public static void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MobiMixApplication.getInstance().getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

