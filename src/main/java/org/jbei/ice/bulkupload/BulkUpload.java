package org.jbei.ice.bulkupload;

import org.jbei.ice.account.Account;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.folder.AbstractFolder;

import java.util.ArrayList;
import java.util.Date;

/**
 * Data transfer model for bulk upload. Wraps information about the bulk upload
 * including permissions and parts contained in each
 *
 * @author Hector Plahar
 */
public class BulkUpload extends AbstractFolder {

    private String name;
    private String type;
    private String linkType;
    private int count;
    private long created;
    private long lastUpdate;
    private Account account;
    private BulkUploadStatus status;
    private final ArrayList<PartData> entryList;

    public BulkUpload() {
        entryList = new ArrayList<>();
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

    public void setLinkType(String linkType) {
        this.linkType = linkType;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
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
}