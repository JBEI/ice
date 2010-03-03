package org.jbei.ice.lib.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;

import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

public class Logger {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger("org.jbei.ice.system");

    public static void trace(String msg) {
        logger.trace(msg);
    }

    public static void debug(String msg) {
        logger.debug(msg);
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warn(msg);
    }

    @Deprecated
    public static void error(String msg) {
        sendEmail(msg);
        logger.error(msg);
    }

    public static void error(String msg, Throwable e) {
        msg = msg + "\n" + Utils.stackTraceToString(e);
        sendEmail(msg, e);
        logger.error(msg);
    }

    @Deprecated
    public static void fatal(String msg) {
        sendEmail(msg);
        logger.fatal(msg);
    }

    public static void fatal(String msg, Throwable e) {
        msg = msg + "\n" + Utils.stackTraceToString(e);
        sendEmail(msg, e);
        logger.fatal(msg);
    }

    private static void sendEmail(String msg, Throwable e) {
        if (e instanceof MessagingException) {
            // if error is "Can't send email", there is no need to try to send email
        } else {
            if (JbeirSettings.getSetting("SEND_EMAIL_ON_ERRORS").equals("YES")) {

                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String body = "System Time: " + dateFormatter.format((new Date())) + "\n\n";
                body = body + msg;
                String subject = "Error";
                Emailer.error(JbeirSettings.getSetting("ERROR_EMAIL_EXCEPTION_PREFIX") + " "
                        + subject, body);

            }
        }
    }

    private static void sendEmail(String msg) {
        sendEmail(msg, new Exception("Error"));
    }

}
