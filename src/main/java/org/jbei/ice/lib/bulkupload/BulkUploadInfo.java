package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.dto.bulkupload.Headers;
import org.jbei.ice.lib.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.shared.EntryAddType;

/**
 * Data transfer model for bulk upload
 *
 * @author Hector Plahar
 */
public class BulkUploadInfo implements IDataTransferModel {

    private long id;
    private String name;
    private EntryAddType type;
    private int count;
    private Date created;
    private Date lastUpdate;
    private AccountTransfer account;
    private BulkUploadStatus status;
    private ArrayList<PartData> entryList;
    private ArrayList<PreferenceInfo> preferences;
    private ArrayList<AccessPermission> accessPermissions;
    private ArrayList<BulkUploadAutoUpdate> updates;
    private ArrayList<Headers> headers;
    private ArrayList<Entry> entries;

    public BulkUploadInfo() {
        entryList = new ArrayList<>();
        preferences = new ArrayList<>();
        accessPermissions = new ArrayList<>();
        updates = new ArrayList<>();
        headers = new ArrayList<>();
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

    public EntryAddType getType() {
        return type;
    }

    public void setType(EntryAddType type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public AccountTransfer getAccount() {
        return account;
    }

    public void setAccount(AccountTransfer account) {
        this.account = account;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public ArrayList<PartData> getEntryList() {
        return entryList;
    }

    public ArrayList<PreferenceInfo> getPreferences() {
        return preferences;
    }

    public ArrayList<AccessPermission> getAccessPermissions() {
        return accessPermissions;
    }

    public BulkUploadStatus getStatus() {
        return status;
    }

    public void setStatus(BulkUploadStatus status) {
        this.status = status;
    }

    public ArrayList<BulkUploadAutoUpdate> getUpdates() {
        return updates;
    }

    public ArrayList<Headers> getHeaders() {
        return headers;
    }

    public void setHeaders(ArrayList<Headers> headers) {
        this.headers = headers;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }
}
