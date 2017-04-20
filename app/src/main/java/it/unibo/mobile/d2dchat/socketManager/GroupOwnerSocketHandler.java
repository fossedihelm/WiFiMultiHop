package it.unibo.mobile.d2dchat.socketManager;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.DeviceManager;
import it.unibo.mobile.d2dchat.messagesManager.MessageManager;
import it.unibo.mobile.d2dchat.messagesManager.Message;


/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */

/*
public class GroupOwnerSocketHandler extends Thread implements IReceiver, SocketHandler {

    ServerSocket socket = null;
    private final int THREAD_COUNT = 10;
    private static final String TAG = "GroupOwnerSocketHandler";
    private int received = 0;
    public int sent = 0;
    private String deviceName;
    public Map<String, MessageManager> devices = new HashMap<>();
    SocketReceiver handler;


    // A ThreadPool for client sockets.
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    public GroupOwnerSocketHandler(DeviceManager handler) {
        Log.d(TAG, "Group owner creato");
        this.deviceName = handler.getDeviceName();
        this.handler = handler;
        try {
            socket = new ServerSocket(Constants.SERVER_PORT);
            Log.d("GroupOwnerSocketHandler", "Socket Started");
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdownNow();
        }
    }

    public void newSocket() {
        try {
            socket = new ServerSocket(Constants.SERVER_PORT);
            Log.d("GroupOwnerSocketHandler", "Socket Started");
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdownNow();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                // A blocking operation. Initiate a ChatManager instance when
                // there is a new connection
                Socket client = socket.accept();
                MessageManager chatManagerClient = new MessageManager(client, this);
                pool.execute(chatManagerClient);
                Log.d(TAG, "Launching the I/O handler");
            } catch (IOException e) {
                try {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (IOException ioe) {

                }
                e.printStackTrace();
                pool.shutdownNow();
                break;
            }
        }
    }


    public void notifyClient(String deviceName, MessageManager manager) {
        devices.put(deviceName, manager);
    }

    public void receiveMessage(Message message) {
        //Message receiver is the owner
        if (message.getDest().equals(deviceName)) {
            //handler.receiveMessage(Constants.EVENT_MESSAGE, message);
            // Discard message and record its arrival.
            received++;
            long totalTime = System.currentTimeMillis() - message.getSendTime();
            Log.d(TAG, "Received msg "+message.getSeqNum()+" after "+(float)totalTime/1000+" seconds.");
        }
            //It's a group message, all the participants should receive the message
        else if (message.getDest().equals(Constants.GROUP_MESSAGE)) {
            handler.receiveMessage(Constants.EVENT_MESSAGE, message);
            for (Map.Entry<String, MessageManager> d : devices.entrySet()) {
                if (!d.getKey().equals(message.getSource()))
                    d.getValue().write(message);
            }
        } else //It's a private message, let's redirect it to the right receiver
            devices.get(message.getDest()).write(message);
    }

    @Override
    public void receiveMessage(Message message, MessageManager manager) {
        switch (message.getType()) {
            case Constants.MESSAGE_REGISTER:
                notifyClient(message.getSource(), manager);
                handler.receiveMessage(Constants.EVENT_REGISTER, message);
                notifyOthers(message);
                break;
            case Constants.MESSAGE_TEXT:
            case Constants.MESSAGE_DATA:
                receiveMessage(message);
                break;

        }
    }

    private void notifyOthers(Message message) {

        for (Map.Entry<String, MessageManager> d : devices.entrySet()) {
            if (!d.getKey().equals(message.getSource())) {
                d.getValue().write(message);  //Notifichiamo agli altri che si è registrato un nuovo utente
                Message msg = prepareRegisterMessage(d.getKey()); //Notifichiamo al nuovo utente chi è presente in chat
                devices.get(message.getSource()).write(msg);
            }


        }
    }

    private Message prepareRegisterMessage(String sender) {
        Message result = new Message();
        result.setSource(sender);
        result.setType(Constants.MESSAGE_REGISTER);
        return result;
    }

    @Override
    public void writeMessage(Message message) {
        if (message.getDest().equals(Constants.GROUP_MESSAGE)) {
            for (Map.Entry<String, MessageManager> d : devices.entrySet()) {
                d.getValue().write(message);
            }
        } else
            devices.get(message.getDest()).write(message);
    }

    @Override
    public void stopHandler() {
        pool.shutdownNow();
        try {
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

*/