package it.unibo.mobile.d2dchat.device;

import android.net.wifi.p2p.WifiP2pInfo;

import it.unibo.mobile.d2dchat.messagesManager.Message;
import it.unibo.mobile.d2dchat.messagesManager.MessageManager;

/**
 * Created by asig on 12/9/16.
 */

public abstract class Peer {
    public abstract void onConnect(WifiP2pInfo info);
    public abstract void onDisconnect();
    public abstract void writeMessage(Message message);
    public abstract void receiveMessage(Message message, MessageManager messageManager);
}
