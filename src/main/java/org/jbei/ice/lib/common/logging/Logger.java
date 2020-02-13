package org.jbei.ice.lib.common.logging;

import org.slf4j.LoggerFactory;

/**
 * Logger for ICE.
 *
 * @author Hector Plahar
 */
public class Logger {

    private static final ch.qos.logback.classic.Logger LOGGER = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("[ICE]");

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable e) {
        LOGGER.error(message, e);
    }

    public static void error(Throwable e) {
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
}
