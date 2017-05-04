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

import it.unibo.mobile.d2dchat.device.Peer;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public abstract class MessageManager extends Thread {

    protected Socket socket = null;
    protected String deviceName;
    protected Peer peer;
    protected Sender sender = null;
    protected InputStream inputStream = null;
    protected OutputStream outputStream = null;
    protected static final String TAG = "MessageManager";
    protected ArrayList<Message> outputQueue = null;
    public volatile boolean keepRunning = true;

    protected class InverseSemaphore extends Semaphore {
        public InverseSemaphore(int permits) {
            super(permits);
        }

        public void takePermit() {
            reducePermits(1);
        }
    }

    protected class Sender extends Thread {
        public Semaphore queue = null;
        public InverseSemaphore emptyQueue = null;

        public Sender() {
            queue = new Semaphore(0);
            emptyQueue = new InverseSemaphore(1);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    queue.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                // Send message
                Message message = outputQueue.get(0);
                outputQueue.remove(0);
                Log.i(TAG, "Sending message: \n" + message.getContents());
                try {
                    if (outputStream == null)
                        Log.d(TAG, "null outputStream");
                    new ObjectOutputStream(outputStream).writeObject(message);
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }
                emptyQueue.release();
            }
        }

    }

    public void send(Message message) {
        // lazy instantiation of sender thread
        if (sender == null) {
            sender = new Sender();
            sender.start();
        }
        outputQueue.add(message);
        sender.emptyQueue.takePermit();
        sender.queue.release();
    }

    public MessageManager(Peer peer) {
        this.peer = peer;
        this.outputQueue = new ArrayList<>(50);
    }

    public void stopManager() {
        keepRunning = false;
        sender.emptyQueue.tryAcquire();
        closeSocket();
    }

    private void closeSocket(){
        Log.d(TAG, "Close socket request received");
        try {
            socket.close();
            Log.d(TAG, "Close socket request executed");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
