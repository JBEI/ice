package org.jbei.ice.server.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.jbei.ice.lib.logging.Logger;

/**
 * Helper class to Initialize Hibernate, and obtain new sessions.
 * 
 * @author Zinovii Dmytriv, Timothy Ham, Hector Plahar
 */
public class HibernateHelper {

    // thread safe global object that is instantiated once
    private static SessionFactory sessionFactory;

    // singleton
    private HibernateHelper() {
    }

    /**
     * Open a new {@link Session} from the sessionFactory.
     * 
     * @return New Hibernate {@link Session}.
     */
    public static Session newSession() {
        return getSessionFactory().openSession();
    }

    /**
     * Initialize a in-memory mock database for testing.
     */
    public static void initializeMock() {
        initialize(Type.MOCK);
    }

    private static synchronized void initialize(Type type) {
        if (sessionFactory == null) { // initialize only when there is no previous sessionFactory
            if (type == Type.MOCK) {
                try {
                    sessionFactory = new AnnotationConfiguration().configure(
                        "mock_hibernate.cfg.xml").buildSessionFactory();
                } catch (Throwable e) {
                    String msg = "Could not initialize hibernate!!!";
                    Logger.error(msg, e);
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    // Create the SessionFactory from hibernate.cfg.xml
                    sessionFactory = new AnnotationConfiguration().configure()
                            .buildSessionFactory();
                } catch (Throwable e) {
                    String msg = "Could not initialize hibernate!!!";
                    Logger.error(msg, e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Retrieve the {@link SessionFactory}.
     * 
     * @return Hibernate sessionFactory
     */
    private static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            initialize(Type.NORMAL);
        }
        return sessionFactory;
    }

    /**
     * initialization types
     */
    private enum Type {
        NORMAL, MOCK;
    }

}
