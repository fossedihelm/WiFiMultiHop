package it.unibo.mobile.d2dchat.messagesManager;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
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
    private ServerSocket serverSocket;
    public int sent = 0;
    public MessageGenerator generator;
    public volatile Semaphore sending = new Semaphore(0);
    public volatile boolean doneSending = false;
    public volatile boolean stopGenerating = false;

    // Thread that generates and sends messages.
    class MessageGenerator extends Thread {
        private DeviceManager deviceManager;
        private Message message;
        public Semaphore lock;

        public MessageGenerator() {
            this.lock = new Semaphore(0);
            message = new Message();
            message.setType(Constants.MESSAGE_DATA);
            message.setSource(deviceManager.deviceAddress);
            message.setDest(deviceManager.currentDest);
            message.setData(new char[1024]);
            message.setSeqNum(0);
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
                    if (!stopGenerating) {
                        send(message);
                        sent++;
                    }
                    doneSending = true;
                    if (stopGenerating) {
                        sending.release();
                        break;
                    }
                    else // sleep only if we have not already waited with lock.acquire()
                        sleep(100);
                } catch (InterruptedException e) {
                    Log.d("MessageGenerator", "sleep() interrupted, ignoring");
                }
            }
        }
    }

    public GroupOwnerMessageManager(Peer peer) {
        super(peer);
        generator = new MessageGenerator();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            Log.d("GroupOwnerSocketHandler", "Socket started");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket = serverSocket.accept();
        } catch (IOException e) {
            try {
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException ioe) {

            }
            e.printStackTrace();
        }

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (keepRunning) {
            try {
                Message message = (Message) new ObjectInputStream(inputStream).readObject();
                receiver.receiveMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                if (socket != null && !socket.isClosed())
                    try {
                        socket.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                break;
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Read error: ", e);
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void stopManager() {
        super.stopManager();
        try {
            serverSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
