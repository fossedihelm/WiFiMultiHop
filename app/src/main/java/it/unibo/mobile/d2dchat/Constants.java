package it.unibo.mobile.d2dchat;

/**
 * Created by Stefano on 22/07/2016.
 */
public class Constants {

    public static final int SERVER_PORT = 4545;

    //Message codes
    public static final int MESSAGE_REGISTER = 1;
    public static final int MESSAGE_TEXT = 2;
    public static final int MESSAGE_DATA = 3;
    public static final int MESSAGE_FILE = 4;
    public static final int MESSAGE_STOP = 5;
    public static final int MESSAGE_STOP_ACK = 6;


    //EVENT codes
    public static final int EVENT_REGISTER = 1;
    public static final int EVENT_MESSAGE = 2;

    //RECEIVER code
    public static final String GROUP_MESSAGE = "all";

    //MAIN activity status
    public static final int STATUS_CHAT = 1;

    //Device status
    public static final int DEVICE_INIT = 0;
    public static final int DEVICE_DISCOVERY = 1;
    public static final int DEVICE_CONNECTED = 2;
    public static final int DEVICE_NOWIFI = 3;
    public static final int DEVICE_DISCONNECTED = 4; // when switching, right after disconnecting
}
