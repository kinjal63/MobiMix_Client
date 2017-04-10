package io.connection.bluetooth.socketmanager.modules;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

import io.connection.bluetooth.Database.BusinessCard;
import io.connection.bluetooth.Database.DataBaseHelper;
import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.activity.BusinessCardReceivedList;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.socketmanager.SocketManager;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by Kinjal on 4/8/2017.
 */

public class ReadBusinessCard {
    private Socket socket;
    private boolean disable = false;
    private DataBaseHelper db;
    private String TAG = "ReadBusinessCard";

    public ReadBusinessCard(Socket socket) {
        this.socket = socket;
        db = new DataBaseHelper(ImageCache.getContext());
    }

    public void readData() {
        int bufferSize = 1024;
        byte[] buffer = new byte[8 * bufferSize];
        File files;

        FileOutputStream fos = null;

        while(!disable) {
            try {

                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream(), buffer.length);
                DataInputStream dis = new DataInputStream(bis);

                String name = dis.readUTF();
                String email = dis.readUTF();
                String phone = dis.readUTF();
                String filename = dis.readUTF();
                String deviceId = dis.readUTF();

                int fileLength = dis.readInt();

                Log.d(TAG, "run:  Start   \n   " + name + " \n" + email + " \n" + phone + "\n " + filename + " \n" + fileLength + "\n   End");


                int counting = 0;
                if (!new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").exists()) {
                    new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").mkdir();
                    new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/BusinessCard").mkdir();
                    new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/MediaFiles").mkdir();
                }

                files = new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/BusinessCard", filename);

                BusinessCard businessCard = new BusinessCard();
                businessCard.setName(name);
                businessCard.setPhone(phone);
                businessCard.setEmail(email);
                businessCard.setPicture(files.getPath());
                businessCard.setDeviceId(deviceId);

                long value = db.insertBusinessCard(businessCard);

                db.closeDB();
                fos = new FileOutputStream(files);
                int len = 0;
                int remaining = fileLength;

                while ((len = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                    counting += len;
                    remaining -= len;
                    System.out.println("read " + counting + " bytes.");
                    fos.write(buffer, 0, len);
                }
                Log.d(TAG, "run: data inserted id  is  " + value);


                fos.close();

                UtilsHandler.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ImageCache.getContext(), BusinessCardReceivedList.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        ImageCache.getContext().startActivity(intent);

                    }
                });

                WifiDirectService.getInstance(MobileMeasurementApplication.getInstance().getActivity()).closeSocket();

            } catch (Exception e) {
                try {
                    disable = true;
                    fos.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();

            } finally {
                try {
//                    WifiDirectService.getInstance(MobileMeasurementApplication.getInstance().getActivity()).closeSocket();
//                    socket.close();
                    disable = true;
                } catch (Exception ee) {
                    ee.printStackTrace();
                }

            }
        }
    }
}
