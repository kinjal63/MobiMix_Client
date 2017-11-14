package io.connection.bluetooth.socketmanager;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import io.connection.bluetooth.utils.MessageConstructor;

/**
 * Created by KP49107 on 11-04-2017.
 */
public class SocketHeartBeat extends Thread {
    private SocketManager socketManager;
    private boolean isSocketConnected = true;
    private String TAG = SocketHeartBeat.class.getSimpleName();

    public SocketHeartBeat(SocketManager socketManager) {
        this.socketManager = socketManager;
    }

    @Override
    public void run() {
        Socket socketToMonitor = socketManager.getConnectedSocket();
        InetAddress address = socketToMonitor.getInetAddress();

        while (isSocketConnected) {
            try {
                Thread.sleep(5000);
                if (address.isReachable(2500)) {
                    socketManager.sendHeartBeat();
                    continue;
                }
                isSocketConnected = false;
            } catch (IOException ie) {
                ie.printStackTrace();
                Log.d(TAG, "Remote device + " + address.getHostAddress() + " is not reachable");
            } catch (InterruptedException i) {
                i.printStackTrace();
            }
            finally {
                if(!isSocketConnected) {
                    System.out.println("Socket disconnected event is sent from SocketHearBeat");
                    this.socketManager.socketClosed();
                }
            }
        }
    }
}
