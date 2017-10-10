package io.connection.bluetooth.socketmanager;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.actionlisteners.SocketConnectionListener;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by Kinjal on 3/28/2017.
 */

public class WifiP2PClientHandler extends Thread {

    private Socket mSocket;
    private InetAddress mAddress;
    private MessageHandler mHandler;
    private SocketManager socketManager;
    private SocketConnectionListener mSocketConnectionListener;
    private String TAG = "WifiP2PClientHandler";

    public WifiP2PClientHandler(MessageHandler mHandler, InetAddress mAddress) {
        this.mHandler = mHandler;
        this.mAddress = mAddress;
    }

    @Override
    public void run() {
        mSocket = new Socket();
        try {
            mSocket.bind(null);
            mSocket.connect(new InetSocketAddress(mAddress.getHostAddress(), Constants.GROUP_OWNER_PORT));

            System.out.println("Hostname by client side :" + mSocket.getInetAddress().getHostName()
                    + ",Host Address by client side :" + mSocket.getInetAddress().getHostAddress());

            socketManager = new SocketManager(mSocket, mHandler);
            socketManager.setRemoteDeviceHostAddress(mAddress.getHostName());
            new Thread(socketManager).start();
        } catch (IOException e) {
            Log.e(TAG, "IOException throwed by socket", e);
            try {
                mSocket.close();
            } catch (IOException e1) {
                Log.e(TAG, "IOException during close Socket", e1);
            }
        }
    }

    /**
     * Method to close the client/peer socket and kill this entire thread.
     */
    public void closeSocketAndKillThisThread() {
        if(mSocket!=null && !mSocket.isClosed()) {
            try {
                mSocket.close();
                socketManager = null;
            } catch (IOException e) {
                Log.e(TAG,"IOException during close Socket" , e);
            }
        }

        //to interrupt this thread, without the threadpoolexecutor
        if(!this.isInterrupted()) {
            Log.d(TAG,"Stopping ClientSocketHandler");
            this.interrupt();
        }
    }

    public void checkSocketConnection(String hostName) {

    }
}
