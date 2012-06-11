package org.jbei.ice.server.dao.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.server.dao.IRepository;

/**
 * Hibernate Persistence
 * 
 * @author Hector Plahar, Zinovii Dmytriv, Timothy Ham
 */

public class HibernateRepository implements IRepository {

    /**
     * Start a new Hibernate {@link Session}.
     * 
     * @return {@link Session}
     */
    public static Session newSession() {
        return HibernateHelper.newSession();
    }

    /**
     * Delete an {@link IModel} object from the database.
     * 
     * @param model
     * @throws DAOException
     */
    public void delete(IModel model) throws DAOException {
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
        } catch (Exception e1) {
            // Something really bad happened.
            session.getTransaction().rollback();
            e1.printStackTrace();
            resetSessionFactory(session);
            throw new DAOException("Unkown database exception ", e1);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Saves or updates an {@link IModel} object into the database.
     * 
     * @param model
     *            {@link IModel} object to save
     * @return Object saved object
     * @throws DAOException
     *             in the event of a problem saving or null model parameter
     */
    public Object save(IModel model) throws DAOException {
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
        } catch (Exception e1) {
            session.getTransaction().rollback();
            Logger.error(e1);
            resetSessionFactory(session);
            throw new DAOException("Unkown database exception ", e1);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Retrieve an {@link IModel} object from the database by Class and database id.
     * 
     * @param theClass
     * @param id
     * @return IModel object from the database.
     * @throws DAOException
     */
    public Object get(Class<? extends IModel> theClass, long id) throws DAOException {
        Object result = null;

        Session session = newSession();

        try {
            session.getTransaction().begin(); // Do not assign transaction to value
            result = session.get(theClass, id);
        } catch (HibernateException e) {
            throw new DAOException("dbGet failed for " + theClass.getCanonicalName() + " and id="
                    + id, e);
        } catch (Exception e1) {
            // Something really bad happened.
            session.getTransaction().rollback();
            Logger.error(e1);
            resetSessionFactory(session);
            throw new DAOException("Unkown database exception ", e1);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Disconnect the session and reset the SessionFactory.
     * 
     * @param session
     */
    private static void resetSessionFactory(Session session) {
        Logger.error("Closing session factory in DAO.java.");
        session.disconnect();
        session.close();
        session.getSessionFactory().close();
    }

}
