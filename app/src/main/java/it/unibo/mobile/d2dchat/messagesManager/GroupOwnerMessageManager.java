package it.unibo.mobile.d2dchat.messagesManager;

import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.DeviceManager;
import it.unibo.mobile.d2dchat.device.GroupOwner;
import it.unibo.mobile.d2dchat.device.Peer;

/**
 * Created by asig on 4/27/17.
 */

public class GroupOwnerMessageManager extends MessageManager {
    protected static final String TAG = "GOMessageManager";
    public int sent = 0;
    public MessageGenerator generator;
    public volatile Semaphore sending = new Semaphore(0);
    public volatile boolean doneSending = false;
    public volatile boolean stopGenerating = false;

    // Thread that generates and sends messages.
    class MessageGenerator extends Thread {
        private Message message;

        public MessageGenerator() {
            message = new Message();
            message.setType(Constants.MESSAGE_DATA);
            message.setSource(peer.getDeviceManager().deviceAddress);
            message.setDest(peer.getDeviceManager().currentDest);
            message.setData(new char[1024]);
            message.setSeqNum(0);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    message.setDest(peer.getDeviceManager().currentDest);
                    message.setSendTime(System.currentTimeMillis());
                    message.incSeqNum();

                    // GroupOwner could have to stop sending messages at any time.
                    // When the GO receives the message telling it to stop sending, if MessageGenerator is not doneSending the GroupOwner waits
                    // until it is done sending. It also sets waitForClient to true, so that the MessageGenerator knows that it has to wait for
                    // the client return before sending again.
                    doneSending = false;
                    Log.d(TAG, "StopGenerating status: " + stopGenerating);
                    if (!stopGenerating) {
                        send(message, Constants.CLIENT_PORT);
                        sent++;
                    }
                    doneSending = true;
                    if (stopGenerating) {
                        Log.d(TAG, "StopGenerating status is true");
                        stopGenerating = false;
                        sending.release();
                        break;
                    }
                    else // sleep only if we have not already waited with lock.acquire()
                        sleep(100);
                } catch (InterruptedException e) {
                    Log.d(TAG, "sleep() interrupted, ignoring");
                }
            }
        }
    }

    public GroupOwnerMessageManager(Peer peer) {
        super(peer);
        try {
            Log.d(TAG, "Creating socket with address " + peer.getDeviceManager().getInfo().groupOwnerAddress.getHostAddress());
            socket = new DatagramSocket(Constants.SERVER_PORT);
            Log.d(TAG, "Server Socket started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
    }

    public void startGenerating() {
        generator = new MessageGenerator();
        generator.start();
    }
}
