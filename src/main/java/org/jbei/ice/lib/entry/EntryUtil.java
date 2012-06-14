package org.jbei.ice.lib.entry;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jbei.ice.shared.dto.EntryType;

/**
 * Utility class for operating on entries
 * 
 * @author Hector Plahar
 */
public class EntryUtil {

    /**
     * Generate the options map of entry types containing friendly names for entryType field.
     * 
     * @return Map of entry types and names.
     */
    public static Map<String, String> getEntryTypeOptionsMap() {
        Map<String, String> resultMap = new LinkedHashMap<String, String>();
        for (EntryType type : EntryType.values()) {
            resultMap.put(type.getName(), type.getDisplay());
        }

        return resultMap;
    }

}
