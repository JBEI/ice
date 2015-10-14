package org.jbei.ice.lib.dao.hibernate;

import org.jbei.ice.lib.common.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * Helper class to Initialize Hibernate, and obtain new sessions.
 *
 * @author Zinovii Dmytriv, Timothy Ham, Hector Plahar
 */
public class HibernateUtil {

    // thread safe global object that is instantiated once
    private static SessionFactory sessionFactory;

    // singleton
    private HibernateUtil() {
    }

    private static String BASE_FILE = "hibernate.cfg.xml";
    private static String MOCK_FILE = "mock.cfg.xml";

    /**
     * Open a new {@link Session} from the sessionFactory.
     * This needs to be closed when done with
     *
     * @return New Hibernate {@link Session}.
     */
    public static Session newSession() {
        return getSessionFactory().openSession();
    }

    /**
     * @return session bound to context.
     */
    protected static Session currentSession() {
        return getSessionFactory().getCurrentSession();
    }

    public static void beginTransaction() {
        if (!getSessionFactory().getCurrentSession().getTransaction().isActive()) {
            getSessionFactory().getCurrentSession().beginTransaction();
        }
    }

    public static void commitTransaction() {
        if (getSessionFactory().getCurrentSession().getTransaction().isActive()) {
            getSessionFactory().getCurrentSession().getTransaction().commit();
        }
    }

    public static void rollbackTransaction() {
        if (getSessionFactory().getCurrentSession().getTransaction().isActive()) {
            getSessionFactory().getCurrentSession().getTransaction().rollback();
        }
    }

    /**
     * Initialize a in-memory mock database for testing.
     */
    public static void initializeMock() {
        initialize(Type.MOCK);
    }

    private static synchronized void initialize(Type type) {
        if (sessionFactory == null) { // initialize only when there is no previous sessionFactory
            Logger.info("Initializing session factory for type " + type.name());
            Configuration configuration = new Configuration().configure(BASE_FILE);
            try {
                if (type == Type.MOCK) {
                    configuration.configure(MOCK_FILE);
                } else {
                    String[] properties =
                            {"hibernate.connection.username",
                             "hibernate.connection.url",
                             "hibernate.connection.driver_class",
                             "hibernate.connection.password",
                             "hibernate.dialect"};

                    for(int i = 0; i < properties.length; i++){
                        String key = properties[i];
                        String value = System.getProperty(properties[i]);

                        if(value != null){
                            Logger.info("Set " + key + ": " + value);
                            configuration.setProperty(key, value);
                        }
                    }
                }

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
                        configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Retrieve the {@link SessionFactory}.
     *
     * @return Hibernate sessionFactory
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            initialize(Type.NORMAL);
        }
        return sessionFactory;
    }

    public static void close() {
        currentSession().disconnect();
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            Logger.info("Closing session factory");
            sessionFactory.close();
        }
    }

    /**
     * initialization types
     */
    private enum Type {
        NORMAL, MOCK;
    }
}
