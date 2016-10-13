package it.unibo.mobile.d2dchat.messagesManager;

import java.io.Serializable;

/**
 * Created by Stefano on 14/09/2016.
 */
public class FileAttach implements Serializable {
    public byte[] data;
    public String fileName;

    public FileAttach(byte[] data, String fileName) {
        this.data = data;
        this.fileName = fileName;
    }
}
