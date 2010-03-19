package org.jbei.ice.lib.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DAO {

    public static Session newSession() {
        return HibernateHelper.newSession();
    }

    public static void delete(IModel model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to delete null model!");
        }

        Session session = newSession();

        try {
            session.getTransaction().begin(); // Do not assign transaction to value

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
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public static Object save(IModel model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to save null model!");
        }

        Object result = null;

        Session session = newSession();
        try {
            session.getTransaction().begin(); // Do not assign transaction to value
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
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    public static Object get(Class<? extends IModel> theClass, int id) throws DAOException {
        Object result = null;

        Session session = newSession();

        try {
            result = session.load(theClass, id);
        } catch (HibernateException e) {
            throw new DAOException("dbGet failed for " + theClass.getCanonicalName() + " and id="
                    + id, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    public static Object merge(IModel model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to merge null model!");
        }

        Object result = null;

        Session session = newSession();
        try {
            result = session.merge(model);
        } catch (HibernateException e) {
            throw new DAOException("Merge failed: ", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }
}
