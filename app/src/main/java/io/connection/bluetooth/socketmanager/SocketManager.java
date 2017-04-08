package io.connection.bluetooth.socketmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.connection.bluetooth.Database.BusinessCard;
import io.connection.bluetooth.Database.DataBaseHelper;
import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.BusinessCardReceivedList;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by KP49107 on 29-03-2017.
 */
public class SocketManager implements Runnable {
    private Socket socket;
    private MessageHandler handler;
    private InputStream is;
    private OutputStream os;
    private boolean disable = false;
    private String remoteHostAddress;

    private String TAG = "SocketManager";

    SocketManager(Socket socket, MessageHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();

            byte [] buffer = new byte[1024];
            int bytes;

            handler.getHandler().obtainMessage(Constants.FIRSTMESSAGEXCHANGE, this).sendToTarget();

            System.out.println("Sending first message");

//            while(!disable) {
            try {
                if (is != null) {
                    bytes = is.read(buffer);

                    System.out.println("Getting message" + new String(buffer));
                    handler.getHandler().obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
//            }
        }
        catch (IOException e) {
            Log.e(TAG,"Exception : " + e.toString());
        }
        finally {
            try {
                is.close();
                socket.close();
            } catch (IOException e) {
                Log.e(TAG,"Exception during close socket or isStream",  e);
            }
        }
    }

    public void readChatData() {
        byte [] buffer = new byte[1024];
        int bytes;

        while(!disable) {
            try {
                if (is != null) {
                    bytes = is.read(buffer);
                    if (bytes == -1) {
                        break;
                    }

                    System.out.println("Getting message" + new String(buffer));
                    handler.getHandler().obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readBusinessCard() {
        int bufferSize = 1024;
        byte[] buffer = new byte[8 * bufferSize];
        File files;
        DataBaseHelper db = new DataBaseHelper(ImageCache.getContext());
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
                        Intent intent = new Intent(ImageCache.getContext(), BusinessCardReceivedList.class);
                        ImageCache.getContext().startActivity(intent);

                    }
                });

            }
        }

    }

    public void readFiles() {

    }

    public void writeMessage(byte[] buffer) {
        try {
            if (os != null) {
                System.out.println("Writing message" + new String(buffer));

                os.write(buffer);
                os.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBusinessCard() {
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

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
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
//                    Thread.sleep(3000);
                    dos.close();
//                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "disconnected", e);
        }
    }

    public String setRemoteDeviceHostAddress(String hostAddress) {
        return this.remoteHostAddress = hostAddress;
    }

    public String getRemoteDeviceAddress() {
        return this.remoteHostAddress;
    }

    public void close() {
        if(socket!=null && !socket.isClosed()) {
            try {
                is.close();
                os.close();

                socket.close();
                disable = true;
            } catch (IOException e) {
                Log.e(TAG, "IOException during close Socket", e);
            }
        }
    }
}
