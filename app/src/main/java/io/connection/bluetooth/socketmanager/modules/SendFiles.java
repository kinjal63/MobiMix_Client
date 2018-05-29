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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
//    private final Socket mSocket;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private MessageHandler handler;
    NotificationManager notificationManager;
    NotificationCompat.Builder mBuilder;
    List<Uri> uri;

    private String TAG = "SendFiles";

    public SendFiles(ObjectOutputStream socket, ObjectInputStream ois, MessageHandler handler) {
        this.ois = ois;
        this.oos = socket;
        this.handler = handler;
    }

    public void run() {
        Log.d(TAG, "BEGIN mConnectedThread");

        uri = QueueManager.getFilesToSend();

        int bufferSize = 1024;
        byte[] buffer = new byte[8 * bufferSize];

        try {

            if (ImageCache.getContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ImageCache.getContext().checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                OutputStream os = mSocket.getOutputStream();
//                BufferedOutputStream bos = new BufferedOutputStream(os);
//                DataOutputStream os = new DataOutputStream(bos);
                int size = uri.size();
                oos.writeInt(size);
                try {
                    int i = 0;

                    for (Uri uriFile : uri) {
                        File f = new File(uriFile.toString());
                        long filelength = f.length();
                        //uriFile = Uri.fromFile(f);
                        Log.d(TAG, "sendFile: " + f.length());

                        oos.writeLong(filelength);
                        final String fileName = f.getName();
                        oos.writeUTF(fileName);

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
                            oos.write(buffer, 0, theByte);
                            Log.d(TAG, "doInBackground: " + filelength + "   " + total + "  counting " + counting);

                        }

                        bis.close();
                        oos.flush();
                        oos.close();

                        mBuilder.setContentText("Send Successfully").setProgress(0, 0, false);
                        notificationManager.notify(i, mBuilder.build());
                        i++;
                    }

                    //
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "disconnected", e);
        }

        //read message
        try {
            byte[] inBuffer;

//            BufferedInputStream bis = new BufferedInputStream(mSocket.getInputStream(), buffer.length);
//            DataInputStream dis = new DataInputStream(bis);

            if (ois != null) {
                inBuffer = ois.readUTF().getBytes();
                ois.close();

                System.out.println("Getting message" + new String(buffer));
                handler.getHandler().obtainMessage(Constants.MESSAGE_READ, 0, -1, inBuffer).sendToTarget();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
