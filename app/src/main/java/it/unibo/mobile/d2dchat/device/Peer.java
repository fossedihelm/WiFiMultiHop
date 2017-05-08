package it.unibo.mobile.d2dchat.device;

import android.databinding.BaseObservable;
import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.util.concurrent.Semaphore;

import it.unibo.mobile.d2dchat.messagesManager.Message;
import it.unibo.mobile.d2dchat.messagesManager.MessageManager;

/**
 * Created by asig on 12/9/16.
 */

public abstract class Peer extends BaseObservable{
    //public volatile MessageManager messageManager;
    protected DeviceManager deviceManager;
    public volatile Semaphore semaphore;
    public enum Action {connect, disconnect, wait, initiateDisconnection};
    public class NextAction {
        private Action action = Action.wait;

        public synchronized Action getAction() {
            return action;
        }

        public synchronized void setAction (Action action) {
            this.action = action;
        }
    }
    public DeviceManager getDeviceManager(){
        return this.deviceManager;
    }
    public NextAction nextAction;
    public abstract void onConnect();
    public abstract void onDisconnect();
    public abstract void receiveMessage(Message message);
    public abstract void initiateDisconnection();

    public void run() {
        while (true) {
            try {
                semaphore.acquire();
            }
            catch (InterruptedException e) {
                Log.d("Peer", "acquire() was interrupted");
            }
            if (nextAction.getAction() == Action.connect)
                onConnect();
            else if (nextAction.getAction() == Action.disconnect)
                onDisconnect();
            else if (nextAction.getAction() == Action.initiateDisconnection)
                initiateDisconnection();
        }
    }

    public Peer(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
        semaphore = new Semaphore(0);
        nextAction = new NextAction();
        nextAction.setAction(Action.wait);
    }
}
