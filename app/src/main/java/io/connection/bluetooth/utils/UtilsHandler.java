package io.connection.bluetooth.utils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import io.connection.bluetooth.MobileMeasurementApplication;

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

    public static void showProgressDialog(String message) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(MobileMeasurementApplication.getInstance().getActivity(), "Press back to cancel", message, true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}

