package io.connection.bluetooth.Thread;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import io.connection.bluetooth.R;
import io.connection.bluetooth.activity.ImageCache;
import io.connection.bluetooth.utils.Constants;
import io.connection.bluetooth.utils.UtilsHandler;

/**
 * Created by songline on 24/08/16.
 */
public class ThreadConnection {

    private static final String TAG = "ThreadConnection";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     */
    public ThreadConnection(Context context) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;

    }


    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        //  mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
      /*  if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
*/
        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure, List<Uri> uriList) {
        Log.d(TAG, "connect to: " + device);

        /*// Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }*/

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure, uriList);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType, List<Uri> uris) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        /*// Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        */

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType, uris);
        mConnectedThread.start();


        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }


    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Start the service over to restart listening mode
        ThreadConnection.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Start the service over to restart listening mode
        ThreadConnection.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {

                tmp = mAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME_UUID,
                        Constants.uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (true) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                    if (socket.isConnected()) {
                        FileReadThread readingFile = new FileReadThread(socket);
                        readingFile.start();
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                }

                // If a connection was accepted
             /*   if (socket != null) {
                    synchronized (ThreadConnection.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType, new ArrayList<Uri>());
                                break;
                        }
                    }
                }*/

                Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);
            }

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    private class FileReadThread extends Thread {
        private static final String TAG = "readFile";
        BluetoothSocket socket = null;
        InputStream in = null;
        OutputStream out = null;
        Context context;
        NotificationManager notificationManager;
        NotificationCompat.Builder mBuilder;

        FileReadThread(BluetoothSocket socket) {
            this.socket = socket;
            this.context = ImageCache.getContext();

            notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        @Override
        public void run() {
            Log.d(TAG, "run:  reading Start");
            int bufferSize = 1024;
            byte[] buffer = new byte[8 * bufferSize];
            File[] files = null;

            try {
                if (context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream(), buffer.length);
                    DataInputStream dis = new DataInputStream(bis);
                    int fileCount = dis.readInt();
                    Log.d(TAG, "run:  filecount" + fileCount);
                    files = new File[fileCount];

                    for (int i = 0; i < fileCount; i++) {
                        String filename = System.nanoTime() + "";
                        long fileLength = dis.readLong();
                        try {
                            filename = dis.readUTF();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        final String filenameReceive = filename;

                        mBuilder = new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.music_icon)
                                .setContentTitle("Receive File " + filenameReceive)
                                .setContentText("Recevie File in progress");

                        int counting = 0;
                        if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                    + "/TransferBluetooth").exists()) {
                            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                    + "/TransferBluetooth").mkdir();
                            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                    + "/TransferBluetooth/BusinessCard").mkdir();
                            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                    + "/TransferBluetooth/MediaFiles").mkdir();
                        }

                        files[i] = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                + "/TransferBluetooth/MediaFiles", filename);

                        files[i].createNewFile();
                        FileOutputStream fos = new FileOutputStream(files[i]);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        in = socket.getInputStream();
                        int len = 0;
                        int newBuffer = 8192;
                        for (counting = 0; counting < fileLength; ) {
                            len = bis.read(buffer, 0, newBuffer);
                            bos.write(buffer, 0, len);
                            counting += len;
                            if (fileLength - counting <= newBuffer
                                    ) {
                                newBuffer = (int) (fileLength - counting);
                            }
                            mBuilder.setProgress(100, (int) ((counting * 100) / fileLength), false);
                            notificationManager.notify(i, mBuilder.build());

                            Log.d(TAG, "run: accept thread " + counting + "  -----  " + (fileLength - counting) + "  -- ----    ");
                        }

                        mBuilder.setContentText("Received Successfully").setProgress(0, 0, false);
                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                        String ext = files[i].getName().substring(files[i].getName().indexOf(".") + 1);
                        String type = mime.getMimeTypeFromExtension(ext.toLowerCase());
                        System.out.println(" ---->    " + mime.toString() + " \n    ------> " + ext + " \n -------> " + type);
                        Intent openFile = new Intent(Intent.ACTION_VIEW, Uri.fromFile(files[i]));
                        openFile.setDataAndType(Uri.fromFile(files[i]), type);
                        openFile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openFile, PendingIntent.FLAG_CANCEL_CURRENT);
                        mBuilder.setContentIntent(pendingIntent);
                        notificationManager.notify(i, mBuilder.build());
                        bos.flush();
                        final String filenameStandard = files[i].getAbsolutePath();
                        UtilsHandler.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /*ImageCache.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse
                                        ("file://"
                                                + Environment.getExternalStorageDirectory())));*/

                                MediaScannerConnection.scanFile(ImageCache.getContext(), new String[]{

                                                filenameStandard},

                                        null, new MediaScannerConnection.OnScanCompletedListener() {

                                            public void onScanCompleted(String path, Uri uri)

                                            {

                                            }

                                        });


                            }
                        });


                        Log.d(TAG, "run:  file path " + files[i].getPath());
                    }
                    dis.close();

                }
            } catch (Exception e) {

                e.printStackTrace();
                Log.d(TAG, "run:  readFile  " + e.getMessage());
            } finally {
                try {
                    Log.d(TAG, "run: socket close");
                    socket.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;
        List<Uri> uriS;

        public ConnectThread(BluetoothDevice device, boolean secure, List<Uri> uriList) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            uriS = uriList;


            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {

                tmp = device.createRfcommSocketToServiceRecord(
                        Constants.uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (ThreadConnection.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType, uriS);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        NotificationManager notificationManager;
        NotificationCompat.Builder mBuilder;
        List<Uri> uri;

        public ConnectedThread(BluetoothSocket socket, String socketType, List<Uri> uris) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            uri = uris;

        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            int bufferSize = 1024;
            byte[] buffer = new byte[8 * bufferSize];
            int bytes;


            try {

                if (ImageCache.getContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ImageCache.getContext().checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    OutputStream os = mmSocket.getOutputStream();
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    DataOutputStream dos = new DataOutputStream(bos);
                    int size = uri.size();
                    dos.writeInt(size);
                    try {
                        int i = 0;

                        for (Uri uriFile : uri) {
                            File f = new File(uriFile.toString());
                            long filelength = f.length();
                            //uriFile = Uri.fromFile(f);
                            Log.d(TAG, "sendFile: " + f.length());


                            dos.writeLong(filelength);
                            final String fileName = f.getName();
                            dos.writeUTF(fileName);

                            notificationManager =
                                    (NotificationManager) ImageCache.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            mBuilder = new NotificationCompat.Builder(ImageCache.getContext())
                                    .setSmallIcon(R.drawable.music_icon)
                                    .setContentTitle("Sending File " + fileName)
                                    .setContentText("Sending in progress");


                            FileInputStream fis = new FileInputStream(f);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            int total = 0;
                            int theByte;
                            int counting = 0;

                            while ((theByte = bis.read(buffer)) != -1) {
                                total += theByte;
                                mBuilder.setProgress(100, (int) ((++total * 100) / f.length()), false);
                                notificationManager.notify(i, mBuilder.build());
                                bos.write(buffer, 0, theByte);
                                Log.d(TAG, "doInBackground: " + filelength + "   " + total + "  counting " + counting);

                            }


                            bis.close();
                            bos.flush();

                            mBuilder.setContentText("Send  Successfully").setProgress(0, 0, false);
                            notificationManager.notify(i, mBuilder.build());
                            i++;
                        }


                        //
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            Thread.sleep(3000);
                            dos.close();
                            mmSocket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                connectionLost();
                // Start the service over to restart listening mode
                ThreadConnection.this.start();
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}
