package io.connection.bluetooth.socketmanager.modules;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

import io.connection.bluetooth.Database.BusinessCard;
import io.connection.bluetooth.Database.DataBaseHelper;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.BusinessCardReceivedList;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by Kinjal on 4/8/2017.
 */

public class ReadBusinessCard implements Runnable {
    private Socket socket;
    private ObjectInputStream ois;
    private MessageHandler handler;
    private boolean disable = false;
    private DataBaseHelper db;
    private String TAG = "ReadBusinessCard";

    public ReadBusinessCard(ObjectInputStream socket, MessageHandler handler) {
        this.ois = socket;
        this.handler = handler;
        db = new DataBaseHelper(ImageCache.getContext());
    }

    @Override
    public void run() {
        int bufferSize = 1024;
        byte[] buffer = new byte[8 * bufferSize];
        File files;

        FileOutputStream fos = null;
        handler.setModule(Modules.NONE);

        while(!disable) {
            try {
                Log.d(TAG, "Starting to read business card");

//                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream(), buffer.length);
//                DataInputStream dis = new DataInputStream(bis);

//                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                String data = (String)ois.readObject();
                JSONObject jObject = new JSONObject(data);

                String name = jObject.optString("name");
                String email = jObject.optString("email");
                String phone = jObject.optString("phone");
                String filename = jObject.optString("picture");
                String fileData = jObject.optString("pictureData");
                String deviceId = jObject.optString("device_id");

                int fileLength = jObject.optInt("fileLength");

                Log.d(TAG, "run:  Start   \n   " + name + " \n" + email + " \n" + phone + "\n " + filename + " \n" + fileLength + "\n   End");


                int counting = 0;
                if (!new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").exists()) {
                    new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth").mkdir();
                    new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/BusinessCard").mkdir();
                    new File(Environment.getExternalStorageDirectory() + "/TransferBluetooth/MediaFiles").mkdir();
                }

                files = new File(filename);
                if(!files.exists()) {
                    files.createNewFile();
                }

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

//                while ((len = ois.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
//                    counting += len;
//                    remaining -= len;
//                    System.out.println("read " + counting + " bytes.");
//                    fos.write(buffer, 0, len);
//                }
                fos.write(fileData.getBytes());
                Log.d(TAG, "run: data inserted id  is  " + value);

                fos.close();

                UtilsHandler.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ImageCache.getContext(), BusinessCardReceivedList.class);
                        intent.putExtra("received", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        ImageCache.getContext().startActivity(intent);
                    }
                });

//                WifiDirectService.getInstance(MobileMeasurementApplication.getInstance().getActivity()).closeWifiSocket();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    disable = true;
                    fos.close();

                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(MobiMix.ScoketEvents.EVENT_BUSINESSCARD_RECEIVED);

                    Thread.sleep(1000);
                    handler.closeWifiSocket();
                    Thread.currentThread().interrupt();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
    }
}
