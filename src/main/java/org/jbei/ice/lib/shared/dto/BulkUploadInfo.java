package org.jbei.ice.lib.shared.dto;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;
import org.jbei.ice.lib.shared.dto.permission.PermissionInfo;

/**
 * Data transfer model for bulk upload
 *
 * @author Hector Plahar
 */
public class BulkUploadInfo implements IDTOModel {

    private long id;
    private String name;
    private EntryAddType type;
    private int count;
    private Date created;
    private Date lastUpdate;
    private AccountInfo account;
    private ArrayList<EntryInfo> entryList;
    private ArrayList<PreferenceInfo> preferences;
    private ArrayList<PermissionInfo> permissions;

    public BulkUploadInfo() {
        entryList = new ArrayList<EntryInfo>();
        preferences = new ArrayList<PreferenceInfo>();
        permissions = new ArrayList<PermissionInfo>();
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

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public ArrayList<EntryInfo> getEntryList() {
        return entryList;
    }

    public ArrayList<PreferenceInfo> getPreferences() {
        return preferences;
    }

    public ArrayList<PermissionInfo> getPermissions() {
        return permissions;
    }
}
