package it.unibo.mobile.d2dchat.network.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.device.DeviceManager;
import it.unibo.mobile.d2dchat.device.Peer;

import static android.content.ContentValues.TAG;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;
    private DeviceManager deviceManager;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       DeviceManager deviceManager) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.deviceManager = deviceManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            deviceManager.wifiState(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, deviceManager);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                Log.d(TAG, "WiFi status: connected");
                mManager.requestConnectionInfo(mChannel, deviceManager); //Chiama onConnectionInfoAvailable
            } else {
                Log.d(TAG, "WiFi status: disconnected");
            }
            WifiP2pGroup group = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
            if (deviceManager.peer != null && group.getClientList().isEmpty()) { // ci siamo disconnessi
                Log.d(TAG, "Disconnected at data link layer.");
            //    deviceManager.peer.nextAction.setAction(Peer.Action.disconnect);
            //    deviceManager.peer.semaphore.release();
            }
            Log.d(TAG, "Clients: "+group.getClientList().size());

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            deviceManager.updateDevice(device);
            Log.d(TAG, "device status: "+device.status);
            if (device.status == WifiP2pDevice.AVAILABLE) {
                Log.d(TAG, "Device AVAILABLE, ready to connect");
                //if (deviceManager.getDeviceStatus() == Constants.DEVICE_DISCONNECTED) {
                if (deviceManager.GOlist != null) {
                    // Disconnected from group, connect to next one.
                    deviceManager.discover();
                    deviceManager.onConnectionInfoNotAvailable();
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            if (state == WIFI_P2P_DISCOVERY_STARTED) {
                Log.d(TAG, "Discovery is active");
                if (!deviceManager.firstDiscovery && !deviceManager.isGO) {
                    //deviceManager.switchGO();
                    deviceManager.switching = true;
                }
                deviceManager.firstDiscovery = false;
            }
            if (state == WIFI_P2P_DISCOVERY_STOPPED) Log.d(TAG, "Discovery is NOT active");
        }
    }
}
