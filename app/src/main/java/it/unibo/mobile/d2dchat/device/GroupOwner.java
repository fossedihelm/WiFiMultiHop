package it.unibo.mobile.d2dchat.device;

import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

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
    public Semaphore lock;
    public volatile boolean waitForClient = false;
    public volatile boolean doneSending = false;

    public MessageGenerator(DeviceManager deviceManager, GroupOwner groupOwner) {
        this.deviceManager = deviceManager;
        this.groupOwner = groupOwner;
        this.lock = new Semaphore(0);
        message = new Message();
        message.setType(Constants.MESSAGE_DATA);
        message.setSource(deviceManager.deviceAddress);
        message.setDest(deviceManager.currentDest);
        message.setData(new char[1024]);
        message.setSeqNum(0);
    }

    public synchronized void send(Message message) {

    }

    @Override
    public void run() {
        while (true) {
            try {
                message.setDest(deviceManager.currentDest);
                message.setSendTime(System.currentTimeMillis());
                message.incSeqNum();

                // GroupOwner could have to stop sending messages at any time.
                // When the GO receives the message telling it to stop sending, if MessageGenerator is not doneSending the GroupOwner waits
                // until it is done sending. It also sets waitForClient to true, so that the MessageGenerator knows that it has to wait for
                // the client return before sending again.
                doneSending = false;
                if (!waitForClient) {
                    groupOwner.messageManager.write(message);
                    groupOwner.sent++;
                }
                doneSending = true;
                groupOwner.lock.release();
                if (waitForClient)
                    lock.acquire();
                else // sleep only if we have not already waited with lock.acquire()
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
    private long totalTimeReceivedMessages = 0;
    private int received = 0;
    private int completedConnections = 0;
    public volatile int sent = 0;
    public Semaphore lock;
    private int count = 0;

    public GroupOwner(DeviceManager deviceManager) {
        super();
        this.deviceManager = deviceManager;
        this.lock = new Semaphore(0);
    }

    // Used to unlock MessageGenerator

    @Override
    public void onConnect() {
        count++;
        Log.d(TAG, "onConnect() called "+Integer.toString(count)+" times.");
        try {
            newSocket();
            // every time a new wifi connection is established we need to create a new socket
            Socket client = socket.accept();
            // stop old instance because it's using an old socket
            if (messageManager != null)
                messageManager.keepRunning = false;
            messageManager = new MessageManager(client, this);
            messageManager.start();
        } catch (IOException e) {
            Log.e(TAG, "Could not create socket.");
            try {
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException ioe) {

            }
            e.printStackTrace();
        }
        Log.d(TAG, "onConnect() created new connection");
        if (generator == null) { // lazy instantiation of MessageGenerator (thread)
            generator = new MessageGenerator(deviceManager, this);
            generator.start();
        }
        generator.waitForClient = false;
        generator.lock.release();
    }

    @Override
    public void onDisconnect() {
        Log.d(TAG, "onDisconnect()");
        messageManager.keepRunning = false;
        completedConnections++;
    }

    MessageManager getMessageManager() {
        return messageManager;
    }

    @Override
    public void receiveMessage(Message message) {
        if (message.getType() == Constants.MESSAGE_DATA) {
            //Message receiver is the owner
            if (message.getDest().equals(deviceManager.getDeviceName())) {
                // Discard message and record its arrival.
                received++;
                long totalTime = System.currentTimeMillis() - message.getSendTime();
                totalTimeReceivedMessages += totalTime;
                Log.d(TAG, "Received msg " + message.getSeqNum() + " after " + (float) totalTime / 1000 + " seconds.");
            }
        }
        else if (message.getType() == Constants.MESSAGE_STOP) {
            initiateDisconnection();
        }
    }

    public void newSocket() {
        try {
            socket = new ServerSocket();
            socket.setReuseAddress(true);
            try {
                sleep(1000);
            } catch (InterruptedException e) {}
            socket.bind(new InetSocketAddress(Constants.SERVER_PORT));
            Log.d("GroupOwnerSocketHandler", "Socket Started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initiateDisconnection() {
        Log.d(TAG, "Departure procedure initiated.");
        // We have to stop sending messages, because the client is leaving.
        while (!generator.doneSending)
            try {
                lock.acquire();
            } catch (InterruptedException e) {
                Log.d(TAG, "Interrupted exception while waiting for generator to finish sending.");
        }
        // Generate response to client.
        Message message = new Message();
        message.setType(Constants.MESSAGE_STOP_ACK);
        message.setSource(deviceManager.deviceAddress);
        message.setDest(deviceManager.currentDest); // this is the other GO's address but this message is intended for the client
        message.setSeqNum(0);
        messageManager.write(message);
    }
}
