package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.cxf.common.util.StringUtils;

/**
 * Utility methods for email.
 * <p/>
 * The SMTP server is specified in the configuration file and the admin email is also used for all communications
 *
 * @author Hector Plahar, Zinovii Dmytriv, Timothy Ham
 */
public class Emailer {

    /**
     * Sends an email to the specified recipient with a carbon copy send to the specified ccEmail.
     * The email contains the specified subject and body
     *
     * @param receiverEmail Address to send email to.
     * @param ccEmail       Address to send carbon copy to.
     * @param subject       Text of subject.
     * @param body          Text of body.
     */
    public static boolean send(String receiverEmail, String ccEmail, String subject, String body) {
        String hostName = Utils.getConfigValue(ConfigurationKey.SMTP_HOST);
        if (StringUtils.isEmpty(hostName)) {
            return false;
        }

        Email email = new SimpleEmail();
        email.setHostName(hostName);
        try {
            email.setFrom(Utils.getConfigValue(ConfigurationKey.ADMIN_EMAIL));
            email.addTo(receiverEmail);
            email.addCc(ccEmail);
            email.setSubject(subject);
            email.setMsg(body);
            email.send();
            return true;
        } catch (EmailException e) {
            Logger.error(e);
            return false;
        }
    }

    /**
     * Send an email, and cc to the administrator.
     *
     * @param receiverEmail Address to send email to.
     * @param subject       Subject text.
     * @param body          Body text.
     */
    public static boolean send(String receiverEmail, String subject, String body) {
        return send(receiverEmail, Utils.getConfigValue(ConfigurationKey.ADMIN_EMAIL), subject, body);
    }

    /**
     * Send an email to the administrator.
     *
     * @param subject Subject text.
     * @param body    Body text.
     */
    public static void error(String subject, String body) {
        send(Utils.getConfigValue(ConfigurationKey.ADMIN_EMAIL), subject, body);
    }
}
