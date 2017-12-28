package io.connection.bluetooth.socketmanager;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;

import io.connection.bluetooth.Thread.MessageHandler;
import io.connection.bluetooth.Thread.module.ReadGameEventData;
import io.connection.bluetooth.core.BluetoothService;
import io.connection.bluetooth.core.EventData;
import io.connection.bluetooth.core.MobiMix;
import io.connection.bluetooth.utils.Constants;

/**
 * Created by Kinjal on 12/23/2017.
 */

public class BluetoothSocketManager implements Runnable {
    private BluetoothSocket socket;
    private OutputStream os;
    private MessageHandler handler;

    public BluetoothSocketManager(BluetoothSocket socket) {
        this.socket = socket;
        init();
    }

    private void init() {
        this.handler = BluetoothService.getInstance().handler();
        handler.setManager(this);
        startToListenEvents();
    }

    private void startToListenEvents() {
        ReadGameEventData readData = new ReadGameEventData(socket, handler);
        readData.start();
    }

    public void sendConnectionEstablishedEvent() {
        EventData eventData = new EventData(MobiMix.GameEvent.EVENT_CONNECTION_ESTABLISHED_ACK);
        handler.sendEvent(eventData);
    }

    @Override
    public void run() {
        try {
            os = socket.getOutputStream();
//            handler.getHandler().obtainMessage(Constants.FIRSTMESSAGEXCHANGE_BLUETOOTH, this).sendToTarget();
        }
        catch (IOException e) {
            e.printStackTrace();
            os = null;
        }
    }

    public void writeObject(Object object) {
        try {
            os.write(object.toString().getBytes());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            if (socket != null) {
                if(os != null)
                    os.close();
                socket.close();

                if(!Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
