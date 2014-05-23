package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class Collection implements IDataTransferModel {

    private long available;
    private long personal;
    private long shared;
    private long deleted;
    private long bulkUpload;

    public long getAvailable() {
        return available;
    }

    public void setAvailable(long available) {
        this.available = available;
        this.shared = available;
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
        return bulkUpload;
    }

    public void setBulkUpload(long bulkUpload) {
        this.bulkUpload = bulkUpload;
    }
}
