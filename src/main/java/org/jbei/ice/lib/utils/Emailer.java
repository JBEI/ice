package org.jbei.ice.lib.utils;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.shared.dto.ConfigurationKey;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

/**
 * Utility methods for email.
 * <p/>
 * The SMTP server is specified in the configuration file.
 *
 * @author Zinovii Dmytriv, Timothy Ham
 */
public class Emailer {
    /**
     * Send an email.
     *
     * @param receiverEmail Address to send email to.
     * @param ccEmail       Address to send carbon copy to.
     * @param subject       Text of subject.
     * @param body          Text of body.
     */
    public static boolean send(String receiverEmail, String ccEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", Utils.getConfigValue(ConfigurationKey.SMTP_HOST));
        // props.put("mail.debug", "true");

        Session session = Session.getInstance(props);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(Utils.getConfigValue(ConfigurationKey.ADMIN_EMAIL)));

            InternetAddress[] receivers = {new InternetAddress(ccEmail), new InternetAddress(receiverEmail)};

            msg.setRecipients(Message.RecipientType.TO, receivers);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(body);

            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            Logger.error("Failed to send email message to " + receiverEmail + "!", e);
            Logger.error("Error message: " + e.getMessage(), e);
            Logger.error("Stacktrace: " + Arrays.toString(e.getStackTrace()), e);
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
