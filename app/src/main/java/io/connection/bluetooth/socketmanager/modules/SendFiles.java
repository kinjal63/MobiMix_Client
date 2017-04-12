package io.connection.bluetooth.socketmanager.modules;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import io.connection.bluetooth.Domain.QueueManager;
import io.connection.bluetooth.R;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by Kinjal on 4/9/2017.
 */

public class SendFiles extends Thread {
    private final Socket mSocket;
    private MessageHandler handler;
    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder;
    List<Uri> uri;

    private String TAG = "SendFiles";

    public SendFiles(Socket socket, MessageHandler handler) {
        this.mSocket = socket;
        this.handler = handler;
    }

    public void run() {
        Log.d(TAG, "BEGIN mConnectedThread");

        uri = QueueManager.getFilesToSend();

        int bufferSize = 1024;
        byte[] buffer = new byte[8 * bufferSize];

        try {

            if (ImageCache.getContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ImageCache.getContext().checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                OutputStream os = mSocket.getOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(os);
                DataOutputStream dos = new DataOutputStream(bos);
                int size = uri.size();
                dos.writeInt(size);
                try {
                    int i = 0;

                    for (Uri uriFile : uri) {
                        File f = new File(uriFile.toString());
                        long filelength = f.length();
                        //uriFile = Uri.fromFile(f);
                        Log.d(TAG, "sendFile: " + f.length());


                        dos.writeLong(filelength);
                        final String fileName = f.getName();
                        dos.writeUTF(fileName);

                        notificationManager =
                                (NotificationManager) ImageCache.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        mBuilder = new NotificationCompat.Builder(ImageCache.getContext())
                                .setSmallIcon(R.drawable.music_icon)
                                .setContentTitle("Sending File " + fileName)
                                .setContentText("Sending in progress");


                        FileInputStream fis = new FileInputStream(f);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        int total = 0;
                        int theByte;
                        int counting = 0;

                        while ((theByte = bis.read(buffer)) != -1) {
                            total += theByte;
                            mBuilder.setProgress(100, (int) ((++total * 100) / f.length()), false);
                            notificationManager.notify(i, mBuilder.build());
                            bos.write(buffer, 0, theByte);
                            Log.d(TAG, "doInBackground: " + filelength + "   " + total + "  counting " + counting);

                        }

                        bis.close();
                        bos.flush();
                        bos.close();

                        mBuilder.setContentText("Send Successfully").setProgress(0, 0, false);
                        notificationManager.notify(i, mBuilder.build());
                        i++;
                    }

                    //
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "disconnected", e);
        }

        //read message
//        try {
//            byte[] inBuffer = new byte[1024];
//            int bytes;
//            InputStream is = mSocket.getInputStream();
//
//            if (is != null) {
//                bytes = is.read(inBuffer);
//                if (bytes != -1) {
//                    System.out.println("Getting message" + new String(buffer));
//                    handler.getHandler().obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
//                }
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
