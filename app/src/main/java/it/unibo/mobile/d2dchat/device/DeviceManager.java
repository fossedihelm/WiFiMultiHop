package it.unibo.mobile.d2dchat.device;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.unibo.mobile.d2dchat.Constants;
import it.unibo.mobile.d2dchat.MainActivity;
import it.unibo.mobile.d2dchat.messagesManager.FileAttach;
import it.unibo.mobile.d2dchat.messagesManager.Message;
import it.unibo.mobile.d2dchat.network.wifidirect.WiFiDirectBroadcastReceiver;
import it.unibo.mobile.d2dchat.socketManager.SocketHandler;
import it.unibo.mobile.d2dchat.socketManager.SocketReceiver;

//Deve gestire il device,  dovrà fare da intermediario tra la rete e l'activity. Gran parte del codice dell'activity andrà qui
public class DeviceManager implements PeerListListener, ConnectionInfoListener, GroupInfoListener, SocketReceiver {

    private int deviceStatus = Constants.DEVICE_INIT;
    private WifiP2pManager wifiP2pManager;
    private Channel channel;
    private WiFiDirectBroadcastReceiver wiFiDirectBroadcastReceiver;
    private MainActivity mainActivity;
    private String deviceName;
    private String groupOwner;
    private List<WifiP2pDevice> peers;
    public String deviceAddress;
    public boolean isGO;
    public ArrayList<WifiP2pDevice> GOlist;
    public boolean firstDiscovery = true;
    public boolean switching = false;
    public String currentDest = null;
    public int currentGO = 0;
    public int timeInterval = 10000;
    private Timer timer = new Timer();
    public Peer peer;

    private static final String TAG = "DeviceManager";

    public WiFiDirectBroadcastReceiver getWiFiDirectBroadcastReceiver() {
        return wiFiDirectBroadcastReceiver;
    }

    // peers getter and setter
    public List<WifiP2pDevice> getPeers() {
        return peers;
    }

    public void setPeers(List<WifiP2pDevice> peers) {
        this.peers = peers;
    }

    public class ActionListenerDiscoverPeers implements ActionListener {
        //La discovery dei peers ha avuto successo, tecnicamente non serve a niente perchè ci avvertirà la callback onPeersAvailable
        @Override
        public void onSuccess() {
            Log.i(TAG, "Discovery peer con successo");
            deviceStatus = Constants.DEVICE_DISCOVERY;
        }

        //La discovery dei peers non ha avuto successo, che facciamo?
        @Override
        public void onFailure(int i) {
            Log.e(TAG, "Errore nella discovery peer, codice: " + i);
        }
    }

    public DeviceManager(WifiP2pManager wifiP2pManager, Channel channel, MainActivity mainActivity) {

        this.channel = channel;
        this.wifiP2pManager = wifiP2pManager;
        this.mainActivity = mainActivity;
        this.peers = new ArrayList<WifiP2pDevice>();

        wiFiDirectBroadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);

        Log.d(TAG, "Costruttore devicemanager");
        wifiP2pManager.discoverPeers(channel, new ActionListenerDiscoverPeers());
    }

    // Called on activity destruction
    public void stop() {
        deviceStatus = Constants.DEVICE_INIT;
        //peer.nextAction.setAction(Peer.Action.disconnect);
        //peer.semaphore.release();
        wifiP2pManager.removeGroup(channel, null);
    }

    //Lo stato del wifi è cambiato, active == true wifi attivo (e viceversa)
    public void wifiState(boolean active) {
        Log.d(TAG, "Stato del wifi cambiato: " + active);
        if (active) {
            switch (deviceStatus) {
                case Constants.DEVICE_INIT:
                case Constants.DEVICE_NOWIFI://non ha senso
                    wifiP2pManager.discoverPeers(channel, new ActionListenerDiscoverPeers());
                    //Supponiamo che siamo già nella schermata principale poichè i due stati del case dovrebbero già essere gestiti
                    break;
            }
        } else {
            deviceStatus = Constants.DEVICE_NOWIFI;
            wifiP2pManager.stopPeerDiscovery(channel, null); //non ci interessa la callback sulla stop discovery, forse
            //TODO: Avvisiamo la'ctivity che non c'è più connessione
        }
    }


    @Override
    //E' successo qualcosa nei vicini (nuovi vicini, vecchi vicini andati, etc..), non si parla di gruppo
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.d(TAG, "Cambiato il gruppo dei peers");
        //Ci interessa solo se siamo in discovery, così aggiorniamo la lista . Se siamo connessi starà agli altri connettersi al gruppo
        if (deviceStatus == Constants.DEVICE_DISCOVERY) {
            peers.clear();
            peers.addAll(wifiP2pDeviceList.getDeviceList());
            mainActivity.updatePeers();
            if (switching) {
                switching = false;
                switchGO();
            }
        }
    }


    @Override
    //Sono disponibili informazioni sulla connessione (siamo in un gruppo), probabilmente ci siamo connessi
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
     //   if (deviceStatus != Constants.DEVICE_CONNECTED) {
            deviceStatus = Constants.DEVICE_CONNECTED;
            boolean creation = true;
            if (peer != null)
                creation = false;
            if (wifiP2pInfo.isGroupOwner) {
                isGO = true;
                if (creation) {
                    peer = new GroupOwner(this);
                    peer.start();
                }
                else {
                    peer.info = wifiP2pInfo;
                    //perform onConnect()
                    peer.nextAction.setAction(Peer.Action.connect);
                    peer.semaphore.release();
                }
                // send data to a client in the list (the only one in our test scenario)
                for (WifiP2pDevice device : peers) {
                    if (!device.isGroupOwner())
                        currentDest = device.deviceAddress;
                }
            } else {
                if (creation) {
                    peer = new Client(this);
                    peer.info = wifiP2pInfo;
                    //perform onConnect()
                    peer.nextAction.setAction(Peer.Action.connect);
                    peer.semaphore.release();
                    peer.start();
                }
                currentDest = wifiP2pInfo.groupOwnerAddress.getHostAddress();
            }

            //TODO: verify that one thread is enough
            //((Thread) (peer)).start();

            wifiP2pManager.requestGroupInfo(channel, this);
        //}
    }

    @Override
    //Riceviamo le informazioni sul gruppo
    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
        Log.d(TAG, "Informazioni sul gruppo ricevute");
        List<WifiP2pDevice> devices = new ArrayList<>();
        devices.addAll(wifiP2pGroup.getClientList());
        //Il GO sembra che non appare nella lista dei client.
        //Naturalmente lo aggiungiamo solo se non siamo GO (non abbiamo noi stessi nella lista del gruppo)
        if (!isGO) {
            devices.add(wifiP2pGroup.getOwner());
        }
        groupOwner = wifiP2pGroup.getOwner().deviceName;
        //Gli apaprtenenti al gruppo sono cambiati, aggiorniamo
        mainActivity.setGroupPeers(devices);
    }

    //l'opposto di quello sopra, la chiamiamo quando si è registrato un cambio alla connessione ma non siamo connessi
    public void onConnectionInfoNotAvailable() {
        if (deviceStatus == Constants.DEVICE_CONNECTED) {
            //Non siamo più nel gruppo, ricominciamo la discovery
            deviceStatus = Constants.DEVICE_INIT;
            wifiP2pManager.discoverPeers(channel, new ActionListenerDiscoverPeers());
            mainActivity.updateDevice();
        }
    }

    //E' cambiato lo stato del nostro device
    public void updateDevice(WifiP2pDevice device) {

        //Aggiorniamo o, se è la prima volta, salviamo il nome del device
        if (deviceName == null || !deviceName.equals(device.deviceName))
            deviceName = device.deviceName;
            deviceAddress = device.deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getDeviceStatus() {
        return deviceStatus;
    }

    @Override
    //Il socket handler ci comunica l'arrivo di un evento, in genere messaggi
    public void receiveMessage(int eventType, Message message) {
        switch (eventType) {
            case Constants.EVENT_REGISTER: //Dobbiamo registrarci al GO
                if (message == null)
                    registerMessage(); //Dobbiamo registrarci
                else
                    mainActivity.addParticipant(message.getSource());
                break;
            case Constants.EVENT_MESSAGE:
                receiveChatMessage((Message) message);
                break;
        }
    }

    //Ci registriamo al GO. In questo modo il GO associa il nostro nome al ChatManager da cui riceve il messaggio.
    //(Il GO ha un ChatManager per ogni client).
    private void registerMessage() {
        Message message = new Message();
        message.setSource(deviceName);
        message.setType(Constants.MESSAGE_REGISTER);
        message.setDest(groupOwner);
        peer.writeMessage(message);
    }

    private void receiveChatMessage(Message message) {
        mainActivity.addMessage(message);
    }

    public void sendDataMessage(String dest) {
        Message message = new Message();
        message.setSource(deviceName);
        message.setType(Constants.MESSAGE_DATA);
        message.setDest(dest);
        message.setData(new char[1024]);
        message.setSendTime(System.currentTimeMillis());
    }

    public void sendTextMessage(String text, String receiver) {
        Message message = new Message();
        message.setSource(deviceName);
        message.setType(Constants.MESSAGE_TEXT);
        message.setDest(receiver);
        message.setData(text);
        sendMessage(message);
    }

    public void sendFile(Uri filePath, String receiver) {
        Message message = new Message();
        message.setSource(deviceName);
        message.setType(Constants.MESSAGE_FILE);
        message.setDest(receiver);
        byte[] bfile = getBytes(filePath);
        if (bfile != null) {
            message.setData(new FileAttach(bfile, getFilenameFromUri(filePath)));
            sendMessage(message);
        }
    }


    public String getFilenameFromUri(Uri uri) {
        String fileName = null;
        Context context = mainActivity.getApplicationContext();
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            fileName = uri.getLastPathSegment();
        } else if (scheme.equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                int columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE);
                cursor.moveToFirst();
                fileName = cursor.getString(columnIndex);
            }
        }
        return fileName;
    }


    private byte[] getBytes(Uri filePath) {
        InputStream fileInputStream = null;
        try {
            fileInputStream = mainActivity.getContentResolver().openInputStream(filePath);
            byte[] bytes = new byte[fileInputStream.available()];
            fileInputStream.read(bytes);
            fileInputStream.close();
            return bytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private void sendMessage(Message message) {
        mainActivity.addMessage(message);
        peer.writeMessage(message);
    }


    public void connectTo(final WifiP2pDevice device) {
        Log.d(TAG, "Ci proviamo a connettere ad un device");
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "["+device.deviceName+"]Connessione riuscita");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "["+device.deviceName+"]Connessione non riuscita, codice: " + reason);
            }
        });
    }

    public void createGroup (){
        Log.d(TAG, "Mi dichiaro GO");

        wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Creazione riuscita");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Creazione non riuscita, codice: " + reason);
            }
        });
    }

    public void startPingPongProcedure (){
        Log.d(TAG, "Fase di ping pong iniziata");
        GOlist = new ArrayList<WifiP2pDevice>();
        for (WifiP2pDevice peer: peers) {
            if(peer.isGroupOwner() && (peer.deviceName.equals("Nexus4") || peer.deviceName.equals("Elephone") || peer.deviceName.equals("N5") || peer.deviceName.equals("Mi4C") )) {
                GOlist.add(peer);
            }
        }
        switchGO();
    }

    public void switchGO() {
        if(deviceStatus==Constants.DEVICE_CONNECTED) {
            ((Client)peer).keepSending = false; // stop sending queued messages
            peer.nextAction.setAction(Peer.Action.initiateDisconnection); // message exchange to stop GO from sending
        }
        else {
            currentGO = (currentGO + 1) % GOlist.size();
            Log.d(TAG, "switch verso: " + GOlist.get(currentGO).deviceName);
            connectTo(GOlist.get(currentGO));
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    switchGO();
                }
            }, timeInterval);
        }
    }

    public void discover() {
        wifiP2pManager.discoverPeers(channel, new ActionListenerDiscoverPeers());
    }

    public void disconnect() {
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess(){
                deviceStatus = Constants.DEVICE_DISCONNECTED;
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Disconnect non riuscita, codice: " + reason);
            }
        });
    }
}
