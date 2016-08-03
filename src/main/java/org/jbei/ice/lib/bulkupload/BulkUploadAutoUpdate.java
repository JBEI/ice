package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.bulkupload.EditMode;
import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Data model for bulk upload auto updates
 *
 * @author Hector Plahar
 */
public class BulkUploadAutoUpdate implements IDataTransferModel {

    private HashMap<EntryField, String> keyValue;   // header being updated -> value for that row
    private long entryId;
    private long bulkUploadId;
    private EntryType type;
    private Date lastUpdate;
    private int row;
    private EditMode editMode;

    public BulkUploadAutoUpdate(EntryType type) {
        this(type, EditMode.DEFAULT);
    }

    public BulkUploadAutoUpdate(EntryType type, EditMode mode) {
        this.type = type;
        keyValue = new HashMap<>();
        this.editMode = mode;
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
            sb.append("AutoUpdate [mode = ").append(editMode.toString()).append("] (entry:").append(entryId)
              .append(", bulkupload:").append(bulkUploadId).append(
                    ", field:").append(set.getKey()).append(", value:").append(set.getValue()).append(", row:").append(
                    row).append(")");
            i += 1;
            if (i < keyValue.size())
                sb.append("\n");
        }
        return sb.toString();
    }

    public EntryType getType() {
        return type;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public EditMode getEditMode() {
        return editMode;
    }
}
