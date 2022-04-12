package org.jbei.ice.servlet;

import org.jbei.ice.ApplicationInitialize;
import org.jbei.ice.executor.IceExecutorService;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.nio.file.Path;

/**
 * Ice servlet context listener for running initializing
 * and pre-shutdown instructions
 *
 * @author Hector Plahar
 */
public class IceServletContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
        Path path = ApplicationInitialize.configure();
        if (path == null)
            return;

        try {
            HibernateConfiguration.beginTransaction();
            ApplicationInitialize.start(path);
            HibernateConfiguration.commitTransaction();
        } catch (Throwable e) {
            Logger.logErrorOnly(e);
            HibernateConfiguration.rollbackTransaction();
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        // shutdown executor service
        IceExecutorService.getInstance().stopService();
        HibernateConfiguration.close();
    }
}
