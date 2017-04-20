package it.unibo.mobile.d2dchat.messagesManager;

import java.io.Serializable;

public class Message implements Serializable {
    public static final int SETUP = 0;
    public static final int DATA = 1;
    private int type;
    private String source;
    private String dest;
    private int seqNum;
    private Object data;
    private long sendTime;

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
}
