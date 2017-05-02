package it.unibo.mobile.d2dchat.device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asig on 5/2/17.
 */

public class DeviceQueue {
    private ArrayList<List> list = null;
    private ArrayList<String> devices = null;

    public DeviceQueue(int size) {
        list = new ArrayList<>(size);
        devices = new ArrayList<>(size);
    }

    public List getQueue(String address) {
        for (int i = 0; i < devices.size(); i++) {
            if (address.equals(devices.get(i))) {
                return list.get(i);
            }
        }
        return null;
    }

    public void add(List l, String address) {
        list.add(l);
        devices.add(address);
    }
}
