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
    private WifiSocketManager wifiSocketManager;
    private SocketConnectionListener mSocketConnectionListener;
    private String TAG = "WifiP2PClientHandler";

    public WifiP2PClientHandler(MessageHandler mHandler, InetAddress mAddress) {
        this.mHandler = mHandler;
        this.mAddress = mAddress;
    }

    @Override
    public void run() {
        try {
            tryToConnect();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void tryToConnect() throws InterruptedException {
        mSocket = new Socket();
        try {
            mSocket.bind(null);
            mSocket.connect(new InetSocketAddress(mAddress.getHostAddress(), Constants.GROUP_OWNER_PORT));

            System.out.println("Hostname by client side :" + mSocket.getInetAddress().getHostName()
                    + ",Host Address by client side :" + mSocket.getInetAddress().getHostAddress());

            wifiSocketManager = new WifiSocketManager(mSocket, mHandler);
            wifiSocketManager.setRemoteDeviceHostAddress(mAddress.getHostName());
            new Thread(wifiSocketManager).start();
        }
        catch (IOException e) {
            Thread.sleep(5000);
            tryToConnect();
            e.printStackTrace();
        }
    }

    /**
     * Method to close the client/peer socket and kill this entire thread.
     */
    public void closeSocketAndKillThisThread() {
        if(mSocket!=null && !mSocket.isClosed()) {
            try {
                mSocket.close();
                wifiSocketManager = null;
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
}
