package org.jbei.ice.server.dao.hibernate;

import org.jbei.ice.lib.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

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
            Configuration configuration;
            try {
                if (type == Type.MOCK) {
                    configuration = new Configuration().configure("mock_hibernate.cfg.xml");
                } else {
                    // Create the SessionFactory from hibernate.cfg.xml
                    configuration = new Configuration().configure();
                }

                ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(
                        configuration.getProperties()).buildServiceRegistry();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);

                //            Session session = newSession();
//            FullTextSession fullTextSession = Search.getFullTextSession(session);
//            fullTextSession.createIndexer().start();

            } catch (Throwable e) {
                String msg = "Could not initialize hibernate!!!";
                Logger.error(msg, e);
                throw new RuntimeException(e);
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
