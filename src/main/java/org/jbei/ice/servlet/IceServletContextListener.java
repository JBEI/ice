package org.jbei.ice.servlet;

import org.hibernate.SessionFactory;
import org.jbei.ice.ApplicationInitialize;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.storage.hibernate.HibernateUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

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

        closeSessionFactory(HibernateUtil.getSessionFactory());

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

    // work
    private void closeSessionFactory(SessionFactory factory) {
        factory.close();
    }

    protected void init() {
        try {
            HibernateUtil.beginTransaction();
            ApplicationInitialize.startUp();
            HibernateUtil.commitTransaction();
        } catch (Throwable e) {
            HibernateUtil.rollbackTransaction();
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
