package org.jbei.ice.lib.dto.search;

import org.jbei.ice.lib.dto.entry.EntryFieldLabel;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * Filter a specified entry field
 *
 * @author Hector Plahar
 */
public class FieldFilter implements IDataTransferModel {

    private EntryFieldLabel field;
    private String filter;

    public EntryFieldLabel getField() {
        return field;
    }

    public void setField(EntryFieldLabel field) {
        this.field = field;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
