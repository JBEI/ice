package org.jbei.ice.dto.entry;

import org.jbei.ice.account.Account;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.Date;

public class TraceSequenceAnalysis implements IDataTransferModel {

    private long id;
    private String filename;
    private Account depositor;
    private long created;
    private String fileId;
    private String sequence;
    private boolean canEdit;
    private TraceSequenceAlignmentInfo traceSequenceAlignment;

    public TraceSequenceAnalysis() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String name) {
        this.filename = name;
    }

    public Account getDepositor() {
        return depositor;
    }

    public void setDepositor(Account depositor) {
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

    public TraceSequenceAlignmentInfo getTraceSequenceAlignment() {
        return traceSequenceAlignment;
    }

    public void setTraceSequenceAlignment(TraceSequenceAlignmentInfo traceSequenceAlignment) {
        this.traceSequenceAlignment = traceSequenceAlignment;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
}
