package org.jbei.ice.lib.dto.bulkupload;

import org.jbei.ice.lib.bulkupload.BulkCSVUploadHeaders;
import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.shared.EntryAddType;

/**
 * @author Hector Plahar
 */
public class Headers implements IDataTransferModel {

    private String value;
    private boolean required;
    private boolean canLock;

    public Headers(EntryField field, EntryAddType type) {
        this.value = field.getLabel();
        required = BulkCSVUploadHeaders.isRequired(field, type);
        this.canLock = field.isCanLock();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isCanLock() {
        return canLock;
    }

    public void setCanLock(boolean canLock) {
        this.canLock = canLock;
    }
}
