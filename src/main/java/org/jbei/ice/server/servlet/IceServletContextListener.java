package org.jbei.ice.server.servlet;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;

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
        Logger.info("Destroying Servlet Context");

        // shutdown executor service
        IceExecutorService.getInstance().stopService();

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

    protected void init() {
        try {
            HibernateHelper.getSessionFactory().getCurrentSession().beginTransaction();
            PopulateInitialDatabase.initializeDatabase();
            ApplicationController.upgradeDatabaseIfNecessary();
            HibernateHelper.getSessionFactory().getCurrentSession().getTransaction().commit();
            // TODO : rollback
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
