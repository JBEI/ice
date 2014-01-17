package org.jbei.ice.servlet;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jbei.ice.ApplicationController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;

import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

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

        closeSessionFactory(HibernateHelper.getSessionFactory());

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
        if (factory instanceof SessionFactoryImpl) {
            SessionFactoryImpl sf = (SessionFactoryImpl) factory;
            ConnectionProvider conn = sf.getConnectionProvider();
            if (conn instanceof C3P0ConnectionProvider) {
                ((C3P0ConnectionProvider) conn).close();
            }
        }
        factory.close();
    }

    protected void init() {
        try {
            HibernateHelper.beginTransaction();
            PopulateInitialDatabase.initializeDatabase();
            ApplicationController.initialize();
            HibernateHelper.commitTransaction();

            checkBlast();
        } catch (Throwable e) {
            HibernateHelper.rollbackTransaction();
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected void checkBlast() {
        Logger.info("Checking blast database");
        ApplicationController.scheduleBlastIndexRebuildTask(false);
    }
}
