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
    private int dest = 0;
    public ArrayList<Message> currentQueue = null;
    public volatile boolean keepSending = true;

    public Client(DeviceManager deviceManager) {
        super(deviceManager);
        this.deviceManager = deviceManager;
        goQueue = new ArrayList<>(deviceManager.GOlist.size());
        for (int i = 0; i < deviceManager.GOlist.size(); i++) {
            goQueue.add(new ArrayList<Message>(200));
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
            keepSending = true;
            dest = 1 - deviceManager.currentGO; // we assume only 2 GOs
            currentQueue = goQueue.get(dest);
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
        while (!goQueue.get(dest).isEmpty() && keepSending) {
            Message message = goQueue.get(dest).get(0);
            goQueue.get(dest).remove(0);
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
