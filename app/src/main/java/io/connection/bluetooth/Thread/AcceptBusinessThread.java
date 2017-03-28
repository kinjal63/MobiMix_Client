package io.connection.bluetooth.Thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import io.connection.bluetooth.Database.BusinessCard;
import io.connection.bluetooth.Database.DataBaseHelper;
import io.connection.bluetooth.activity.BusinessCardReceivedList;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 07/09/16.
 */
public class AcceptBusinessThread extends Thread {
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a77");

    private final BluetoothServerSocket serverSocket;
    private static final String TAG = "AcceptThread";
    BluetoothSocket socket = null;

    Context context;

    public AcceptBusinessThread(BluetoothAdapter bluetoothAdapter, Context context) {
        BluetoothServerSocket tmp = null;
        this.context = context;

        try {

            tmp = bluetoothAdapter
                    .listenUsingRfcommWithServiceRecord(
                            "MyUUID", MY_UUID_SECURE);
        } catch (IOException e) {
        }
        serverSocket = tmp;
    }

    public void run() {


        while (true) {
            try {
                socket = serverSocket.accept();

                if (socket.isConnected()) {
                    Log.d(TAG, "run:  connection successfull");
                    Log.d(TAG, "run: " + socket.getRemoteDevice().getName() + "  " +
                            socket.getRemoteDevice().getAddress());
                    ReadBusinessCard readfile = new ReadBusinessCard(socket);
                    readfile.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: " + e.getMessage());
            }

        }
    }
}

class ReadBusinessCard extends Thread {
    private static final String TAG = "readFile";
    BluetoothSocket socket = null;
    InputStream in = null;
    OutputStream out = null;
    Context context;
    int bufferSize = 1024;
    byte[] buffer = new byte[8 * bufferSize];
    File files;
    DataBaseHelper db;


    ReadBusinessCard(BluetoothSocket socket) {
        this.socket = socket;
        this.context = ImageCache.getContext();
        db = new DataBaseHelper(context);
    }

    @Override
    public void run() {
        FileOutputStream fos = null;
        while (socket.isConnected()) {

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
                int newBuffer = 8192;
                int remaining = fileLength;

                while ((len = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                    counting += len;
                    remaining -= len;
                    System.out.println("read " + counting + " bytes.");
                    fos.write(buffer, 0, len);
                }
                Log.d(TAG, "run: data inserted id  is  " + value);


                fos.close();
                dis.close();

                // socket.close();
            } catch (Exception e) {
                try {
                    fos.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();

            } finally {
                try {
                    socket.close();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }


                UtilsHandler.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(context, BusinessCardReceivedList.class);
                        context.startActivity(intent);

                    }
                });

            }

        }

    }


}
