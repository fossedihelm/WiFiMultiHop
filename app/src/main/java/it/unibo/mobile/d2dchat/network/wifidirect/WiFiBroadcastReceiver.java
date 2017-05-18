package it.unibo.mobile.d2dchat.network.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
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

public class WiFiBroadcastReceiver extends BroadcastReceiver {

    private WifiManager mManager;
    private Channel mChannel;
    private DeviceManager deviceManager;

    public WiFiBroadcastReceiver(WifiManager manager, Channel channel,
                                       DeviceManager deviceManager) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.deviceManager = deviceManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            deviceManager.wifiState(state == WifiManager.WIFI_STATE_ENABLED);
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                Log.d(TAG, "WiFi status: connected");
            } else {
                Log.d(TAG, "WiFi status: disconnected");
            }
            deviceManager.onConnectionInfoAvailable(networkInfo);
        }
    }
}
