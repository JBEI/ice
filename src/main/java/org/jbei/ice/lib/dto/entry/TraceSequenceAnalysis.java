package org.jbei.ice.lib.dto.entry;

import java.util.Date;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dao.IDataTransferModel;

public class TraceSequenceAnalysis implements IDataTransferModel {

    private long id;
    private String filename;
    private AccountTransfer depositor;
    private long created;
    private String fileId;
    private String sequence;
    private TraceSequenceAlignmentInfo traceSequenceAlignment;

    public TraceSequenceAnalysis() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String name) {
        this.filename = name;
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
}
