package io.connection.bluetooth.socketmanager;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by KP49107 on 11-04-2017.
 */
public class SocketHeartBeat extends Thread {
    private WifiSocketManager wifiSocketManager;
    private boolean isSocketConnected = true;
    private String TAG = SocketHeartBeat.class.getSimpleName();

    public SocketHeartBeat(WifiSocketManager wifiSocketManager) {
        this.wifiSocketManager = wifiSocketManager;
    }

    @Override
    public void run() {
        Vector<Socket> socketsToMonitor = wifiSocketManager.getConnectedSocket();

        while (isSocketConnected) {
            try {
                Thread.sleep(5000);
                for(Socket socket : socketsToMonitor) {
                    InetAddress address = socket.getInetAddress();
                    if (address.isReachable(2500)) {
                        wifiSocketManager.sendHeartBeat();
                        continue;
                    }
                    else {
                        System.out.println("Socket disconnected event is sent from SocketHearBeat");
                        Log.d(TAG, "Remote device + " + address.getHostAddress() + " is not reachable");
                        this.wifiSocketManager.closeSocket();
                    }
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            } catch (InterruptedException i) {
                i.printStackTrace();
            }
            finally {
                isSocketConnected = false;
                socketsToMonitor.clear();
                if(!this.isInterrupted()) {
                    this.interrupt();
                }
            }
        }
    }
}
