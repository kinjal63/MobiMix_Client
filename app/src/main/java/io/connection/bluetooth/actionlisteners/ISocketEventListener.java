package io.connection.bluetooth.actionlisteners;

/**
 * Created by Kinjal on 5/1/2018.
 */

public interface ISocketEventListener {
    void socketInitialized(String remoteSocketAddress);
    void socketDiconnected();
}
