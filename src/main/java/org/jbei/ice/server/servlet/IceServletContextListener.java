package org.jbei.ice.server.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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

    public void contextInitialized(ServletContextEvent event) {
        init();
    }

    public void contextDestroyed(ServletContextEvent event) {
    }

    /**
     * Start the job cue thread.
     */
    private void initializeQueueingSystem() {
        JobCue jobCue = JobCue.getInstance();
        Thread jobThread = new Thread(jobCue);
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
