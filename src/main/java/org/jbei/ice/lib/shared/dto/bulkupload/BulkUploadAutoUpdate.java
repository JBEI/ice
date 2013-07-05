package org.jbei.ice.lib.shared.dto.bulkupload;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.lib.shared.dto.IDTOModel;
import org.jbei.ice.lib.shared.dto.entry.EntryType;

/**
 * @author Hector Plahar
 */
public class BulkUploadAutoUpdate implements IDTOModel {

    private HashMap<EntryField, String> keyValue;   // header being updated -> value for that row
    private long entryId;
    private long bulkUploadId;
    private EntryType type;
    private Date lastUpdate;
    private int row;

    // no arg constructor for serializations
    public BulkUploadAutoUpdate() {
        keyValue = new HashMap<EntryField, String>();
    }

    public HashMap<EntryField, String> getKeyValue() {
        return this.keyValue;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public long getBulkUploadId() {
        return bulkUploadId;
    }

    public void setBulkUploadId(long bulkUploadId) {
        this.bulkUploadId = bulkUploadId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<EntryField, String> set : keyValue.entrySet()) {
            sb.append("AutoUpdate (entry:" + entryId + ", bulkupload:" + bulkUploadId + ", field:" + set.getKey()
                              + ", value:" + set.getValue() + ", row:" + row + ")");
            i += 1;
            if (i < keyValue.size())
                sb.append("\n");
        }
        return sb.toString();
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
