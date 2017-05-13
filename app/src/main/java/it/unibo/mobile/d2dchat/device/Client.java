package it.unibo.mobile.d2dchat.device;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.messagesManager.ClientMessageManager;
import it.unibo.mobile.d2dchat.messagesManager.Message;

/**
 * Created by asig on 1/3/17.
 */

public class Client extends Peer {
    private DeviceManager deviceManager;
    private ClientMessageManager manager;
    private Map<String, ArrayList<Message>> goQueues = new HashMap<>();
    private int discarded = 0;
    private int totalSent = 0;
    private static final String TAG = "Client";
    private int count = 0;
    private int runNum = 0;
    private int reconnections = 0;
    private long sumAllDisconnectionsTime = 0;
    private long lastDisconnectedTime = 0;
    private boolean firstConnect = true;
    private ExecutorService pool = Executors.newSingleThreadExecutor();
    public volatile boolean keepSending = true;

    public Client(DeviceManager deviceManager) {
        super(deviceManager);
        deviceManager.infoMessage.toPrint = "RunNum;TTR;\n";
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
            deviceManager.scheduleSwitchGO();
            if (firstConnect) {
                firstConnect = false;
            }
            else {
                if(reconnections % deviceManager.GOlist.size()==0)
                    runNum++ ;
                reconnections++;
                long reconnectionTime = System.currentTimeMillis() - lastDisconnectedTime;
                sumAllDisconnectionsTime += reconnectionTime;
                double averageReconnectionTime = (double) sumAllDisconnectionsTime / reconnections;
                getDeviceManager().infoMessage.toPrint += runNum + ";" + reconnectionTime + ";\n";
                getDeviceManager().infoMessage.setAverageReconnectionTime(averageReconnectionTime);
                getDeviceManager().infoMessage.notifyChange();
            }
            manager = new ClientMessageManager(this);
            //manager.start();
            pool.execute(manager);
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
        manager.stopManager(true);
        partReceived = 0;
        discarded += goQueues.get(deviceManager.getGroupOwnerMacAddress()).size();
        goQueues.get(deviceManager.getGroupOwnerMacAddress()).clear();
        lastDisconnectedTime = System.currentTimeMillis();
        deviceManager.disconnect();
    }

    @Override
    public void receiveMessage(Message message) {
        if (message.getType() == Constants.MESSAGE_DATA) {
            totalReceived++;
            partReceived++;
            goQueues.get(message.getDest()).add(message);
            getDeviceManager().infoMessage.setTotalRecvMessage(totalReceived);
            getDeviceManager().infoMessage.setPartialRecvMessage(partReceived);
            getDeviceManager().infoMessage.notifyChange();
        }
        else if (message.getType() == Constants.MESSAGE_STOP_ACK) {
            Log.i(TAG, "Received message: \n" + message.getContents());
            onDisconnect();
            deviceManager.disconnect();
        }
    }

    public void sendQueued() {
        ArrayList<Message> currentQueue = goQueues.get(deviceManager.getGroupOwnerMacAddress());
        int sentCnt = 0;
        while (!currentQueue.isEmpty() && keepSending) {
            sentCnt++;
            totalSent++;
            Message message = currentQueue.get(0);
            currentQueue.remove(0);
            manager.send(message);
            getDeviceManager().infoMessage.setPartialSentMessage(sentCnt);
            getDeviceManager().infoMessage.setTotalSentMessage(totalSent);
            getDeviceManager().infoMessage.notifyChange();
        }
        Log.d(TAG, "Sent all queued messages.");
    }

    public void initiateDisconnection() {
        Log.d(TAG, "Departure procedure initiated.");
        Message message = new Message();
        message.setType(Constants.MESSAGE_STOP);
        message.setSource(deviceManager.deviceAddress);
        message.setDest(deviceManager.getGroupOwnerMacAddress());
        message.setSeqNum(0);
        manager.send(message);
        nextAction.setAction(Action.wait);
    }
}
