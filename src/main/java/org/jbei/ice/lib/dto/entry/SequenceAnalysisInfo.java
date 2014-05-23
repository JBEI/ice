package org.jbei.ice.lib.dto.entry;

import java.util.Date;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dao.IDataTransferModel;

public class SequenceAnalysisInfo implements IDataTransferModel {

    private long id;
    private String name;
    private AccountTransfer depositor;
    private long created;
    private String fileId;

    public SequenceAnalysisInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountTransfer getDepositor() {
        return depositor;
    }

    public void setDepositor(AccountTransfer depositor) {
        this.depositor = depositor;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created.getTime();
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
