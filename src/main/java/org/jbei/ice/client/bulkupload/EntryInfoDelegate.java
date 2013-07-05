package org.jbei.ice.client.bulkupload;

import org.jbei.ice.lib.shared.dto.entry.EntryType;

/**
 * Delegate for retrieving entry info associated with a row
 *
 * @author Hector Plahar
 */
public interface EntryInfoDelegate {

    /**
     * @param row the row that is currently being worked on (and needs saving)
     * @return retrieved id for info on that row or 0
     */
    long getEntryIdForRow(int row);

    void callBackForLockedColumns(int row, long bulkUploadId, long entryId, EntryType type);

    long getBulkUploadId();
}
