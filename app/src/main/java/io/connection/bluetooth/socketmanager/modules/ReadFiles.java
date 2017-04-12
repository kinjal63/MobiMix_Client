package io.connection.bluetooth.socketmanager.modules;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by Kinjal on 4/8/2017.
 */

public class ReadFiles {
    Socket socket = null;
    InputStream in = null;
    OutputStream out = null;
    Context context;
    MessageHandler handler;
    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder;

    private static final String TAG = "readFile";

    public ReadFiles(Socket socket, MessageHandler handler) {
        this.socket = socket;
        this.handler = handler;
        this.context = ImageCache.getContext();

        notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void readFiles() {
        Log.d(TAG, "run:  reading Start");
        int bufferSize = 1024;
        byte[] buffer = new byte[8 * bufferSize];
        File[] files = null;

        handler.setModule(Modules.NONE);

        try {
            if (context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {


                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream(), buffer.length);
                DataInputStream dis = new DataInputStream(bis);
                int fileCount = dis.readInt();
                Log.d(TAG, "run:  filecount" + fileCount);
                files = new File[fileCount];

                for (int i = 0; i < fileCount; i++) {
                    String filename = System.nanoTime() + "";
                    long fileLength = dis.readLong();
                    try {

                        filename = dis.readUTF();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final String filenameReceive = filename;

                    mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.music_icon)
                            .setContentTitle("Receive File " + filenameReceive)
                            .setContentText("Recevie File in progress");

                    int counting = 0;
                    if (!new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").exists()) {
                        new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").mkdir();
                        new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/BusinessCard").mkdir();
                        new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/MediaFiles").mkdir();
                    }

                    files[i] = new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/MediaFiles", filename);
                    FileOutputStream fos = new FileOutputStream(files[i]);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    in = socket.getInputStream();
                    int len = 0;
                    int newBuffer = 8192;
                    for (counting = 0; counting < fileLength; ) {
                        len = bis.read(buffer, 0, newBuffer);
                        bos.write(buffer, 0, len);
                        counting += len;
                        if (fileLength - counting <= newBuffer
                                ) {
                            newBuffer = (int) (fileLength - counting);
                        }
                        mBuilder.setProgress(100, (int) ((counting * 100) / fileLength), false);
                        notificationManager.notify(i, mBuilder.build());

                        Log.d(TAG, "run: accept thread " + counting + "  -----  " + (fileLength - counting) + "  -- ----    ");
                    }

                    mBuilder.setContentText("Received Successfully").setProgress(0, 0, false);
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    String ext = files[i].getName().substring(files[i].getName().indexOf(".") + 1);
                    String type = mime.getMimeTypeFromExtension(ext.toLowerCase());
                    System.out.println(" ---->    " + mime.toString() + " \n    ------> " + ext + " \n -------> " + type);
                    Intent openFile = new Intent(Intent.ACTION_VIEW, Uri.fromFile(files[i]));
                    openFile.setDataAndType(Uri.fromFile(files[i]), type);
                    openFile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openFile, PendingIntent.FLAG_CANCEL_CURRENT);
                    mBuilder.setContentIntent(pendingIntent);
                    notificationManager.notify(i, mBuilder.build());
                    bos.flush();
                    final String filenameStandard = files[i].getAbsolutePath();
                    UtilsHandler.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                /*ImageCache.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse
                                        ("file://"
                                                + Environment.getExternalStorageDirectory())));*/

                            MediaScannerConnection.scanFile(ImageCache.getContext(), new String[]{

                                            filenameStandard},

                                    null, new MediaScannerConnection.OnScanCompletedListener() {

                                        public void onScanCompleted(String path, Uri uri)

                                        {

                                        }

                                    });


                        }
                    });


                    Log.d(TAG, "run:  file path " + files[i].getPath());
                }
                dis.close();

            }
        } catch (Exception e) {

            e.printStackTrace();
            Log.d(TAG, "run:  readFile  " + e.getMessage());
        } finally {
            try {
                WifiDirectService.getInstance(MobileMeasurementApplication.getInstance().getActivity()).closeSocket();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
