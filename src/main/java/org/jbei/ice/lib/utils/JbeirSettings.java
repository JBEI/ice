package org.jbei.ice.lib.utils;

import java.util.ResourceBundle;

import org.jbei.ice.lib.logging.Logger;

public class JbeirSettings {

    public static String getSetting(String key) {
        String result = "";
        try {
            ResourceBundle resource = ResourceBundle.getBundle("jbeir");
            String tmp = resource.getString(key).trim();
            if (tmp != null) {
                result = tmp;
            }
        } catch (Exception e) {
            String msg = "Could not read properties file: " + e.toString();
            Logger.error(msg);
        }
        return result;
    }

}
