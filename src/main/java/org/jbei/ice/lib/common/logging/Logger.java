package org.jbei.ice.lib.common.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.MessagingException;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;

import org.slf4j.LoggerFactory;

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
        if (e instanceof MessagingException || e instanceof ControllerException || e instanceof DAOException) {
            // if error is "Can't send email", there is no need to try to send email
            return;
        }

        String value = Utils.getConfigValue(ConfigurationKey.SEND_EMAIL_ON_ERRORS);

        if (value != null && value.equalsIgnoreCase("YES")) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String body = "System Time: " + dateFormatter.format((new Date())) + "\n\n";
            body = body + message;
            String subject = "Error";
            Emailer.error(Utils.getConfigValue(ConfigurationKey.ERROR_EMAIL_EXCEPTION_PREFIX) + " " + subject, body);
        }
    }

    private static void sendEmail(String msg) {
        sendEmail(msg, new Exception("Error"));
    }
}
