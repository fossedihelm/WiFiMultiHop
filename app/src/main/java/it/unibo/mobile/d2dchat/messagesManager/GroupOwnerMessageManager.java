package it.unibo.mobile.d2dchat.messagesManager;

import android.util.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Semaphore;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.Peer;

/**
 * Created by asig on 4/27/17.
 */

public class GroupOwnerMessageManager extends MessageManager {
    protected static final String TAG = "GOMessageManager";
    private ServerSocket serverSocket;
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
//            message.setGoList(new char[1024]);
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
                    if (!stopGenerating) {
                        send(message);
                        sent++;
                        peer.getDeviceManager().infoMessage.setTotalSentMessage(sent);
                        peer.getDeviceManager().infoMessage.setPartialSentMessage(message.getSeqNum());
                        peer.getDeviceManager().infoMessage.notifyChange();
                    }
                    doneSending = true;
                    if (stopGenerating) {
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
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            Log.d(TAG, "Server Socket started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startGenerating() {
        stopGenerating = false;
        generator = new MessageGenerator();
        generator.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                keepRunning = true;
                socket = serverSocket.accept();
                Log.d(TAG, "Server Socket accepted");
            } catch (IOException e) {
                if (socket != null && !socket.isClosed())
                    stopManager(false);
                e.printStackTrace();
            }

            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            receive(false);
        }
    }

}
