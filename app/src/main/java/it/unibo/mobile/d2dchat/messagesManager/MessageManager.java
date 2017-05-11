package it.unibo.mobile.d2dchat.messagesManager;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.Peer;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public abstract class MessageManager extends Thread {

    protected Socket socket = null;
    protected Peer peer;
    protected Sender sender = null;
    protected InputStream inputStream = null;
    protected OutputStream outputStream = null;
    protected static final String TAG = "MessageManager";
    protected Object lock = null;
    public volatile boolean keepRunning = true;

    protected class Sender extends Thread {
        protected ArrayList<Message> outputQueue = null;
        public Semaphore queue = null;
        public volatile boolean keepRunning = true;
        public volatile boolean keepSending = true;

        public Sender() {
            queue = new Semaphore(0);
            outputQueue = new ArrayList<>(50);
        }

        public synchronized void addToQueue(Message message) {
            outputQueue.add(message);
        }

        private synchronized void send() {
            // Send message
            Message message = outputQueue.get(0);
            outputQueue.remove(0);
            try {
                if (outputStream == null)
                    Log.d(TAG, "null outputStream");
                new ObjectOutputStream(outputStream).writeObject(message);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
            wakeUp();
            if (message.getType() != Constants.MESSAGE_DATA)
                Log.i(TAG, "Sent message: \n" + message.getContents());
        }

        public synchronized int getQueueSize() {
            return outputQueue.size();
        }

        @Override
        public void run() {
            while (keepRunning) {
                try {
                    queue.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (keepSending)
                    send();
            }
        }

    }

    public synchronized void wakeUp() {
        notifyAll();
    }

    public void send(Message message) {
        // lazy instantiation of sender thread
        if (sender == null || sender.keepRunning == false) {
            sender = new Sender();
            sender.start();
        }
        sender.addToQueue(message);
        sender.queue.release();
    }

    public void receive(boolean stopSender) {
        while (keepRunning) {
            try {
                Message message = (Message) new ObjectInputStream(inputStream).readObject();
                peer.receiveMessage(message);
            } catch (EOFException e) {
                Log.d(TAG, "Connection closed, stop reading.");
                keepRunning = false;
                break;
            } catch (IOException e) {
                Log.d(TAG, "The socket was probably closed. This is fine.");
                //e.printStackTrace();
                if (socket != null && !socket.isClosed())
                    stopManager(stopSender);
                break;
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Read error: ", e);
                if (socket != null && !socket.isClosed()) {
                    stopManager(stopSender);
                }
                break;
            }
        }
    }

    public MessageManager(Peer peer) {
        this.peer = peer;
        this.lock = new Object();
    }

    public synchronized void stopManager(boolean stopSender) {
        while (sender.getQueueSize() > 0) {
            try{
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (stopSender) {
            sender.keepRunning = false;
            sender.keepSending = false;
            sender.queue.release();
        }
        closeSocket();
    }

    public void closeSocket(){
        Log.d(TAG, "Close socket request received");
        try {
            if (!socket.isClosed()) {
                socket.close();
                Log.d(TAG, "Close socket request executed");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
