package org.jbei.ice.lib.folder;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Wrapper around counts for collections
 *
 * @author Hector Plahar
 */
public class CollectionCounts implements IDataTransferModel {

    private long available;
    private long personal;
    private long shared;
    private long deleted;
    private long drafts;
    private long pending;
    private long transferred;

    public long getAvailable() {
        return available;
    }

    public void setAvailable(long available) {
        this.available = available;
    }

    public long getPersonal() {
        return personal;
    }

    public void setPersonal(long personal) {
        this.personal = personal;
    }

    public long getShared() {
        return shared;
    }

    public void setShared(long shared) {
        this.shared = shared;
    }

    public long getDeleted() {
        return deleted;
    }

    public void setDeleted(long deleted) {
        this.deleted = deleted;
    }

    public long getBulkUpload() {
        return drafts;
    }

    public void setDrafts(long drafts) {
        this.drafts = drafts;
    }

    public long getPending() {
        return this.pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getTransferred() {
        return transferred;
    }

    public void setTransferred(long transferred) {
        this.transferred = transferred;
    }
}
