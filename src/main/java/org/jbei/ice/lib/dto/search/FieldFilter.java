package org.jbei.ice.lib.dto.search;

import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * Filter a specified entry field
 *
 * @author Hector Plahar
 */
public class FieldFilter implements IDataTransferModel {

    private EntryField field;
    private String filter;

    public EntryField getField() {
        return field;
    }

    public void setField(EntryField field) {
        this.field = field;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
