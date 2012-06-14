package org.jbei.ice.lib.entry;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.utils.Utils;

/**
 * Factory for creating entry objects in the database
 * 
 * @author Hector Plahar
 */
public class EntryFactory {

    public static Entry createEntry(String number, Entry newEntry) {
        PartNumber partNumber = new PartNumber();
        partNumber.setPartNumber(number);
        Set<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();
        partNumbers.add(partNumber);
        newEntry.getPartNumbers().add(partNumber);
        if (newEntry.getRecordId() == null || "".equals(newEntry.getRecordId())) {
            newEntry.setRecordId(Utils.generateUUID());
        }
        if (newEntry.getVersionId() == null || "".equals(newEntry.getVersionId())) {
            newEntry.setVersionId(newEntry.getRecordId());
        }
        if (newEntry.getCreationTime() == null) {
            newEntry.setCreationTime(Calendar.getInstance().getTime());
        } else {
            newEntry.setModificationTime(Calendar.getInstance().getTime());
        }

        return newEntry;
    }
}
