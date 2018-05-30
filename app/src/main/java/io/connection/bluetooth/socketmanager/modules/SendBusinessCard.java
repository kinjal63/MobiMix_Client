package io.connection.bluetooth.socketmanager.modules;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by KP49107 on 12-04-2017.
 */
public class SendBusinessCard extends Thread {
    private final Socket mSocket;
    private MessageHandler handler;
    private String TAG = SendBusinessCard.class.getSimpleName();

    public SendBusinessCard(Socket socket, MessageHandler handler) {
        mSocket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        sendCard();
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

            DataObjectOutputStream oos = new DataObjectOutputStream(mSocket.getOutputStream());
            try {
                File f = new File(file.getPath());
                FileInputStream fis = new FileInputStream(f);
                fis.read(buffer);

                long filelength = f.length();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name", name);
                    jsonObject.put("email", email);
                    jsonObject.put("phone", phone);
                    jsonObject.put("picture", picture);
                    jsonObject.put("pictureData", new String(buffer));
                    jsonObject.put("device_id", deviceId);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                oos.writeObject(jsonObject.toString());

//                FileInputStream fis = new FileInputStream(f);
//                fis.read(buffer);
//                while (fis.read(buffer) > 0) {
//                    oos.write(buffer);
//                }
                fis.close();
                oos.flush();

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
            byte[] inBuffer;

            BufferedInputStream bis = new BufferedInputStream(mSocket.getInputStream(), buffer.length);
            DataInputStream dis = new DataInputStream(bis);

            if (dis != null) {
                inBuffer = dis.readUTF().getBytes();
                dis.close();

                System.out.println("Getting message" + new String(inBuffer));
                handler.getHandler().obtainMessage(Constants.MESSAGE_READ, 0, -1, inBuffer).sendToTarget();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
