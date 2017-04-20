package it.unibo.mobile.d2dchat.network.wifidirect.multiGroup;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.List;

/**
 * Created by ghosty on 09/11/16.
 */

public class MultiGroup {

    private List<WifiP2pDevice> peers;
    private WifiP2pDevice gateway;

    public List<WifiP2pDevice> getPeers() {
        return peers;
    }

    public void setPeers(List<WifiP2pDevice> peers) {
        this.peers = peers;
    }

    public void setGateway(WifiP2pDevice gateway) {
        this.gateway = gateway;
    }

    public WifiP2pDevice getGateway() {
        return gateway;
    }

    public List<WifiP2pDevice> addPeerList(List<WifiP2pDevice> peers) {
        this.peers.addAll(peers);
        return peers;
    }

    public List<WifiP2pDevice> removePeerList(List<WifiP2pDevice> peers) {
        this.peers.removeAll(peers);
        return peers;
    }
}
