package org.jbei.ice.lib.shared.dto.entry;

import java.util.Date;

import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.user.User;

public class SequenceAnalysisInfo implements IDTOModel {

    private String name;
    private User depositor;
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

    public User getDepositor() {
        return depositor;
    }

    public void setDepositor(User depositor) {
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
