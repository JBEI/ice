package org.jbei.ice.lib.managers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

public class HibernateHelper {
    public static Session getSession() {
        Session session = (Session) HibernateHelper.session.get();
        if (session == null) {
            session = sessionFactory.openSession();
            HibernateHelper.session.set(session);
        }
        return session;
    }

    private static final ThreadLocal<Session> session = new ThreadLocal<Session>();
    private static final SessionFactory sessionFactory = new AnnotationConfiguration().configure()
            .buildSessionFactory();

}
