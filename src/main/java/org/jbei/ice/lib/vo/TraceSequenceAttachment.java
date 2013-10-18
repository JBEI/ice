package org.jbei.ice.lib.vo;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlMimeType;

/**
 * @author Hector Plahar
 */
public class TraceSequenceAttachment {

    private String filename;
    private String depositor;

    @XmlMimeType("application/octet-stream")
    private DataHandler data;

    public DataHandler getData() {
        return data;
    }

    public void setData(DataHandler data) {
        this.data = data;
    }

    public String getDepositor() {
        return depositor;
    }

    public void setDepositor(String depositor) {
        this.depositor = depositor;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
