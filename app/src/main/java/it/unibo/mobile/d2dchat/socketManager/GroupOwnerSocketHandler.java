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
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.DeviceManager;
import it.unibo.mobile.d2dchat.device.GroupOwner;
import it.unibo.mobile.d2dchat.messagesManager.MessageManager;
import it.unibo.mobile.d2dchat.messagesManager.Message;


/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */
/*

public class GroupOwnerSocketHandler extends Thread implements SocketHandler {

    ServerSocket socket = null;
    private final int THREAD_COUNT = 2;
    private static final String TAG = "GroupOwnerSocketHandler";
    private GroupOwner groupOwner = null;
    private MessageManager messageManager = null;
    public volatile boolean keepRunning = true;
    public volatile Semaphore lock = new Semaphore(0);


    // A ThreadPool for client sockets.
    public final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    public GroupOwnerSocketHandler(GroupOwner groupOwner) {
        this.groupOwner = groupOwner;
        try {
            socket = new ServerSocket(Constants.SERVER_PORT);
            Log.d("GroupOwnerSocketHandler", "Socket started");
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
        while (keepRunning) {
            try {
                // A blocking operation. Initiate a ChatManager instance when
                // there is a new connection
                Socket client = socket.accept();
                messageManager = new MessageManager(client, groupOwner);
                pool.execute(messageManager);
                Log.d(TAG, "Launching the I/O handler");
                boolean finished = false;
                while (!finished) {
                    try {
                        lock.acquire();
                        finished = true;
                    } catch (InterruptedException e) {}
                }
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

    @Override
    public void stopHandler() {
        while (!messageManager.doneWriting);
        pool.shutdownNow();
        boolean done = false;
        while (!done) {
            try {
                pool.awaitTermination(2000, TimeUnit.MILLISECONDS);
                done = true;
            } catch (InterruptedException e) {}
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        keepRunning = false;
        lock.release();
    }

}
*/
