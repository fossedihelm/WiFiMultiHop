package it.unibo.mobile.d2dchat.messagesManager;

import java.io.Serializable;

/**
 * Created by Stefano on 22/07/2016.
 */
public class Message implements Serializable {

    private int type;
    private String sender;
    private String receiver;
    private Object data;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
