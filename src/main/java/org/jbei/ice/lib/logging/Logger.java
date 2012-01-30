package org.jbei.ice.lib.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;

import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

/**
 * Logger for gd-ice.
 * <p>
 * Contains static methods for different log levels, using the log configuration defined in the
 * settings file.
 * <p>
 * For Errors and Fatal levels, send out an email, according to the configuration.
 * 
 * @author Timothy Ham
 * 
 */
public class Logger {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger("org.jbei.ice.system");

    /**
     * Log a message at the TRACE level.
     * 
     * @param msg
     */
    public static void trace(String msg) {
        logger.trace(msg);
    }

    /**
     * Log a message at the DEBUG level.
     * 
     * @param msg
     */
    public static void debug(String msg) {
        logger.debug(msg);
    }

    /**
     * Log a message at the INFO level.
     * 
     * @param msg
     */
    public static void info(String msg) {
        logger.info(msg);
    }

    /**
     * Log a message at the WARN level.
     * 
     * @param msg
     */
    public static void warn(String msg) {
        logger.warn(msg);
    }

    /**
     * Log a message at the ERROR level.
     * 
     * @param msg
     */
    public static void error(String msg) {
        sendEmail(msg);
        logger.error(msg);
    }

    /**
     * Log a message at the ERROR level, with the Throwable e.
     * <p>
     * This method is preferred over error with only the message, as the stack trace is included in
     * the generated error message.
     * 
     * @param msg
     * @param e
     */
    public static void error(String msg, Throwable e) {
        msg = msg + "\n" + Utils.stackTraceToString(e);
        sendEmail(msg, e);
        logger.error(msg);
    }

    /**
     * Log a Throwable e.
     * 
     * @param e
     */
    public static void error(Throwable e) {
        String msg = Utils.stackTraceToString(e);
        sendEmail(msg, e);
        logger.error(msg);
    }

    /**
     * Log a message at the fatal level.
     * 
     * @param msg
     */

    public static void fatal(String msg) {
        sendEmail(msg);
        logger.fatal(msg);
    }

    /**
     * Log a message at the fatal level, with Throwable e.
     * 
     * @param msg
     * @param e
     */
    public static void fatal(String msg, Throwable e) {
        msg = msg + "\n" + Utils.stackTraceToString(e);
        sendEmail(msg, e);
        logger.fatal(msg);
    }

    /**
     * Send an email to the address in the configuration.
     * 
     * @param message
     * @param e
     */
    private static void sendEmail(String message, Throwable e) {
        if (e instanceof MessagingException) {
            // if error is "Can't send email", there is no need to try to send email
        } else {
            if (JbeirSettings.getSetting("SEND_EMAIL_ON_ERRORS").equals("YES")) {

                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String body = "System Time: " + dateFormatter.format((new Date())) + "\n\n";
                body = body + message;
                String subject = "Error";
                Emailer.error(JbeirSettings.getSetting("ERROR_EMAIL_EXCEPTION_PREFIX") + " "
                        + subject, body);

            }
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
