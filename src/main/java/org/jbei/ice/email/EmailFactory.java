package org.jbei.ice.email;

import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.utils.Utils;

/**
 * Factory for instantiation the specified email object for use
 *
 * @author Hector Plahar
 */
public class EmailFactory {

    public static Email getEmail() {
        String typeString = Utils.getConfigValue(ConfigurationKey.EMAILER);
        Type type = Type.valueOf(typeString);

        switch (type) {
            case CUSTOM:
            default:
                return new CustomEmail();

            case GMAIL:
                return new GMail();
        }
    }
}
