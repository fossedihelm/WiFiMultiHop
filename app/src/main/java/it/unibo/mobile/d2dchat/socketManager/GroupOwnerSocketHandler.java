package it.unibo.mobile.d2dchat.socketManager;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.messagesManager.ChatManager;
import it.unibo.mobile.d2dchat.messagesManager.Message;

/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */
public class GroupOwnerSocketHandler extends Thread implements IReceiver, SocketHandler {

    ServerSocket socket = null;
    private final int THREAD_COUNT = 10;
    private static final String TAG = "GroupOwnerSocketHandler";
    private String deviceName;
    public Map<String, ChatManager> devices = new HashMap<>();
    SocketReceiver handler;

    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    public GroupOwnerSocketHandler(String deviceName, SocketReceiver handler) {
        Log.d(TAG, "Group owner creato");
        this.deviceName = deviceName;
        this.handler = handler;
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
                ChatManager chatManagerClient = new ChatManager(client, this);
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


    public void notifyClient(String deviceName, ChatManager manager) {
        devices.put(deviceName, manager);
    }

    public void receiveMessage(Message message) {
        //Message receiver is the owner
        if (message.getReceiver().equals(deviceName))
            handler.receiveMessage(Constants.EVENT_MESSAGE, message);
            //It's a group message, all the participants should receive the message
        else if (message.getReceiver().equals(Constants.GROUP_MESSAGE)) {
            handler.receiveMessage(Constants.EVENT_MESSAGE, message);
            for (Map.Entry<String, ChatManager> d : devices.entrySet()) {
                if (!d.getKey().equals(message.getSender()))
                    d.getValue().write(message);
            }
        } else //It's a private message, let's redirect it to the right receiver
            devices.get(message.getReceiver()).write(message);
    }

    @Override
    public void chatStarted() {

    }

    @Override
    public void receiveMessage(Message message, ChatManager manager) {
        switch (message.getType()) {
            case Constants.MESSAGE_REGISTER:
                notifyClient(message.getSender(), manager);
                handler.receiveMessage(Constants.EVENT_REGISTER, message);
                notifyOthers(message);
                break;
            case Constants.MESSAGE_TEXT:
            case Constants.MESSAGE_FILE:
                receiveMessage(message);
                break;

        }
    }

    private void notifyOthers(Message message) {

        for (Map.Entry<String, ChatManager> d : devices.entrySet()) {
            if (!d.getKey().equals(message.getSender())) {
                d.getValue().write(message);  //Notifichiamo agli altri che si è registrato un nuovo utente
                Message msg = prepareRegisterMessage(d.getKey()); //Notifichiamo al nuovo utente chi è presente in chat
                devices.get(message.getSender()).write(msg);
            }


        }
    }

    private Message prepareRegisterMessage(String sender) {
        Message result = new Message();
        result.setSender(sender);
        result.setType(Constants.MESSAGE_REGISTER);
        return result;
    }

    @Override
    public void writeMessage(Message message) {
        if (message.getReceiver().equals(Constants.GROUP_MESSAGE)) {
            for (Map.Entry<String, ChatManager> d : devices.entrySet()) {
                d.getValue().write(message);
            }
        } else
            devices.get(message.getReceiver()).write(message);
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
