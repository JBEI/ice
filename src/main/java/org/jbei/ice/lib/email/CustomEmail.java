package org.jbei.ice.lib.email;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;

/**
 * @author Hector Plahar
 */
public class CustomEmail extends Email {

    /**
     * Sends an email to the specified recipient with a carbon copy send to the specified ccEmail.
     * The email contains the specified subject and body
     *
     * @param receiverEmail Address to send email to.
     * @param ccEmail       Address to send carbon copy to.
     * @param subject       Text of subject.
     * @param body          Text of body.
     */
    public boolean send(String receiverEmail, String ccEmail, String subject, String body) {
        String hostName = Utils.getConfigValue(ConfigurationKey.SMTP_HOST);
        if (StringUtils.isEmpty(hostName)) {
            return false;
        }

        org.apache.commons.mail.Email email = new SimpleEmail();
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
    public boolean send(String receiverEmail, String subject, String body) {
        return send(receiverEmail, Utils.getConfigValue(ConfigurationKey.ADMIN_EMAIL), subject, body);
    }

    /**
     * Send an email to the administrator.
     *
     * @param subject Subject text.
     * @param body    Body text.
     */
    public void sendError(String subject, String body) {
        send(Utils.getConfigValue(ConfigurationKey.ADMIN_EMAIL), subject, body);
    }
}
