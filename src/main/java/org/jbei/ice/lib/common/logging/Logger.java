package org.jbei.ice.lib.common.logging;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.email.EmailFactory;
import org.jbei.ice.lib.utils.Utils;
import org.slf4j.LoggerFactory;

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

    public static boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
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
        final String sendEmailConfig;
        final String emailPrefix;
        if (e instanceof HibernateException) {
            return;
        }

        // A transaction may not be started, or is already rolled back. Must wrap calls here
        //  in a try-catch to avoid uncaught errors in error-handling code.
        try {
            sendEmailConfig = Utils.getConfigValue(ConfigurationKey.SEND_EMAIL_ON_ERRORS);
            emailPrefix =  Utils.getConfigValue(ConfigurationKey.ERROR_EMAIL_EXCEPTION_PREFIX);
        } catch (Throwable t) {
            LOGGER.error("Cannot load email configuration to mail errors!", t);
            return;
        }

        // Do not allow errors constructing the email to abort error handler logging
        try {
            if (sendEmailConfig != null && sendEmailConfig.equalsIgnoreCase("YES")) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String body = "System Time: " + dateFormatter.format((new Date())) + "\n\n";
                body = body + message;
                String subject = "Error";
                EmailFactory.getEmail().sendError(emailPrefix + " " + subject, body);
            }
        } catch(Throwable t) {
            LOGGER.error("Failed to build error mail!", t);
        }
    }

    private static void sendEmail(String msg) {
        sendEmail(msg, new Exception("Error"));
    }
}
