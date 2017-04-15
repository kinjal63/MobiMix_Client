package io.connection.bluetooth.socketmanager;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by Kinjal on 3/28/2017.
 */

public class WifiP2PServerHandler extends Thread {
    private ServerSocket mSocket;
    private MessageHandler mHandler;
    private InetAddress ipAddress;
    private Socket clientSocket;

    private String TAG = "WifiP2PServerHandler";

    public WifiP2PServerHandler(MessageHandler mHandler) throws IOException {
        try {
            mSocket = new ServerSocket(Constants.GROUP_OWNER_PORT);
            this.mHandler = mHandler;
            Log.d("GroupOwnerSocketHandler", "Socket Started");
        } catch (IOException e) {
            Log.e(TAG, "IOException during open ServerSockets with port 5050", e);
            pool.shutdownNow();
            throw e;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                // A blocking operation. Initiate a ChatManager instance when
                // there is a new connection
                if(mSocket!=null && !mSocket.isClosed()) {
                    clientSocket = mSocket.accept(); //because now i'm connected with the client/peer device

                    this.mHandler.socketConnected();
                    ipAddress = clientSocket.getInetAddress();

                    SocketManager socketManager = new SocketManager(clientSocket, mHandler);

                    socketManager.setRemoteDeviceHostAddress(ipAddress.getHostName());
                    pool.execute(socketManager);
                    System.out.println("Hostname by server side :" + clientSocket.getInetAddress().getHostName()
                    + ",Host Address by server side :" + clientSocket.getInetAddress().getHostAddress());
                }
            } catch (IOException e) {
                //if there is an exception, after closing socket and pool, the execution stops with a "break".
                try {
                    if (mSocket != null && !mSocket.isClosed()) {
                        mSocket.close();
                    }
                } catch (IOException ioe) {
                    Log.e(TAG, "IOException during close Socket", ioe);
                }
                pool.shutdownNow();
                break; //stop the while(true).
            }
        }
    }

    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            Constants.THREAD_COUNT, Constants.THREAD_COUNT,
            Constants.THREAD_POOL_EXECUTOR_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());


    /**
     * Method to close the group owner sockets and kill this entire thread.
     */
    public void closeSocketAndKillThisThread() {
        if(mSocket!=null && !mSocket.isClosed()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException during close Socket", e);
            }
            pool.shutdown();
            Log.d(TAG,"Stopping ServerSocketHandler");
        }
    }

    public void checkSocketConnection(String hostName) {

    }
}
