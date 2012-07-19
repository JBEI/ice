package org.jbei.ice.server.servlet;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;
import org.jbei.ice.lib.utils.UtilityException;

/**
 * Ice servlet context listener for running initializing
 * and pre-shutdown instructions
 *
 * @author Hector Plahar
 */
public class IceServletContextListener implements ServletContextListener {
    private Thread jobThread;

    public void contextInitialized(ServletContextEvent event) {
        init();
    }

    public void contextDestroyed(ServletContextEvent event) {
        Logger.info("DESTROYING CONTEXT");

        if (jobThread != null) {
            Logger.info("INTERRUPTING JOB THREAD");
            jobThread.interrupt();
        }

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                Logger.info("De-registering JDBC driver: " + driver);
            } catch (SQLException e) {
                Logger.error("Error de-registering driver: " + driver, e);
            }
        }
    }

    /**
     * Start the job cue thread.
     */
    private void initializeQueueingSystem() {
        JobCue jobCue = JobCue.getInstance();
        jobThread = new Thread(jobCue);
        jobThread.setPriority(Thread.MIN_PRIORITY);
        jobThread.start();
    }

    protected void init() {

        initializeQueueingSystem();
        try {
            PopulateInitialDatabase.initializeDatabase();
        } catch (UtilityException e) {
            throw new RuntimeException(e);
        }
    }
}
