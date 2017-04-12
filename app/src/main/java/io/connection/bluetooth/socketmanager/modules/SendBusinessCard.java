package io.connection.bluetooth.socketmanager.modules;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by KP49107 on 12-04-2017.
 */
public class SendBusinessCard {
    private final Socket mSocket;
    private MessageHandler handler;
    private String TAG = SendBusinessCard.class.getSimpleName();

    public SendBusinessCard(Socket socket, MessageHandler handler) {
        mSocket = socket;
        this.handler = handler;
    }

    public void sendCard() {
        SharedPreferences prefs = ImageCache.getContext().getSharedPreferences("businesscard", Context.MODE_PRIVATE);
        String name = prefs.getString("name", "");
        String email = prefs.getString("email", "");
        String phone = prefs.getString("phone", "");
        String picture = prefs.getString("picture", "");
        String deviceId = prefs.getString("device_id", "");

        Uri file = Uri.parse(picture);

        int bufferSize = 1024;
        byte[] buffer = new byte[8 * bufferSize];

        try {
            DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
            try {

                File f = new File(file.getPath());
                long filelength = f.length();
                //uriFile = Uri.fromFile(f);
                Log.d(TAG, "sendFile: " + f.length());
                dos.writeUTF(name);
                dos.writeUTF(email);
                dos.writeUTF(phone);
                final String fileName = f.getName();
                dos.writeUTF(fileName);
                dos.writeUTF(deviceId);
                dos.writeInt((int) filelength);

                FileInputStream fis = new FileInputStream(f);
                int total = 0;
                int counting = 0;

                while (fis.read(buffer) > 0) {
                    dos.write(buffer);
                    Log.d(TAG, "doInBackground: " + filelength + "   " + total + "  counting " + counting);
                }
                dos.flush();
                fis.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
//                    dos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "disconnected", e);
        }

        //read message
        try {
            byte[] inBuffer = new byte[1024];
            int bytes;
            InputStream is = mSocket.getInputStream();

            if (is != null) {
                bytes = is.read(inBuffer);
                if (bytes != -1) {
                    System.out.println("Getting message" + new String(buffer));
                    handler.getHandler().obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
