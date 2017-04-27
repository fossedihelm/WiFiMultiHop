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
    private ServerSocket serverSocket;

    public GroupOwnerMessageManager(Peer peer) {
        super(peer);
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
