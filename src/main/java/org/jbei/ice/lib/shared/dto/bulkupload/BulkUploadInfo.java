package org.jbei.ice.lib.shared.dto.bulkupload;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
import org.jbei.ice.lib.shared.dto.user.User;

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
    private User account;
    private ArrayList<PartData> entryList;
    private ArrayList<PreferenceInfo> preferences;
    private ArrayList<AccessPermission> accessPermissions;

    public BulkUploadInfo() {
        entryList = new ArrayList<PartData>();
        preferences = new ArrayList<PreferenceInfo>();
        accessPermissions = new ArrayList<AccessPermission>();
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

    public User getAccount() {
        return account;
    }

    public void setAccount(User account) {
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
}
