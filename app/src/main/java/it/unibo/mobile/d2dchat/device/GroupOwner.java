package it.unibo.mobile.d2dchat.device;

import android.util.Log;
import java.util.ArrayList;
import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.messagesManager.GroupOwnerMessageManager;
import it.unibo.mobile.d2dchat.messagesManager.Message;

/**
 * Created by asig on 12/9/16.
 */



public class GroupOwner extends Peer {
    private static final String TAG = "GroupOwner";
    private GroupOwnerMessageManager manager;
    private long sumAllRTT = 0;
    private int completedConnections = 0;
    private int runNum = 0;
    private Role role = Role.generator;
    public volatile int sent = 0;
    public enum Role {generator, replier};

    public void setRole(Role role) {
        this.role = role;
    }

    public GroupOwner(DeviceManager deviceManager) {
        super(deviceManager);
        deviceManager.infoMessage.toPrint = "RunNum;MessNum;RTT;\n";
        manager = new GroupOwnerMessageManager(this);
        manager.start();
    }

    @Override
    public void onConnect() {
    }

    @Override
    public void onDisconnect() {
        runNum++;
        getDeviceManager().infoMessage.setRunNumber(runNum);
        getDeviceManager().infoMessage.notifyChange();
        Log.d(TAG, "onDisconnect()");
        sent += manager.sent;
        partReceived = 0;
        manager.stopManager(false);
        completedConnections++;
    }

    @Override
    public void receiveMessage(Message message) {
        if (message.getType() == Constants.MESSAGE_DATA) {
            //Message receiver is the owner
            if (message.getDest().equals(deviceManager.deviceAddress)) {
                totalReceived++;
                partReceived++;
                if (role == Role.generator) {
                    // Record message arrival.
                    long RTT = System.currentTimeMillis() - message.getSendTime();
                    sumAllRTT += RTT;
                    double averageRTT = (double) sumAllRTT / totalReceived;
                    getDeviceManager().infoMessage.setAverageRTT(averageRTT);
                    getDeviceManager().infoMessage.toPrint += (runNum + ";" +message.getSeqNum() + ";" + RTT + ";\n");
                    Log.d(TAG, "Received msg " + message.getSeqNum() + " after " + (float) RTT / 1000 + " seconds.");
                }
                else if (role == Role.replier) {
                    // Reply to message.
                    message.setDest(deviceManager.currentDest);
                    message.setSource(deviceManager.deviceAddress);
                    manager.send(message);
                    getDeviceManager().infoMessage.setTotalSentMessage(totalReceived);
                    getDeviceManager().infoMessage.setPartialSentMessage(message.getSeqNum());
                }
                getDeviceManager().infoMessage.setTotalRecvMessage(totalReceived);
                getDeviceManager().infoMessage.setPartialRecvMessage(partReceived);
                getDeviceManager().infoMessage.notifyChange();

            }
        }
        else if (message.getType() == Constants.MESSAGE_STOP) {
            Log.i(TAG, "Received message: \n" + message.getContents());
            initiateDisconnection();
        }
        else if (message.getType() == Constants.MESSAGE_REGISTER){
            Log.i(TAG, "Received message: \n" + message.getContents());
            ArrayList<String> addresses =(ArrayList<String>) message.getGoList();
            int myIndex = addresses.indexOf(deviceManager.deviceAddress);

            getDeviceManager().infoMessage.fileName = Integer.toString(message.getSwitchTime() / 1000);
            if(addresses.size() <= 1 || myIndex == -1)
                deviceManager.currentDest = addresses.get(myIndex);
            else
                deviceManager.currentDest = addresses.get( (myIndex+1) % addresses.size() );
            if (role == Role.generator)
                manager.startGenerating();
        }
    }

    public void initiateDisconnection() {
        Log.d(TAG, "Departure procedure initiated.");
        if (role == Role.generator) {
            // We have to stop sending messages, because the client is leaving.
            manager.stopGenerating = true;
            while (!manager.doneSending)
                try {
                    manager.sending.acquire();
                } catch (InterruptedException e) {
                    Log.d(TAG, "Interrupted exception while waiting to finish sending.");
                }
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
