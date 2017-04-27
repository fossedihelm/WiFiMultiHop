package it.unibo.mobile.d2dchat.messagesManager;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import it.unibo.mobile.d2dchat.device.Peer;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public abstract class MessageManager extends Thread {

    protected Socket socket = null;
    protected String deviceName;
    protected Peer peer;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected static final String TAG = "MessageManager";
    public volatile boolean keepRunning = true;

    public MessageManager(Peer peer) {
        this.peer = peer;
    }

    public MessageManager(Socket socket, Peer receiver) {
        this.socket = socket;
        this.peer = receiver;
    }


    public MessageManager(Socket socket, String deviceName, Peer receiver) {
        this.socket = socket;
        this.deviceName = deviceName;
        this.peer = receiver;
    }



    public void send(Message message) {
        Log.i(TAG, "Sending message: \n" + message.getContents());
        try {
            if (outputStream == null)
                Log.d(TAG, "null outputStream");
            new ObjectOutputStream(outputStream).writeObject(message);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public void stopManager() {
        keepRunning = false;
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
