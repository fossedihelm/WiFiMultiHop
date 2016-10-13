package it.unibo.mobile.d2dchat.socketManager;

import it.unibo.mobile.d2dchat.messagesManager.Message;

/**
 * Created by Stefano on 14/09/2016.
 */
public interface SocketHandler {

    void writeMessage(Message message);

    void stopHandler();
}
