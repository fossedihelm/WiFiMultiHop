package it.unibo.mobile.d2dchat.messagesManager;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.Peer;

/**
 * Created by ghosty on 27/04/17.
 */

public class ClientMessageManager extends MessageManager {
    protected static final String TAG = "ClientMessageManager";
    public volatile Semaphore connecting = new Semaphore(0);

    public ClientMessageManager(Peer peer) {
        super(peer);
    }

    @Override
    public void run(){
        // every time a new wifi connection is established we need to create a new socket
        socket = new Socket();
        try {
            socket.bind(null);
            int c =0;
            try {
                Log.d(TAG, Integer.toString(++c) + "o tentativo di connessione");
                sleep(1500);
                socket.connect(new InetSocketAddress(peer.getDeviceManager().getInfo().groupOwnerAddress.getHostAddress(),
                        Constants.SERVER_PORT), 5000);
                connecting.release();
            } catch (ConnectException e){
                Log.e(TAG, "CONNECTIONEXP nel " + Integer.toString(c) + "o tentativo di connessione");
                e.printStackTrace();
            } catch (IOException e){
                Log.e(TAG, "IOEXP nel " + Integer.toString(c) + "o tentativo di connessione");
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            //La connessione non è stata posssibile! Forse non sta usando la nostra applicazione?
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
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
                peer.receiveMessage(message);
            } catch (IOException e) {
                Log.d(TAG, "Error reading object");
                e.printStackTrace();
                if (socket != null && !socket.isClosed())
                    super.stopManager();
                break;
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Read error: ", e);
                if (socket != null && !socket.isClosed()) {
                    super.stopManager();
                }
                break;
            }
        }
    }
}
