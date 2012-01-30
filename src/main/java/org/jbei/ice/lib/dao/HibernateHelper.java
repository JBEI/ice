package org.jbei.ice.lib.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.jbei.ice.lib.logging.Logger;

/**
 * Helper class to Initialize Hibernate, and obtain new sessions.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 * 
 */
public class HibernateHelper {
    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
        } catch (Throwable e) {
            String msg = "Could not initialize hibernate!!!";
            Logger.error(msg, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the {@link SessionFactory}.
     * 
     * @return Hibernate sessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Open a new {@link Session} from the sessionFactory.
     * 
     * @return New Hibernate {@link Session}.
     */
    public static Session newSession() {
        return getSessionFactory().openSession();
    }
}
