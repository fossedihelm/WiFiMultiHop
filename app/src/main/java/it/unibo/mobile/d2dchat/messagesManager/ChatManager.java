package it.unibo.mobile.d2dchat.messagesManager;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import it.unibo.mobile.d2dchat.socketManager.IReceiver;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class ChatManager implements Runnable {

    private Socket socket = null;
    private String deviceName;
    private IReceiver receiver;
    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "ChatHandler";

    public ChatManager(Socket socket, IReceiver receiver) {
        this.socket = socket;
        this.receiver = receiver;
    }


    public ChatManager(Socket socket, String deviceName, IReceiver receiver) {
        this.socket = socket;
        this.deviceName = deviceName;
        this.receiver = receiver;
    }


    @Override
    public void run() {
        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            //Qui devo inviare il primo messaggio di registrazione all'owner, con l'username
            receiver.chatStarted();
            while (true) {
                try {
                    Message message = (Message) new ObjectInputStream(iStream).readObject();
                    receiver.receiveMessage(message, this);
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    socket.close();
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "Read error: ", e);
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(Message message) {
        try {
            new ObjectOutputStream(oStream).writeObject(message);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

}
