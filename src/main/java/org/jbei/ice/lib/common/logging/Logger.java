package org.jbei.ice.lib.common.logging;

import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.email.EmailFactory;
import org.jbei.ice.lib.utils.Utils;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger for ICE.
 *
 * @author Hector Plahar
 */
public class Logger {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("org.jbei.ice");

    public static void error(String message) {
        sendEmail(message);
        LOGGER.error(message);
    }

    public static void error(String message, Throwable e) {
        sendEmail(message, e);
        LOGGER.error(message, e);
    }

    public static void error(Throwable e) {
        String message = Utils.stackTraceToString(e);
        sendEmail(message, e);
        LOGGER.error(e.getMessage(), e);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    private static void sendEmail(String message, Throwable e) {
        if (e instanceof MessagingException) {
            // if error is "Can't send email", there is no need to try to send email
            return;
        }

        String value = Utils.getConfigValue(ConfigurationKey.SEND_EMAIL_ON_ERRORS);
        String prefix =  Utils.getConfigValue(ConfigurationKey.ERROR_EMAIL_EXCEPTION_PREFIX);

        if (value != null && value.equalsIgnoreCase("YES")) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String body = "System Time: " + dateFormatter.format((new Date())) + "\n\n";
            body = body + message;
            String subject = "Error";
            EmailFactory.getEmail().sendError(prefix + " " + subject, body);
        }
    }

    private static void sendEmail(String msg) {
        sendEmail(msg, new Exception("Error"));
    }
}
