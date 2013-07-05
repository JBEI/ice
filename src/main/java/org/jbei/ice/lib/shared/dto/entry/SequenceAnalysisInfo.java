package org.jbei.ice.lib.shared.dto.entry;

import java.util.Date;

import org.jbei.ice.lib.shared.dto.AccountInfo;
import org.jbei.ice.lib.shared.dto.IDTOModel;

public class SequenceAnalysisInfo implements IDTOModel {

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
