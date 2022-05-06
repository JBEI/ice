package org.jbei.ice.servlet;

import org.jbei.ice.ApplicationInitialize;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.executor.IceExecutorService;
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
            ApplicationInitialize.startUp(path);
            HibernateConfiguration.commitTransaction();
        } catch (Throwable e) {
            HibernateConfiguration.rollbackTransaction();
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        Logger.info("Destroying Servlet Context");

        // shutdown executor service
        IceExecutorService.getInstance().stopService();
        HibernateConfiguration.close();
    }
}
