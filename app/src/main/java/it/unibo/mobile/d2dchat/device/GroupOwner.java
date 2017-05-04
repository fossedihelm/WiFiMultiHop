package it.unibo.mobile.d2dchat.device;

import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.messagesManager.GroupOwnerMessageManager;
import it.unibo.mobile.d2dchat.messagesManager.Message;

/**
 * Created by asig on 12/9/16.
 */




public class GroupOwner extends Peer {
    private static final String TAG = "GroupOwner";
    private GroupOwnerMessageManager manager;
    private long totalTimeReceivedMessages = 0;
    private int received = 0;
    private int completedConnections = 0;
    public volatile int sent = 0;
    private int count = 0;

    // Thread for SocketHandler
    private final ExecutorService socketHandlerExecutor = Executors.newSingleThreadExecutor();

    public GroupOwner(DeviceManager deviceManager) {
        super(deviceManager);
        manager = new GroupOwnerMessageManager(this);
        manager.start();
    }


    @Override
    public void onConnect() {
        count++;
        if (count <= 1) {
            Log.d(TAG, "onConnect() created new connection");
        }
    }

    @Override
    public void onDisconnect() {
        count = 0;
        Log.d(TAG, "onDisconnect()");
        sent += manager.sent;
        manager.stopManager();
        //socketHandlerExecutor.shutdownNow();
        completedConnections++;
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
        else if (message.getType() == Constants.MESSAGE_REGISTER){
            ArrayList<String> addresses =(ArrayList<String>) message.getData();
            int myIndex = addresses.indexOf(deviceManager.deviceAddress);
            if(addresses.size() <= 1 || myIndex == -1)
                deviceManager.currentDest = addresses.get(myIndex);
            else
                deviceManager.currentDest = addresses.get( (myIndex+1) % addresses.size() );
            manager.startGenerating();
        }
    }

    public void initiateDisconnection() {
        Log.d(TAG, "Departure procedure initiated.");
        // We have to stop sending messages, because the client is leaving.
        manager.stopGenerating = true;
        while (!manager.doneSending)
            try {
                manager.sending.acquire();
            } catch (InterruptedException e) {
                Log.d(TAG, "Interrupted exception while waiting for generator to finish sending.");
        }
        // Generate response to client.
        Message message = new Message();
        message.setType(Constants.MESSAGE_STOP_ACK);
        message.setSource(deviceManager.deviceAddress);
        message.setDest(deviceManager.currentDest); // this is the other GO's address but this message is intended for the client
        message.setSeqNum(0);
        manager.send(message);
        onDisconnect();
    }
}
