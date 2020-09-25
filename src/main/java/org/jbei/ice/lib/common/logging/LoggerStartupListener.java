package org.jbei.ice.lib.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;

public class LoggerStartupListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private boolean started = false;

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext context) {
    }

    @Override
    public void onReset(LoggerContext context) {
    }

    @Override
    public void onStop(LoggerContext context) {
    }

    @Override
    public void onLevelChange(Logger logger, Level level) {
    }

    private String fetchLogDirectory() {
        // check environ variable
        String propertyHome = System.getenv("ICE_LOG_HOME");
        if (!StringUtils.isBlank(propertyHome))
            return propertyHome;

        // check system property (-D in startup script)
        propertyHome = System.getProperty("ICE_LOG_HOME");
        if (!StringUtils.isEmpty(propertyHome))
            return propertyHome;

        // check tmp directory
        propertyHome = System.getProperty("java.io.tmpdir");
        if (!StringUtils.isEmpty(propertyHome) && !Files.isDirectory(Paths.get(propertyHome)))
            return propertyHome;

        // just stash it in home
        return System.getProperty("user.home");
    }

    @Override
    public void start() {
        if (started)
            return;

        Context context = getContext();
        context.putProperty("LOG_PATH", fetchLogDirectory());
        started = true;
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
