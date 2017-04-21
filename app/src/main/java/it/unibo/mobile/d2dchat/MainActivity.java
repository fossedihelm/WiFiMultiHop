package it.unibo.mobile.d2dchat;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.unibo.mobile.d2dchat.device.DeviceManager;
import it.unibo.mobile.d2dchat.fragment.ChatFragment;
import it.unibo.mobile.d2dchat.fragment.DevicesListFragment;
import it.unibo.mobile.d2dchat.messagesManager.Message;

public class MainActivity extends AppCompatActivity implements MyDialogFragment.UserNameListener{


    public static final String TAG = "wifiD2Dchat";

    // TXT RECORD properties
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifiD2Dchat";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public static final Integer SERVER_PORT = 5454;


    WifiP2pManager mManager;
    Channel mChannel;
    DevicesListFragment devicesListFragment;
    ChatFragment chatFragment = new ChatFragment();
    IntentFilter mIntentFilter;


    public DeviceManager deviceManager;

    //Questi oggetti verranno riempiti da fuori e letti dal main activity per fill della gui
    private List<String> groupPeers = new ArrayList();
    private List<Message> messageToProcess = new ArrayList<>();
    ///
    private Object lock = new Object();

    private boolean chatFragmentShowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        //mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        deviceManager = new DeviceManager(mManager, mChannel, this);
        //Quando clicchiamo per scrivere e si alza la tastiera, tutto i layout viene mosso con lei
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        devicesListFragment = new DevicesListFragment();
        devicesListFragment.setDeviceManager(deviceManager);
        getFragmentManager().beginTransaction()
                .add(R.id.container_root, devicesListFragment, "services").commit();


        startTimerThread();

    }

    private void startTimerThread() {
        Thread th = new Thread(new Runnable() {
            private long startTime = System.currentTimeMillis();

            public void run() {
                while (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                if (messageToProcess.size() > 0) {
                                    for (Message message : messageToProcess) {
                                        chatFragment.addMessage(message);
                                    }
                                    chatFragment.getListAdapter().notifyDataSetChanged();
                                    messageToProcess.clear();
                                }
                                chatFragment.setParticipants(groupPeers); //TODO:Il go ora non aggiorna più, evitare di farlo ogni update?
                            }
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        th.start();
    }


    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(deviceManager.getWiFiDirectBroadcastReceiver(), mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(deviceManager.getWiFiDirectBroadcastReceiver());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        deviceManager.stop();

        unregisterReceiver(deviceManager.getWiFiDirectBroadcastReceiver());
        Log.d(TAG, "App chiusa");
    }

    public void connectP2p(final WifiP2pDevice device) {
        deviceManager.connectTo(device);
    }


    public void writeTextMessage(String text, String receiver) {
        deviceManager.sendTextMessage(text, receiver);
    }

    public void sendFile(Uri filePath, String receiver) {
        deviceManager.sendFile(filePath, receiver);
    }

    public void sendDataMessage(Uri filePath, String receiver) {
        deviceManager.sendDataMessage(deviceManager.currentDest);
    }


    public void addMessage(Message message) {
        synchronized (lock) {
            messageToProcess.add(message);
        }
    }

    public void removeMessage(Message message) {
        synchronized (lock) {
            messageToProcess.remove(message);
        }
    }

    public void setGroupPeers(Collection<WifiP2pDevice> groupPeers) {

        if (chatFragment != null && deviceManager.getDeviceStatus() == Constants.DEVICE_CONNECTED) {
            //We have the peer list, let's join the chat fragment
            DevicesListFragment.WiFiDevicesAdapter adapter = devicesListFragment.getWiFiDeviceAdapter();
            adapter.clear();
            if (!chatFragmentShowed) {
                chatFragmentShowed = true;
                getFragmentManager().beginTransaction().hide(devicesListFragment).commit();

                getFragmentManager().beginTransaction().add(R.id.container_root, chatFragment, "services_chat").commit();
            }

            List<String> participants = new ArrayList<>();

            for (WifiP2pDevice device : groupPeers) {
                participants.add(device.deviceName);
            }
            this.groupPeers.addAll(participants);
            chatFragment.setParticipants(participants);
        }
    }

    public void addParticipant(String deviceName) {
        synchronized (lock) {
            groupPeers.add(deviceName);
        }
    }

    public void updatePeers() {
        if (devicesListFragment != null && deviceManager.getDeviceStatus() == Constants.DEVICE_DISCOVERY) {
            DevicesListFragment.WiFiDevicesAdapter adapter = devicesListFragment.getWiFiDeviceAdapter();
            // Informa l'adapter che la lista e' cambiata, cambia anche la view
            adapter.notifyDataSetChanged();
        }
    }

    public void updateDevice() {

    }

    public String getDeviceName() {
        return deviceManager.getDeviceName();
    }

    /** Called when the user clicks the Groupownami button */
    public void groupownami(View view) {
        // Do something in response to button
//        Log.d(TAG, "button clicked");
//        deviceManager.createGroup();
    }
    /** Called when the user clicks the Pingpongami button */
    public void pingpongami(View view) {
//        Log.d(TAG, "button clicked");
//        deviceManager.startPingPongProcedure();
    }

    @Override
    public void onFinishUserDialog(String user) {
        Toast.makeText(this, "Hello, " + user, Toast.LENGTH_SHORT).show();
    }

    public void onClick(View view) {
        // close existing dialog fragments
        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_edit_name");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        switch (view.getId()) {
            case R.id.showCustomFragment:
                MyDialogFragment editNameDialog = new MyDialogFragment();
                editNameDialog.show(manager, "fragment_edit_name");
                break;
            case R.id.showAlertDialogFragment:
                MyAlertDialogFragment alertDialogFragment = new MyAlertDialogFragment();
                alertDialogFragment.show(manager, "fragment_edit_name");
                break;
        }
    }
}