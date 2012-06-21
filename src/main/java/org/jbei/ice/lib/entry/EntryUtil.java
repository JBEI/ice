package org.jbei.ice.lib.entry;

import com.google.common.base.Joiner;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.shared.dto.EntryType;

import java.util.LinkedHashMap;
import java.util.Map;

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

    /**
     * Generate the comma separated string representation of {@link org.jbei.ice.lib.entry.model.PartNumber}s
     * associated with
     * this entry.
     *
     * @return Comma separated part numbers.
     */
    public static String getPartNumbersAsString(Entry entry) {
        Joiner joiner = Joiner.on(", ").skipNulls();
        return joiner.join(entry.getPartNumbers());
    }
}
