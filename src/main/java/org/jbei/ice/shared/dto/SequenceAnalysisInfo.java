package org.jbei.ice.shared.dto;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SequenceAnalysisInfo implements IsSerializable {

    private String name;
    private AccountInfo depositor;
    private Date created;
    private String fileId;

    public SequenceAnalysisInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountInfo getDepositor() {
        return depositor;
    }

    public void setDepositor(AccountInfo depositor) {
        this.depositor = depositor;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
