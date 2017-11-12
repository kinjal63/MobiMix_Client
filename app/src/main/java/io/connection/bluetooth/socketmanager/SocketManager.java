package io.connection.bluetooth.socketmanager;

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import io.connection.bluetooth.MobiMixApplication;
import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.SocketOperationType;
import io.connection.bluetooth.socketmanager.modules.ReadBusinessCard;
import io.connection.bluetooth.socketmanager.modules.ReadChatData;
import io.connection.bluetooth.socketmanager.modules.ReadFiles;
import io.connection.bluetooth.socketmanager.modules.ReadGameData;
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

    private WifiP2PRemoteDevice remoteDevice;
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
            os = socket.getOutputStream();
            is = socket.getInputStream();

            byte[] buffer = new byte[1024];
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

                    if (operationType == SocketOperationType.READ) {
                        startReadModule();
                    } else {
                        startWriteModule();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException i) {
                synchronized (this.obj) {
                    this.obj.notify();
                }
                if (!Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
                i.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception : " + e.toString());
        } finally {
//            try {
//                is.close();
//                socket.close();
//            } catch (IOException e) {
//                Log.e(TAG,"Exception during close socket or isStream",  e);
//            }
        }
    }

    public void startReadModule() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        WifiDirectService wifiP2PService = handler.getWifiP2PService();

        if (wifiP2PService != null) {
            if (wifiP2PService.getModule() == Modules.CHAT) {
                System.out.println("Socket is read 3 Chat");
                ReadChatData chatData = new ReadChatData(socket, handler);
                chatData.readChatData();
            } else if (wifiP2PService.getModule() == Modules.BUSINESS_CARD) {
                System.out.println("Socket is read 3 Business");
                ReadBusinessCard businessCard = new ReadBusinessCard(socket, handler);
                businessCard.readData();
            } else if (wifiP2PService.getModule() == Modules.FILE_SHARING) {
                System.out.println("Socket is read 3 file sharing");
                ReadFiles file = new ReadFiles(socket, handler);
                file.readFiles();
            } else if (wifiP2PService.getModule() == Modules.GAME) {
                System.out.println("Socket is read 3 file Game");
                ReadGameData file = new ReadGameData(socket, handler);
                file.readGameEvent();
            }
        }
        operationType = SocketOperationType.NONE;
    }

    public void startWriteModule() {
        if (handler.isSocketConnected()) {
            WifiDirectService wifiP2PService = handler.getWifiP2PService();
            System.out.println("Socket is write connected 2");

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (wifiP2PService != null) {
                if (wifiP2PService.getModule() == Modules.BUSINESS_CARD) {
                    SendBusinessCard businessCard = new SendBusinessCard(socket, handler);
                    businessCard.sendCard();
                } else if (wifiP2PService.getModule() == Modules.FILE_SHARING) {
                    SendFiles sendFiles = new SendFiles(socket, handler);
                    sendFiles.start();
                }
            }

            operationType = SocketOperationType.NONE;
//            handler.getWifiP2PService().setModule(Modules.NONE);
        }
    }

    public void readData() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send Game Object to remote user
    public void writeObject(Object object) {
        try {
            Thread.sleep(1000);
            if (os != null) {
                byte[] b = object.toString().getBytes();
                os.write(b);
                Log.v(TAG, "written Message::" + object.toString());
                os.flush();
            }
        } catch (IOException | InterruptedException e) {
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

    public WifiP2PRemoteDevice setRemoteDevice(String remoteDeviceAddress, String deviceName) {
        WifiP2pDevice device = new WifiP2pDevice();
        device.deviceAddress = remoteDeviceAddress;
        device.deviceName = deviceName;
        this.remoteDevice = new WifiP2PRemoteDevice(device, deviceName);

        return this.remoteDevice;
    }

    public WifiP2PRemoteDevice getRemoteDevice() {
        return this.remoteDevice;
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
