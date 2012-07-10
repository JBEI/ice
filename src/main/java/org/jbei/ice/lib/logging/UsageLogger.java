package org.jbei.ice.lib.logging;

/**
 * Logger for gd-ice usage.
 * <p>
 * Generate a separate log file for usage data in order to generate statistics.
 * 
 * @author Timothy Ham
 * 
 */
public class UsageLogger {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger("org.jbei.ice.usage");

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
        logger.error(msg);
    }

    /**
     * Log a message at the FATAL level.
     * 
     * @param msg
     */
    public static void fatal(String msg) {
        logger.fatal(msg);
    }
}
