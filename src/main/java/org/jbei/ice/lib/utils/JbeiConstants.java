package org.jbei.ice.lib.utils;

import java.util.Map;

import org.jbei.ice.lib.models.Part;

public class JbeiConstants {

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

    public final static String getPackageFormat(String key) {
        Map<String, String> map = Part.getPackageFormatOptionsMap();
        String result = map.get(key);
        if (result == null) {
            result = "Unrecognized Format";
        }

        return result;
    }

    public final static String getRecordType(String key) {
        String result = "";
        if (key.equals("part")) {
            result = "Part";
        } else if (key.equals("plasmid")) {
            result = "Plasmid";
        } else if (key.equals("strain")) {
            result = "Strain";
        }
        return result;
    }
}
