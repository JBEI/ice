package org.jbei.ice.lib.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Retrieve setting from the external configuration file.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 * @deprecated settings have been moved to the database
 */
public class JbeirSettings {

    /**
     * Retrieve the configuration from file.
     *
     * @param key key of the configuration.
     * @return - value associated with the given key.
     */
    public static String getSetting(String key) throws MissingResourceException {
        String result = "";
        try {
            ResourceBundle resource = ResourceBundle.getBundle("jbeir");
            String tmp = resource.getString(key).trim();
            if (tmp != null) {
                result = tmp;
            }
        } catch (Exception e) {
            String msg = "Could not read properties file: " + e.toString();
            throw new MissingResourceException(msg, ResourceBundle.class.getName(), key);
        }
        return result;
    }

}
