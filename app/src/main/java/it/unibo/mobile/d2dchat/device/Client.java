package it.unibo.mobile.d2dchat.device;

import android.util.Log;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.messagesManager.ClientMessageManager;
import it.unibo.mobile.d2dchat.messagesManager.Message;

/**
 * Created by asig on 1/3/17.
 */

public class Client extends Peer {
    private Socket server;
    private DeviceManager deviceManager;
    private ClientMessageManager manager;
    private Map<String, ArrayList<Message>> goQueues = new HashMap<>();
    private int discarded = 0;
    private static final String TAG = "Client";
    private int count = 0;
    public volatile boolean keepSending = true;

    public Client(DeviceManager deviceManager) {
        super(deviceManager);
        this.deviceManager = deviceManager;
        for (int i = 0; i < deviceManager.GOlist.size(); i++) {
            goQueues.put(deviceManager.GOlist.get(i).deviceAddress,new ArrayList<Message>(200));
        }
    }

    public Map<String, ArrayList<Message>> getGoQueues() {
        return goQueues;
    }

    @Override
    public void onConnect() {
        count++;
        if (count <= 1) {
            manager = new ClientMessageManager(this);
            manager.start();
            Log.d(TAG, "onConnect() created new connection");
            try {
                manager.connecting.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            keepSending = true;
            sendQueued();
        }
    }

    @Override
    public void onDisconnect() {
        count = 0;
        Log.d(TAG, "onDisconnect()");
        manager.stopManager();
        discarded += goQueues.get(deviceManager.getGroupOwnerMacAddress()).size();
        goQueues.get(deviceManager.getGroupOwnerMacAddress()).clear();
    }

    @Override
    public void receiveMessage(Message message) {
        Log.i(TAG, "Received message: \n" + message.getContents());
        if (message.getType() == Constants.MESSAGE_DATA) {
            goQueues.get(message.getDest()).add(message);
        }
        else if (message.getType() == Constants.MESSAGE_STOP_ACK) {
            Log.d(TAG, "Departure procedure completed.");
            onDisconnect();
            deviceManager.disconnect();
        }
    }

    public void sendQueued() {
        ArrayList<Message> currentQueue = (ArrayList<Message>) goQueues.get(deviceManager.getGroupOwnerMacAddress());
        while (!currentQueue.isEmpty() && keepSending) {
            Message message = currentQueue.get(0);
            currentQueue.remove(0);
            manager.send(message, Constants.SERVER_PORT);
        }
        Log.d(TAG, "Sent all queued messages.");
    }

    public void initiateDisconnection() {
        Log.d(TAG, "Departure procedure initiated.");
        Message message = new Message();
        message.setType(Constants.MESSAGE_STOP);
        message.setSource(deviceManager.deviceAddress);
        message.setDest(deviceManager.currentDest);
        message.setSeqNum(0);
        manager.send(message, Constants.SERVER_PORT);
        nextAction.setAction(Action.wait);
    }
}
