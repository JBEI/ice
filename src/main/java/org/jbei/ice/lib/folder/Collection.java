package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class Collection implements IDataTransferModel {

    private int available;
    private int personal;
    private int shared;
    private int deleted;

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getPersonal() {
        return personal;
    }

    public void setPersonal(int personal) {
        this.personal = personal;
    }

    public int getShared() {
        return shared;
    }

    public void setShared(int shared) {
        this.shared = shared;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }
}
