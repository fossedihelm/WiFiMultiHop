package it.unibo.mobile.d2dchat.socketManager;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.messagesManager.ChatManager;
import it.unibo.mobile.d2dchat.messagesManager.Message;

public class ClientSocketHandler extends Thread implements IReceiver, SocketHandler {

    private static final String TAG = "ClientSocketHandler";
    private ChatManager chat;
    private InetAddress mAddress;
    private String deviceName;
    private SocketReceiver handler;
    Socket socket;


    public ClientSocketHandler(InetAddress groupOwnerAddress, String deviceName, SocketReceiver handler) {
        this.mAddress = groupOwnerAddress;
        this.deviceName = deviceName;
        this.handler = handler;
        Log.d(TAG, "Clientsocket creato");
    }

    @Override
    public void run() {
        socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                    Constants.SERVER_PORT), 5000);
            chat = new ChatManager(socket, deviceName, this);
            new Thread(chat).start();
        } catch (IOException e) {
            //La connessione non è stata posssibile! Forse non sta usando la nostra applicazione?
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    public ChatManager getChat() {
        return chat;
    }

    @Override
    public void receiveMessage(Message message, ChatManager manager) {
        switch (message.getType()) {
            case Constants.MESSAGE_TEXT:
            case Constants.MESSAGE_FILE:
                handler.receiveMessage(Constants.EVENT_MESSAGE, message);
                break;
            case Constants.MESSAGE_REGISTER:
                handler.receiveMessage(Constants.EVENT_REGISTER, message);
                break;

        }

    }

    @Override
    public void chatStarted() {
        this.handler.receiveMessage(Constants.EVENT_REGISTER, null);
    }

    public void writeMessage(Message message) {
        chat.write(message);
    }

    @Override
    public void stopHandler() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
