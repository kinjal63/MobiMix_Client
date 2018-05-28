package io.connection.bluetooth.socketmanager;

import android.net.wifi.p2p.WifiP2pDevice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.adapter.model.WifiP2PRemoteDevice;
import io.connection.bluetooth.core.WifiDirectService;
import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.SocketOperationType;
import io.connection.bluetooth.socketmanager.modules.DataObjectOutputStream;
import io.connection.bluetooth.socketmanager.modules.ReadBusinessCard;
import io.connection.bluetooth.socketmanager.modules.ReadChatData;
import io.connection.bluetooth.socketmanager.modules.ReadFiles;
import io.connection.bluetooth.socketmanager.modules.ReadGameData;
import io.connection.bluetooth.socketmanager.modules.SendBusinessCard;
import io.connection.bluetooth.socketmanager.modules.SendFiles;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.Utils;

/**
 * Created by KP49107 on 29-03-2017.
 */
public class WifiSocketManager implements Runnable {
    private SocketOperationType operationType = SocketOperationType.NONE;
    private MessageHandler handler;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private WifiP2PRemoteDevice remoteDevice;
    private String remoteHostAddress;
    private String obj = "WifiSocket";
    public boolean isServer = false;
    private boolean running = true;

    private String TAG = "SocketManager";
    private HashMap<String, Socket> sockets = new HashMap<>();
    private HashMap<String, ObjectOutputStream> oosMap = new HashMap<>();
    private Stack<Socket> clientSockets = new Stack<>();
    private Socket socket = null;

    WifiSocketManager(Socket socket, MessageHandler handler, boolean isServer) {
        this.socket = socket;
        this.handler = handler;
        this.isServer = isServer;
    }

    @Override
    public void run() {
        if (isServer && !this.clientSockets.empty()) {
            Socket clientSocket = this.clientSockets.pop();
            String clientAddress = "/" + clientSocket.getRemoteSocketAddress().toString().split("/")[1];
            sockets.put(clientAddress, clientSocket);
            handleSocket(clientSocket, clientAddress);
        }
        else if(!isServer) {
            running = false;
            handleSocket(socket, getConnectedSocketInetAddress());
        }

        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            closeSocket();
        }
    }

    private synchronized void handleSocket(Socket socket, String socketAddress) {
        byte[] buffer = new byte[1024];
        int bytes;

        JSONObject obj = new JSONObject();
        try {
            obj.put("wifi_socket_manager", this);
            obj.put("wifi_socket_client", socketAddress);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        handler.getHandler().obtainMessage(Constants.FIRSTMESSAGEXCHANGE, obj).sendToTarget();
        System.out.println("Sending first message");
//            while(!disable) {
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            if (ois != null) {
//                bytes = ois.read(buffer, 0, buffer.length);
                String msg = (String)ois.readObject();
//                ois.close();

//                buffer = object.toString().getBytes();
//                bytes = buffer.length;

                buffer = msg.getBytes();
                bytes = buffer.length;
                System.out.println("Getting message" + msg);

                handler.getHandler().obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                synchronized (this.obj) {
                    this.obj.wait();
                }

                if (operationType == SocketOperationType.READ) {
                    startReadModule(socket);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
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
    }

    public void startReadModule(Socket socket) {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        WifiDirectService wifiP2PService = handler.getWifiP2PService();

        if (wifiP2PService != null) {
            if (wifiP2PService.getModule() == Modules.CHAT) {
                System.out.println("Socket is read 3 Chat");
                ReadChatData chatData = new ReadChatData(ois, handler);
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
                ReadGameData file = new ReadGameData(ois, handler);
                file.readGameEvent();
            }
        }
        operationType = SocketOperationType.NONE;
    }

    public void startWriteModule(Socket socket) {
//        if (handler.isSocketConnected()) {
//            WifiDirectService wifiP2PService = handler.getWifiP2PService();
//            System.out.println("Socket is write connected 2");
//
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if (wifiP2PService != null) {
//                if (wifiP2PService.getModule() == Modules.BUSINESS_CARD) {
//                    SendBusinessCard businessCard = new SendBusinessCard(socket, handler);
//                    businessCard.sendCard();
//                } else if (wifiP2PService.getModule() == Modules.FILE_SHARING) {
//                    SendFiles sendFiles = new SendFiles(socket, handler);
//                    sendFiles.start();
//                }
//            }
//
//            operationType = SocketOperationType.NONE;
////            handler.getWifiP2PService().setModule(Modules.NONE);
//        }
    }

    public void readData() {
        operationType = SocketOperationType.READ;
        synchronized (this.obj) {
            this.obj.notify();
        }
    }

    public synchronized void sendHeartBeat() {
        handler.getHandler().obtainMessage(Constants.MESSAGE_HEARBEAT, this);
    }

    // Send Game Object to remote user
    public void writeObject(String socketAddr, Object object) {
        try {
            Socket socket = this.socket;
            ObjectOutputStream oos = null;
            if(sockets != null && !sockets.isEmpty()) {
                socket = sockets.get(socketAddr);
            }

            if(!oosMap.containsKey(socketAddr)) {
                oosMap.put(socketAddr, new ObjectOutputStream(socket.getOutputStream()));
            }
            oos = oosMap.get(socketAddr);

            oos.writeObject(object.toString());
            oos.flush();
//            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFileObject(String socketAddr, Modules module) {
        Socket socket = this.socket;
        if(sockets != null && !sockets.isEmpty()) {
            socket = sockets.get(socketAddr);
        }
        if(module == Modules.BUSINESS_CARD) {
            SendBusinessCard businessCard = new SendBusinessCard(socket, handler);
            businessCard.start();
        }
        else if(module == Modules.FILE_SHARING) {
            SendFiles sendFiles = new SendFiles(socket, handler);
            sendFiles.start();
        }
    }

    public synchronized void sendToAll(Object object) {
        try {
            for(Socket socket: this.sockets.values()) {
                oos = new ObjectOutputStream(socket.getOutputStream());
                byte[] b = object.toString().getBytes();

                oos.writeObject(object);
                oos.flush();
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

    public void addClientSocket(Socket socket) {
        this.clientSockets.push(socket);
    }

    public String getConnectedSocketInetAddress() {
        if(Utils.isGroupOwner()) {
            return "/" + socket.getRemoteSocketAddress().toString().split("/")[1];
        }
        else {
            return "/" + socket.getLocalSocketAddress().toString().split("/")[1];
        }
    }

    public String getRemoteDeviceAddress() {
        return this.remoteHostAddress;
    }

    public Vector<Socket> getConnectedSocket() {
        return this.clientSockets;
    }

    public void closeSocket() {
        handler.closeSocket();
    }

    public void closeSockets() {
        try {
            for (Socket socket : this.sockets.values()) {
                socket.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocketAndKillThread() {
        closeSockets();
        sockets.clear();
        socket = null;
        oosMap.clear();
    }
}
