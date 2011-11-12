package org.jbei.ice.lib.utils;

import java.util.ResourceBundle;

import org.jbei.ice.lib.logging.Logger;

/**
 * Retrieve setting from the external configuration file.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class JbeirSettings {

    /**
     * Retrieve the configuration from file.
     * 
     * @param key
     *            - key of the configuration.
     * @return - value associated with the given key.
     */
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
            Logger.error(msg, e);
        }
        return result;
    }

}
