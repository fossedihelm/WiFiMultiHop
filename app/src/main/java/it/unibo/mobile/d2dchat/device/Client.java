package it.unibo.mobile.d2dchat.device;

import android.util.Log;

import java.net.Socket;
import java.util.ArrayList;

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
    private ArrayList<ArrayList<Message>> goQueue;
    private int discarded = 0;
    private static final String TAG = "Client";
    private int count = 0;
    public volatile boolean keepSending = false;

    public Client(DeviceManager deviceManager) {
        super(deviceManager);
        this.deviceManager = deviceManager;
        goQueue = new ArrayList<>(deviceManager.GOlist.size());
        for (int i = 0; i < deviceManager.GOlist.size(); i++) {
            goQueue.add(new ArrayList<Message>(20));
        }
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
            sendQueued();
        }
    }

    @Override
    public void onDisconnect() {
        count = 0;
        Log.d(TAG, "onDisconnect()");
        manager.stopManager();
        discarded += goQueue.get(deviceManager.currentGO).size();
        goQueue.get(deviceManager.currentGO).clear();
    }

    @Override
    public void receiveMessage(Message message) {
        if (message.getType() == Constants.MESSAGE_DATA)
            goQueue.get(deviceManager.currentGO).add(message);
        else if (message.getType() == Constants.MESSAGE_STOP_ACK) {
            Log.d(TAG, "Departure procedure completed.");
            onDisconnect();
            deviceManager.disconnect();
        }
    }

    public void sendQueued() {
        int dest;
        dest = 1 - deviceManager.currentGO; // we assume only 2 GOs
        while (!goQueue.get(dest).isEmpty() && keepSending) {
            Message message = goQueue.get(dest).get(0);
            goQueue.get(dest).remove(0);
            manager.send(message);
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
        manager.send(message);
        nextAction.setAction(Action.wait);
    }
}
