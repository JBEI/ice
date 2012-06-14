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
     * @return friendly name.
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
     * @return friendly name.
     */
    public final static String getPackageFormat(String key) {
        Map<String, String> map = Part.getPackageFormatOptionsMap();
        String result = map.get(key);
        if (result == null) {
            result = "Unrecognized Format";
        }

        return result;
    }
}
