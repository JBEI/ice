package org.jbei.ice.lib.utils;

import java.util.Map;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;

/**
 * Constants used in gd-ice.
 * 
 * @author Timothy Ham
 * 
 */
public class JbeiConstants {

    /**
     * Return friendly names for {@link Entry}.status field.
     * 
     * @param key
     * @return
     */
    public final static String getStatus(String key) {
        String result = "";
        if (key.equals("complete")) {
            result = "Complete";
        } else if (key.equals("in progress")) {
            result = "In Progress";
        } else if (key.equals("planned")) {
            result = "Planned";
        }
        return result;
    }

    /**
     * Return friendly names for assembly format.
     * 
     * @param key
     * @return
     */
    public final static String getPackageFormat(String key) {
        Map<String, String> map = Part.getPackageFormatOptionsMap();
        String result = map.get(key);
        if (result == null) {
            result = "Unrecognized Format";
        }

        return result;
    }

    /**
     * Return friendly names for {@link Entry}.recordType.
     * 
     * @param key
     * @return
     */
    public final static String getRecordType(String key) {
        String result = "";
        if (key.equals(Entry.PART_ENTRY_TYPE)) {
            result = "Part";
        } else if (key.equals(Entry.PLASMID_ENTRY_TYPE)) {
            result = "Plasmid";
        } else if (key.equals(Entry.STRAIN_ENTRY_TYPE)) {
            result = "Strain";
        } else if (key.equals(Entry.ARABIDOPSIS_SEED_ENTRY_TYPE)) {
            result = "Arabidopsis Seed";
        }
        return result;
    }
}
