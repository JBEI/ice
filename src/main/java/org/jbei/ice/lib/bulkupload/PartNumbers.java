package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.model.Entry;

import java.util.ArrayList;

/**
 * Part numbers that are can be associated with specific entries (as descendants)
 * in a hierarchical relationship
 *
 * @author Hector Plahar
 */
public class PartNumbers {
    public ArrayList<String> getMatchingPartNumbers(String userId, EntryType type, String token, int limit) {
        ArrayList<String> dataList = new ArrayList<>();
        if (token == null)
            return dataList;

        token = token.replaceAll("'", "");
        for (Entry entry : DAOFactory.getEntryDAO().getMatchingEntryPartNumbers(token, limit)) {
            EntryType entryType = EntryType.nameToType(entry.getRecordType());
            PartData partData = new PartData(entryType);
            partData.setId(entry.getId());
            partData.setPartId(entry.getPartNumber());
            partData.setName(entry.getName());
            dataList.add(entry.getPartNumber());
        }
        return dataList;
    }
}
