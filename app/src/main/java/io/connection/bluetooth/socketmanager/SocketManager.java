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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.connection.bluetooth.Domain.QueueManager;
import io.connection.bluetooth.MobileMeasurementApplication;
import io.connection.bluetooth.Services.WifiDirectService;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.SocketOperationType;
import io.connection.bluetooth.socketmanager.modules.ReadBusinessCard;
import io.connection.bluetooth.socketmanager.modules.ReadChatData;
import io.connection.bluetooth.socketmanager.modules.ReadFiles;
import io.connection.bluetooth.socketmanager.modules.SendBusinessCard;
import io.connection.bluetooth.socketmanager.modules.SendFiles;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by KP49107 on 29-03-2017.
 */
public class SocketManager implements Runnable {
    private Socket socket;
    private SocketOperationType operationType = SocketOperationType.NONE;
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

                    if(operationType == SocketOperationType.READ) {
                        startReadModule();
                    }
                    else {
                        startWriteModule();
                    }
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

    public void startReadModule() {
        if(handler.isSocketConnected()) {
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

            operationType = SocketOperationType.NONE;
            handler.getWifiP2PService().setModule(Modules.NONE);
        }
    }

    public void startWriteModule() {
        if(handler.isSocketConnected()) {
            WifiDirectService wifiP2PService = handler.getWifiP2PService();

            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if( wifiP2PService != null ) {
                if(wifiP2PService.getModule() == Modules.BUSINESS_CARD) {
                    SendBusinessCard businessCard = new SendBusinessCard(socket, handler);
                    businessCard.sendCard();
                }
                else if(wifiP2PService.getModule() == Modules.FILE_SHARING) {
                    SendFiles sendFiles = new SendFiles(socket, handler);
                    sendFiles.start();
                }
            }

            operationType = SocketOperationType.NONE;
            handler.getWifiP2PService().setModule(Modules.NONE);
        }
    }

    public void readChatData() {
        operationType = SocketOperationType.READ;
        synchronized (this.obj) {
            this.obj.notify();
        }
    }

    public void readBusinessCard() {
        operationType = SocketOperationType.READ;
        synchronized (this.obj) {
            this.obj.notify();
        }
    }

    public void readFiles() {
        operationType = SocketOperationType.READ;
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
        operationType = SocketOperationType.WRITE;
        synchronized (this.obj) {
            this.obj.notify();
        }
    }

    public void writeFiles() {
        operationType = SocketOperationType.WRITE;
        synchronized (this.obj) {
            this.obj.notify();
        }
    }

    public String setRemoteDeviceHostAddress(String hostAddress) {
        return this.remoteHostAddress = hostAddress;
    }

    public String getRemoteDeviceAddress() {
        return this.remoteHostAddress;
    }

    public Socket getConnectedSocket() {
        return this.socket;
    }

    public void socketConnected() {
        handler.socketConnected();
    }

    public void socketClosed() {
        handler.socketClosed();
    }
}
