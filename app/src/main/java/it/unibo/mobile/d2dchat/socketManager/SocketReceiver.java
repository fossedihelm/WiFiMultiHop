package it.unibo.mobile.d2dchat.socketManager;

import it.unibo.mobile.d2dchat.messagesManager.Message;

/**
 * Created by Stefano on 26/07/2016.
 */
public interface SocketReceiver {

    void receiveMessage(int eventType, Message data);

}
