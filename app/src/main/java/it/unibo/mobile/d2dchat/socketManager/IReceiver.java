package it.unibo.mobile.d2dchat.socketManager;

import it.unibo.mobile.d2dchat.messagesManager.MessageManager;
import it.unibo.mobile.d2dchat.messagesManager.Message;

/**
 * Created by Stefano on 22/07/2016.
 */
public interface IReceiver {

    void receiveMessage(Message message, MessageManager manager);

}
