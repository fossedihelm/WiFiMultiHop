package it.unibo.mobile.d2dchat.messagesManager;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.Peer;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public abstract class MessageManager extends Thread {

    protected DatagramSocket socket;
    protected String deviceName;
    protected InetAddress remoteAddress;
    protected Peer peer;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected static final String TAG = "MessageManager";
    public volatile boolean keepRunning = true;

    public MessageManager(Peer peer) {
        this.peer = peer;
    }

    public MessageManager(Socket socket, Peer receiver) {
        this.peer = receiver;
    }


    public MessageManager(Socket socket, String deviceName, Peer receiver) {
        this.deviceName = deviceName;
        this.peer = receiver;
    }

    public void run() {
        byte[] recvBuf = new byte[2048];
        while (keepRunning) {
            try {
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                int byteCount = packet.getLength();
                ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
                ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(byteStream));
                Message message = (Message) inputStream.readObject();
                inputStream.close();
                remoteAddress = packet.getAddress();
                peer.receiveMessage(message);
            } catch (EOFException e) {
                Log.d(TAG, "Clonnection closed, stop reading.");
                keepRunning = false;
                break;
            } catch (IOException e) {
                Log.d(TAG, "Error reading object");
                e.printStackTrace();
                if (socket != null && !socket.isClosed())
                    stopManager();
                break;
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Read error: ", e);
                if (socket != null && !socket.isClosed()) {
                    stopManager();
                }
                break;
            }
        }
    }

    public void send(Message message) {
        Log.i(TAG, "Sending message: \n" + message.getContents());
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(2048);
            ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            outputStream.flush();
            outputStream.writeObject(message);
            outputStream.flush();
            // retrieves byte array
            byte[] sendBuf = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, remoteAddress, Constants.CLIENT_PORT);
            socket.send(packet);
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public void stopManager() {
        keepRunning = false;
        closeSocket();
    }

    private void closeSocket() {
        Log.d(TAG, "Close socket request received");
        socket.close();
        Log.d(TAG, "Close socket request executed");
    }
}
