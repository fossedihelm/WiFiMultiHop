package it.unibo.mobile.d2dchat.messagesManager;

import java.io.Serializable;
import it.unibo.mobile.d2dchat.Constants;
import java.util.Formatter;

public class Message implements Serializable {
    private int type;
    private String source;
    private String dest;
    private int seqNum;
    private long sendTime;
    private Object goList;
    private int switchTime = -1;

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public Object getGoList() {
        return goList;
    }
    public void setGoList(Object data) {
        this.goList = data;
    }

    public int getSeqNum() {
        return seqNum;
    }
    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }
    public void incSeqNum() { seqNum++; }

    public String getSource() { return source; }
    public void setSource(String source) {
        this.source = source;
    }

    public String getDest() {
        return dest;
    }
    public void setDest(String dest) {
        this.dest = dest;
    }

    public long getSendTime() { return sendTime; }
    public void setSendTime(long sendTime) { this.sendTime = sendTime; }

    public int getSwitchTime() {
        return switchTime;
    }

    public void setSwitchTime(int switchTime) {
        this.switchTime = switchTime;
    }

    public String getContents() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        String type = null;
        switch (getType()) {
            case Constants.MESSAGE_REGISTER:
                type = "REGISTER";
                break;
            case Constants.MESSAGE_TEXT:
                type = "TEXT";
                break;
            case Constants.MESSAGE_DATA:
                type = "DATA";
                break;
            case Constants.MESSAGE_FILE:
                type = "FILE";
                break;
            case Constants.MESSAGE_STOP:
                type = "STOP";
                break;
            case Constants.MESSAGE_STOP_ACK:
                type = "STOP_ACK";
        }
        String data = "null";
        if (getGoList() != null)
            data = "present";
        formatter.format("%-15s%-15s\n", "type", type);
        formatter.format("%-15s%-15s\n", "source", getSource());
        formatter.format("%-15s%-15s\n", "dest", getDest());
        formatter.format("%-15s%-15d\n", "seqNum", getSeqNum());
        formatter.format("%-15s%-15d\n", "sendTime", getSendTime());
        formatter.format("%-15s%-15s\n", "data", data);
        return sb.toString();
    }
}
