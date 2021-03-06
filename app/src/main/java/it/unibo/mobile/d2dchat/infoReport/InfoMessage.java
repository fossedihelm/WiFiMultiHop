package it.unibo.mobile.d2dchat.infoReport;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import it.unibo.mobile.d2dchat.BR;

/**
 * Created by ghosty on 06/05/17.
 */

public class InfoMessage extends BaseObservable {
    private Boolean go = true;

    private Integer partialSentMessage = 0;
    private Integer partialRecvMessage = 0;

    private double averageRTT = 0;
    private double averageReconnectionTime = 0;

    private Integer totalSentMessage = 0;
    private Integer totalRecvMessage = 0;

    private Integer runNumber = 0;

    public String toPrint="";
    public String fileName="";

    @Bindable
    public Boolean getGo() {
        return go;
    }

    public void setGo(Boolean go) {
        this.go = go;
    }

    @Bindable
    public Integer getPartialSentMessage() {
        return partialSentMessage;
    }

    public void setPartialSentMessage(Integer partialSentMessage) {
        this.partialSentMessage = partialSentMessage;
    }

    @Bindable
    public Integer getPartialRecvMessage() {
        return partialRecvMessage;
    }

    public void setPartialRecvMessage(Integer partialRecvMessage) {
        this.partialRecvMessage = partialRecvMessage;
    }

    @Bindable
    public double getAverageRTT() {
        return averageRTT;
    }

    public void setAverageRTT(double averageRTT) {
        this.averageRTT = averageRTT / 1000;
    }

    @Bindable
    public double getAverageReconnectionTime() {
        return averageReconnectionTime;
    }

    public void setAverageReconnectionTime(double averageReconnectionTime) {
        this.averageReconnectionTime = averageReconnectionTime / 1000;
    }

    @Bindable
    public Integer getTotalSentMessage() {
        return totalSentMessage;
    }

    public void setTotalSentMessage(Integer totalSentMessage) {
        this.totalSentMessage = totalSentMessage;
    }

    @Bindable
    public Integer getTotalRecvMessage() {
        return totalRecvMessage;
    }

    public void setTotalRecvMessage(Integer totalRecvMessage) {
        this.totalRecvMessage = totalRecvMessage;
    }

    @Bindable
    public Integer getRunNumber() {
        return runNumber;
    }

    public void setRunNumber(Integer runNumber) {
        this.runNumber = runNumber;
    }
}
