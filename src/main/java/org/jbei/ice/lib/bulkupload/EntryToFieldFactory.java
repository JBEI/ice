package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.model.Entry;

/**
 * Converts an entry into BulkUploadAutoUpdate Field
 *
 * @author Hector Plahar
 */
public class EntryToFieldFactory {

    public static BulkUploadAutoUpdate getUpdate(Entry entry) {
        BulkUploadAutoUpdate update = new BulkUploadAutoUpdate(EntryType.nameToType(entry.getRecordType()));
        update.getKeyValue().put(EntryField.PI, entry.getPrincipalInvestigator());
        update.getKeyValue().put(EntryField.FUNDING_SOURCE, entry.getFundingSource());
        update.getKeyValue().put(EntryField.BIOSAFETY_LEVEL, entry.getBioSafetyLevel().toString());
        update.getKeyValue().put(EntryField.NAME, entry.getName());
        update.getKeyValue().put(EntryField.ALIAS, entry.getAlias());
        update.getKeyValue().put(EntryField.SUMMARY, entry.getShortDescription());
        update.getKeyValue().put(EntryField.KEYWORDS, entry.getKeywords());
        return update;
    }
}
