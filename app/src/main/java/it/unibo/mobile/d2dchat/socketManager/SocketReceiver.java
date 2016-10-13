package it.unibo.mobile.d2dchat.socketManager;

/**
 * Created by Stefano on 26/07/2016.
 */
public interface SocketReceiver {

    void receiveMessage(int eventType, Object data);

}
