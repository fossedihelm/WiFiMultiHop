package it.unibo.mobile.d2dchat.messagesManager;

import java.io.Serializable;

public class Message implements Serializable {
    private int type;
    private String source;
    private String dest;
    private int seqNum;
    private long sendTime;
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
