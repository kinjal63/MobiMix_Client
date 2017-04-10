package io.connection.bluetooth.socketmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.socketmanager.modules.ReadBusinessCard;
import io.connection.bluetooth.socketmanager.modules.ReadChatData;
import io.connection.bluetooth.socketmanager.modules.ReadFiles;
import io.connection.bluetooth.socketmanager.modules.SendFiles;
import io.connection.bluetooth.utils.Constants;

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
    private String obj = "ThreadSync";

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

                    synchronized (this.obj) {
                        this.obj.wait();
                    }

                    startModule();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (InterruptedException i) {
                i.printStackTrace();
            }
//            }
        }
        catch (IOException e) {
            Log.e(TAG,"Exception : " + e.toString());
        }
        finally {
//            try {
//                is.close();
//                socket.close();
//            } catch (IOException e) {
//                Log.e(TAG,"Exception during close socket or isStream",  e);
//            }
        }
    }

    public void startModule() {
        if(socket.isConnected()) {
            WifiDirectService wifiP2PService = handler.getWifiP2PService();

            if( wifiP2PService != null ) {
                if(wifiP2PService.getModule() == Modules.CHAT) {
                    ReadChatData chatData = new ReadChatData(socket, handler);
                    chatData.readChatData();
                }
                else if(wifiP2PService.getModule() == Modules.BUSINESS_CARD) {
                    ReadBusinessCard businessCard = new ReadBusinessCard(socket);
                    businessCard.readData();
                }
                else if(wifiP2PService.getModule() == Modules.FILE_SHARING) {
                    ReadFiles file = new ReadFiles(socket);
                    file.readFiles();
                }
            }
        }
    }

    public void readChatData() {
        synchronized (this.obj) {
            this.obj.notify();
        }
    }

    public void readBusinessCard() {
        synchronized (this.obj) {
            this.obj.notify();
        }
    }

    public void readFiles() {
        synchronized (this.obj) {
            this.obj.notify();
        }
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
            Thread.sleep(2000);
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
        } catch (InterruptedException e) {
            Log.e(TAG, "disconnected", e);
        }


        //read message
        try {
            byte[] inBuffer = new byte[1024];
            int bytes;
            is = socket.getInputStream();

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

    public void writeFiles(List<Uri> files) {
        if( socket.isConnected() ) {
            SendFiles sendFiles = new SendFiles(socket, files);
            sendFiles.start();
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
