package it.unibo.mobile.d2dchat.fragment;

import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unibo.mobile.d2dchat.MainActivity;
import it.unibo.mobile.d2dchat.R;
import it.unibo.mobile.d2dchat.device.DeviceManager;

/**
 * Created by Stefano on 18/07/2016.
 */
public class DevicesListFragment extends ListFragment {

    private DeviceManager deviceManager;
    WiFiDevicesAdapter listAdapter = null;
    Button send = null;
    View view;

    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.devices_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listAdapter = new WiFiDevicesAdapter(this.getActivity(),
                android.R.layout.simple_list_item_2, android.R.id.text1);
        setListAdapter(listAdapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        ((MainActivity) getActivity()).connectP2p((WifiP2pDevice) l
                .getItemAtPosition(position));

    }


    public class WiFiDevicesAdapter extends ArrayAdapter<WifiP2pDevice> {

        public WiFiDevicesAdapter(Context context, int resource,
                                  int textViewResourceId) {
            super(context, resource, textViewResourceId, deviceManager.getPeers());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_2, null);
            }
            WifiP2pDevice service = deviceManager.getPeers().get(position);
            if (service != null) {
                TextView nameText = (TextView) v
                        .findViewById(android.R.id.text1);

                if (nameText != null) {
                    nameText.setText(service.deviceName);
                }

                TextView infoText = (TextView) v
                        .findViewById(android.R.id.text2);
                if (infoText != null) {
                    infoText.setText((service.isGroupOwner() ? "GO" : getDeviceStatus(service.status)));
                }

            }
            return v;
        }

    }

    public static String getDeviceStatus(int statusCode) {
        switch (statusCode) {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    public WiFiDevicesAdapter getWiFiDeviceAdapter(){
        return listAdapter;
    }

}