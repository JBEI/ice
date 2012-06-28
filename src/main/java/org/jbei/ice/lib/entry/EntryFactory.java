package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.utils.Utils;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Factory for creating entry objects in the database
 *
 * @author Hector Plahar
 */
public class EntryFactory {

    public static Entry createEntry(Account account, String number, Entry newEntry) {
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

        newEntry.setCreator(account.getFullName());
        newEntry.setCreatorEmail(account.getEmail());

        return newEntry;
    }
}
