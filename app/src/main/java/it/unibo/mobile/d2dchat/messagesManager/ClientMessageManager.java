package it.unibo.mobile.d2dchat.messagesManager;

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.Peer;
import it.unibo.mobile.d2dchat.device.Client;

/**
 * Created by ghosty on 27/04/17.
 */

public class ClientMessageManager extends MessageManager {
    protected static final String TAG = "ClientMessageManager";
    public volatile Semaphore connecting = new Semaphore(0);

    public ClientMessageManager(Peer peer) {
        super(peer);
        try {
            socket = new DatagramSocket(Constants.CLIENT_PORT);
            Log.d(TAG, "Socket started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        ArrayList<Message> currentQueue = ((Client) peer).getGoQueues()
                .get(peer.getDeviceManager().getGroupOwnerMacAddress());
        remoteAddress = peer.getDeviceManager().getInfo().groupOwnerAddress;
        if (currentQueue.isEmpty()) {
            Message message = new Message();
            ArrayList<String> goListToSend = new ArrayList<>();

            for (WifiP2pDevice device : peer.getDeviceManager().GOlist)
                goListToSend.add(device.deviceAddress);

            message.setData(goListToSend);
            message.setType(Constants.MESSAGE_REGISTER);
            send(message, Constants.SERVER_PORT);
        }

        super.run();
    }
}
