package it.unibo.mobile.d2dchat.device;

import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.messagesManager.Message;
import it.unibo.mobile.d2dchat.messagesManager.MessageManager;

/**
 * Created by asig on 12/9/16.
 */


// Thread that generates and sends messages.
class MessageGenerator extends Thread {
    private DeviceManager deviceManager;
    private GroupOwner groupOwner;
    private Message message;
    public volatile boolean keepRunning = false;

    public MessageGenerator(DeviceManager deviceManager, GroupOwner groupOwner) {
        this.deviceManager = deviceManager;
        message = new Message();
        message.setType(Constants.MESSAGE_DATA);
        message.setDest(deviceManager.currentDest);
        message.setData(new char[1024]);
        message.setSeqNum(0);
    }

    @Override
    public void run() {
        while (keepRunning) {
            try {
                message.setDest(deviceManager.currentDest);
                message.setSendTime(System.currentTimeMillis());
                message.incSeqNum();
                groupOwner.getMessageManager().write(message);
                groupOwner.sent++;
                sleep(100);
            } catch (InterruptedException e) {
                Log.d("MessageGenerator", "sleep() interrupted, ignoring");
            }
        }
    }
}

public class GroupOwner extends Peer {
    private DeviceManager deviceManager;
    private static final String TAG = "GroupOwner";
    private ServerSocket socket = null;
    private MessageGenerator generator;
    private MessageManager messageManager;
    private long totalTimeReceivedMessages = 0;
    private int received = 0;
    public volatile int sent = 0;
    private int completedConnections = 0;

    public GroupOwner(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
        generator = new MessageGenerator(deviceManager, this);
        generator.start();
    }

    @Override
    public void onConnect(WifiP2pInfo info) {
        // every time a new wifi connection is established we need to create a new socket
        newSocket();
        try {
            Socket client = socket.accept();
            // stop old instance because it's using an old socket
            if (messageManager != null)
                messageManager.keepRunning = false;
            messageManager = new MessageManager(client, this);
            messageManager.start();
        } catch (IOException e) {
            try {
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException ioe) {

            }
            e.printStackTrace();
        }
        Log.d(TAG, "onConnect() created new connection");
        generator.keepRunning = true;
        generator.run();
    }

    @Override
    public void onDisconnect() {
        generator.keepRunning = false;
        messageManager.keepRunning = false;
        completedConnections++;
    }

    MessageManager getMessageManager() {
        return messageManager;
    }

    @Override
    public void receiveMessage(Message message, MessageManager manager) {
        //Message receiver is the owner
        if (message.getDest().equals(deviceManager.getDeviceName())) {
            // Discard message and record its arrival.
            received++;
            long totalTime = System.currentTimeMillis() - message.getSendTime();
            totalTimeReceivedMessages += totalTime;
            Log.d(TAG, "Received msg "+message.getSeqNum()+" after "+(float)totalTime/1000+" seconds.");
        }
    }

    @Override
    public void writeMessage(Message message) {
        messageManager.write(message);
    }

    public void newSocket() {
        try {
            socket = new ServerSocket(Constants.SERVER_PORT);
            Log.d("GroupOwnerSocketHandler", "Socket Started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
