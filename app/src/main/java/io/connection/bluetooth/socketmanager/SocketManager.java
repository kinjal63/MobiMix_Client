package io.connection.bluetooth.socketmanager;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.connection.bluetooth.enums.Modules;
import io.connection.bluetooth.enums.NetworkType;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by KP49107 on 29-03-2017.
 */
public class SocketManager implements Runnable {
    private Socket socket;
    private Handler handler;
    private InputStream is;
    private OutputStream os;
    private boolean disable = false;
    private String remoteHostAddress;

    private String TAG = "SocketManager";

    SocketManager(Socket socket, Handler handler) {
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

            handler.obtainMessage(Constants.FIRSTMESSAGEXCHANGE, this).sendToTarget();

            while(!disable) {
                try {
                    if (is != null) {
                        bytes = is.read(buffer);
                        if (bytes == -1) {
                            break;
                        }

                        handler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e) {
            Log.e(TAG,"Exception : " + e.toString());
        }
        finally {
            try {
                is.close();
                socket.close();
            } catch (IOException e) {
                Log.e(TAG,"Exception during close socket or isStream",  e);
            }
        }
    }

    public String  setRemoteDeviceHostAddress(String hostAddress) {
        return this.remoteHostAddress = hostAddress;
    }

    public String getRemoteDeviceAddress() {
        return this.remoteHostAddress;
    }

    public void close() {
        if(socket!=null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException during close Socket", e);
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            if (os != null) {
                os.write(buffer);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
