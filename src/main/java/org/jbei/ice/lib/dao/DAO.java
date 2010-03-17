package org.jbei.ice.lib.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DAO {
    public static Session getSession() {
        return HibernateHelper.getSession();
    }

    public static void delete(IModel model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to delete null model!");
        }

        Session session = getSession();

        try {
            session.beginTransaction(); // Do not assign transaction to value

            try {
                session.delete(model);
                session.getTransaction().commit();
            } catch (org.hibernate.NonUniqueObjectException e) {
                model = (IModel) session.merge(model);
                session.delete(model);
                session.getTransaction().commit();
            }
        } catch (HibernateException e) {
            session.getTransaction().rollback();

            throw new DAOException("dbDelete failed!", e);
        }
    }

    public static Object save(IModel model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to save null model!");
        }

        Object result = null;

        Session session = getSession();
        try {
            session.beginTransaction(); // Do not assign transaction to value

            try {
                session.saveOrUpdate(model);
                session.getTransaction().commit();
            } catch (org.hibernate.NonUniqueObjectException e) {
                session.merge(model);
                session.getTransaction().commit();
            }

            result = model;
        } catch (HibernateException e) {
            session.getTransaction().rollback();

            throw new DAOException("dbSave failed!", e);
        }

        return result;
    }

    public static Object get(Class<? extends IModel> theClass, int id) throws DAOException {
        Object result = null;

        Session tempSession = getSession();

        try {
            result = tempSession.load(theClass, id);
        } catch (HibernateException e) {
            throw new DAOException("dbGet failed for " + theClass.getCanonicalName() + " and id="
                    + id, e);
        }

        return result;
    }

    public static Object merge(IModel model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to merge null model!");
        }

        Object result = null;

        Session session = getSession();
        try {
            result = session.merge(model);
        } catch (HibernateException e) {
            throw new DAOException("Merge failed: ", e);
        }

        return result;
    }
}
