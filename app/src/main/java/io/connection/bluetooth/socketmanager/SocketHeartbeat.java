package io.connection.bluetooth.socketmanager;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

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

        try {
            Thread.sleep(500);
        }
        catch (InterruptedException i) {
            i.printStackTrace();
        }
        this.socketManager.socketConnected();
        while (isSocketConnected) {
            try {
                Thread.sleep(5000);
                if (!address.isReachable(1500)) {
                    isSocketConnected = false;
                }
            } catch (IOException ie) {
                ie.printStackTrace();
                Log.d(TAG, "Remote device + " + address.getHostAddress() + " is not reachable");
            } catch (InterruptedException i) {
                i.printStackTrace();
            }
            finally {
                if(!isSocketConnected) {
                    this.socketManager.socketClosed();

                    if(!this.isInterrupted()) {
                        this.interrupt();
                    }
                }
            }
        }
    }
}
