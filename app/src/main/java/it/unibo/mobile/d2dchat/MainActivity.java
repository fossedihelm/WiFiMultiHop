package it.unibo.mobile.d2dchat;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.unibo.mobile.d2dchat.device.DeviceManager;
import it.unibo.mobile.d2dchat.fragment.InGroupFragment;
import it.unibo.mobile.d2dchat.fragment.DevicesListFragment;
import it.unibo.mobile.d2dchat.infoReport.InfoMessage;
import it.unibo.mobile.d2dchat.messagesManager.Message;

public class MainActivity extends AppCompatActivity implements IntervalFragment.TimeListener{


    public static final String TAG = "wifiD2Dchat";

    WifiP2pManager mManager;
    Channel mChannel;
    DevicesListFragment devicesListFragment;
    InGroupFragment inGroupFragment = new InGroupFragment();
    IntentFilter mIntentFilter;

    public InfoMessage mInfoMessage;
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
        mInfoMessage = new InfoMessage();
        //mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        deviceManager = new DeviceManager(mManager, mChannel, this, mInfoMessage);
        //Quando clicchiamo per scrivere e si alza la tastiera, tutto i layout viene mosso con lei
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        devicesListFragment = new DevicesListFragment();
        devicesListFragment.setDeviceManager(deviceManager);
        getFragmentManager().beginTransaction()
                .add(R.id.container_root, devicesListFragment, "services").commit();
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


    public void setGroupPeers(Collection<WifiP2pDevice> groupPeers) {

        if (inGroupFragment != null && deviceManager.getDeviceStatus() == Constants.DEVICE_CONNECTED) {
            //We have the peer list, let's join the chat fragment
            DevicesListFragment.WiFiDevicesAdapter adapter = devicesListFragment.getWiFiDeviceAdapter();
            adapter.clear();
            if (!chatFragmentShowed) {
                chatFragmentShowed = true;
                getFragmentManager().beginTransaction().hide(devicesListFragment).commit();

                getFragmentManager().beginTransaction().add(R.id.container_root, inGroupFragment, "services_chat").commit();
            }

            List<String> participants = new ArrayList<>();

            for (WifiP2pDevice device : groupPeers) {
                participants.add(device.deviceName);
            }
            this.groupPeers.addAll(participants);
//            inGroupFragment.setParticipants(participants);
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
//         Do something in response to button
        Log.d(TAG, "button clicked");
        deviceManager.createGroup();
        mInfoMessage.setGo(true);
    }
    /** Called when the user clicks the Pingpongami button */
    public void pingpongami(View view) {
        Log.d(TAG, "button clicked");
//        deviceManager.startPingPongProcedure();
        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_edit_name");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        IntervalFragment editNameDialog = new IntervalFragment();
        editNameDialog.show(manager, "fragment_edit_name");
    }

    @Override
    public void onFinishTimeDialog(Integer time) {
        deviceManager.timeInterval = time;
        deviceManager.startPingPongProcedure();
        mInfoMessage.setGo(false);
//        Toast.makeText(this, "Hello, " + user, Toast.LENGTH_SHORT).show();
    }
    public void test(View view){

        inGroupFragment.infoMessage.setPartialRecvMessage(inGroupFragment.infoMessage.getPartialRecvMessage()+1);
        inGroupFragment.binding.executePendingBindings();
    }
}