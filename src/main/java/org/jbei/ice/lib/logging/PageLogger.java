package org.jbei.ice.lib.logging;

public class PageLogger {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger("org.jbei.ice.web");

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

    public static void error(String msg) {
        logger.error(msg);
    }

    public static void fatal(String msg) {
        logger.fatal(msg);
    }
}
