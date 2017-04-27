package it.unibo.mobile.d2dchat.messagesManager;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.Peer;

/**
 * Created by asig on 4/27/17.
 */

public class GroupOwnerMessageManager extends MessageManager {

    protected static final String TAG = "GOMessageManager";
    private ServerSocket serverSocket;

    public GroupOwnerMessageManager(Peer peer) {
        super(peer);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            Log.d(TAG, "Server Socket started");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Log.d(TAG, "Server Socket attending accept..");
            socket = serverSocket.accept();
            Log.d(TAG, "Server Socket accepted");
        } catch (IOException e) {
            if (socket != null && !socket.isClosed())
                super.closeSocket();
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
                Log.d(TAG, "Error reading object");
                e.printStackTrace();
                if (socket != null && !socket.isClosed())
                    super.closeSocket();
                break;
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Read error: ", e);
                if (socket != null && !socket.isClosed()) {
                    super.closeSocket();
                }
                break;
            }
        }
    }

    @Override
    public void stopManager() {
        Log.d(TAG, "Stop server socket request received");
        super.stopManager();
        try {
            serverSocket.close();
            Log.d(TAG, "Stop server socket request executed");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
