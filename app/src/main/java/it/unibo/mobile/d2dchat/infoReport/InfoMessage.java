package it.unibo.mobile.d2dchat.infoReport;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import it.unibo.mobile.d2dchat.BR;

/**
 * Created by ghosty on 06/05/17.
 */

public class InfoMessage extends BaseObservable {
    private Integer partialSentMessage = 0;
    private Integer partialRecvMessage = 0;

    private Integer totalSentMessage = 0;
    private Integer totalRecvMessage = 0;

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
        notifyPropertyChanged(BR.partialRecvMessage);
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
}
