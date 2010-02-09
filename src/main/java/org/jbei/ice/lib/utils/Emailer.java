package org.jbei.ice.lib.utils;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jbei.ice.lib.logging.Logger;

public class Emailer {
    public static void send(String receiverEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", JbeirSettings.getSetting("SMTP_HOST"));
        // props.put("mail.debug", "true");

        Session session = Session.getInstance(props);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(JbeirSettings.getSetting("ADMIN_EMAIL")));

            InternetAddress[] receivers = {
                    new InternetAddress(JbeirSettings.getSetting("ADMIN_EMAIL")),
                    new InternetAddress(receiverEmail) };

            msg.setRecipients(Message.RecipientType.TO, receivers);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(body);

            Transport.send(msg);
        } catch (MessagingException e) {
            Logger.error("Failed to send email message to " + receiverEmail + "!");
            Logger.error("Error message: " + e.getMessage());
            Logger.error("Stacktrace: " + e.getStackTrace());
        }
    }

    public static void error(String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", JbeirSettings.getSetting("SMTP_HOST"));
        // props.put("mail.debug", "true");

        Session session = Session.getInstance(props);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(JbeirSettings.getSetting("ADMIN_EMAIL")));

            InternetAddress[] receivers = { new InternetAddress(JbeirSettings
                    .getSetting("ADMIN_EMAIL")) };

            msg.setRecipients(Message.RecipientType.TO, receivers);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(body);

            Transport.send(msg);
        } catch (MessagingException e) {
            Logger.error("Error message: " + e.getMessage());
            Logger.error("Stacktrace: " + e.getStackTrace());
        }
    }
}
