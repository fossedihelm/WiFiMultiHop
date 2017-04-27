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
    protected Peer receiver;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected static final String TAG = "MessageManager";
    public volatile boolean keepRunning = true;

    public MessageManager(Peer receiver) {
        this.receiver = receiver;
    }

    public MessageManager(Socket socket, Peer receiver) {
        this.socket = socket;
        this.receiver = receiver;
    }


    public MessageManager(Socket socket, String deviceName, Peer receiver) {
        this.socket = socket;
        this.deviceName = deviceName;
        this.receiver = receiver;
    }


    @Override
    public void run() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            while (keepRunning) {
                try {
                    Message message = (Message) new ObjectInputStream(inputStream).readObject();
                    receiver.receiveMessage(message);
                }
                catch (EOFException e) {}
                catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    if (socket != null && !socket.isClosed())
                        socket.close();
                    break;
                }
                catch (ClassNotFoundException e) {
                    Log.e(TAG, "Read error: ", e);
                    if (socket != null && !socket.isClosed())
                        socket.close();
                    break;
                }
            }
        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(Message message) {
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

    protected void closeSocket(){
        Log.d(TAG, "Close socket request received");
        try {
            socket.close();
            Log.d(TAG, "Close socket request executed");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
