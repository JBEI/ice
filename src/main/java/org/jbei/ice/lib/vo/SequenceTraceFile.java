package org.jbei.ice.lib.vo;

import java.util.Date;

/**
 * Value object for sequence trace data.
 *
 * @author Timothy Ham
 */
public class SequenceTraceFile {
    private String base64Data;
    private String fileName;
    private String fileId;
    private String depositorEmail;
    private Date timeStamp;

    public String getBase64Data() {
        return base64Data;
    }

    public void setBase64Data(String base64Data) {
        this.base64Data = base64Data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getDepositorEmail() {
        return depositorEmail;
    }

    public void setDepositorEmail(String depositorEmail) {
        this.depositorEmail = depositorEmail;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timestamp) {
        timeStamp = timestamp;
    }

}