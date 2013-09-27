package org.jbei.ice.lib.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.MessagingException;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.Utils;

import org.slf4j.LoggerFactory;

/**
 * Logger for ICE.
 * <p/>
 * Contains static methods for different log levels, using the log configuration defined in the
 * settings file.
 * <p/>
 * For Errors and Fatal levels, send out an email, according to the configuration.
 *
 * @author Hector Plahar, Timothy Ham
 */
public class Logger {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("org.jbei.ice");

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg
     */
    public static void debug(String msg) {
        LOGGER.debug(msg);
    }

    /**
     * Log a message at the INFO level.
     *
     * @param msg
     */
    public static void info(String msg) {
        LOGGER.info(msg);
    }

    /**
     * Log a message at the WARN level.
     *
     * @param msg
     */
    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param msg
     */
    public static void error(String msg) {
        sendEmail(msg);
        LOGGER.error(msg);
    }

    /**
     * Log a message at the ERROR level, with the Throwable e.
     * <p/>
     * This method is preferred over error with only the message, as the stack trace is included in
     * the generated error message.
     *
     * @param msg
     * @param e
     */
    public static void error(String msg, Throwable e) {
        msg = msg + "\n" + Utils.stackTraceToString(e);
        sendEmail(msg, e);
        LOGGER.error(msg);
    }

    /**
     * Log a Throwable e.
     *
     * @param e
     */
    public static void error(Throwable e) {
        String msg = Utils.stackTraceToString(e);
        sendEmail(msg, e);
        LOGGER.error(msg);
    }

    /**
     * Send an email to the address in the configuration.
     *
     * @param message
     * @param e
     */
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

    /**
     * Send an email to the address in the configuration.
     *
     * @param msg
     */
    private static void sendEmail(String msg) {
        sendEmail(msg, new Exception("Error"));
    }
}
