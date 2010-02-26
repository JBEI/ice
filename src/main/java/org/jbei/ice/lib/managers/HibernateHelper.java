package org.jbei.ice.lib.managers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.jbei.ice.lib.logging.Logger;

public class HibernateHelper {
    private static Session session = null;

    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
        } catch (Throwable e) {
            String msg = "Could not initialize hibernate!!!";
            Logger.error(msg);
            throw new RuntimeException(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Session getSession() {
        if (session == null) {
            session = getSessionFactory().openSession();
        } else if (!session.isOpen()) {
            session = getSessionFactory().openSession();
        }
        return session;
    }

}
