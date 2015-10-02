package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.Date;

/**
 * Data transfer model for bulk upload
 *
 * @author Hector Plahar
 */
public class BulkUploadInfo implements IDataTransferModel {

    private long id;
    private String name;
    private String type;
    private int count;
    private long created;
    private long lastUpdate;
    private AccountTransfer account;
    private BulkUploadStatus status;
    private ArrayList<PartData> entryList;
    private ArrayList<AccessPermission> permissions;

    public BulkUploadInfo() {
        entryList = new ArrayList<>();
        permissions = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created.getTime();
    }

    public AccountTransfer getAccount() {
        return account;
    }

    public void setAccount(AccountTransfer account) {
        this.account = account;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate.getTime();
    }

    public ArrayList<PartData> getEntryList() {
        return entryList;
    }

    public BulkUploadStatus getStatus() {
        return status;
    }

    public void setStatus(BulkUploadStatus status) {
        this.status = status;
    }

    public ArrayList<AccessPermission> getPermissions() {
        return this.permissions;
    }

}
