package org.jbei.ice.lib.dto.entry;

import java.util.Date;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dao.IDataTransferModel;

public class SequenceAnalysisInfo implements IDataTransferModel {

    private String name;
    private AccountTransfer depositor;
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

    public AccountTransfer getDepositor() {
        return depositor;
    }

    public void setDepositor(AccountTransfer depositor) {
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
