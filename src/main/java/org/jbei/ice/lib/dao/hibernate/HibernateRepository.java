package org.jbei.ice.lib.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.dao.IRepository;
import org.jbei.ice.lib.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Hibernate Persistence
 *
 * @author Hector Plahar, Zinovii Dmytriv, Timothy Ham
 */

public class HibernateRepository<T extends IModel> implements IRepository {

    /**
     * Start a new Hibernate {@link Session}.
     *
     * @return {@link Session}
     */
    protected static Session newSession() {
        return HibernateHelper.newSession();
    }

    /**
     * Delete an {@link IModel} object from the database.
     *
     * @param model model to delete
     * @throws DAOException
     */
    protected void delete(T model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to delete null model!");
        }

        Session session = newSession();

        try {
            session.getTransaction().begin();
            session.delete(model);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("dbDelete failed!", e);
        } catch (Exception e) {
            // Something really bad happened.
            session.getTransaction().rollback();
            Logger.error(e);
            resetSessionFactory(session);
            throw new DAOException("Unknown database exception ", e);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Saves or updates an {@link IModel} object into the database.
     *
     * @param model {@link IModel} object to save
     * @return Object saved object
     * @throws DAOException in the event of a problem saving or null model parameter
     */
    protected T saveOrUpdate(T model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to save null model!");
        }

        Session session = newSession();
        try {
            session.getTransaction().begin();
            session.saveOrUpdate(model);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            session.getTransaction().rollback();
            Logger.error(e1);
            resetSessionFactory(session);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession(session);
        }

        return model;
    }

    /**
     * Saves or updates an {@link IModel} object into the database.
     *
     * @param model {@link IModel} object to save
     * @return Object saved object
     * @throws DAOException in the event of a problem saving or null model parameter
     */
    protected T save(T model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to save null model!");
        }

        Session session = newSession();
        try {
            session.getTransaction().begin();
            session.save(model);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            session.getTransaction().rollback();
            Logger.error(e1);
            resetSessionFactory(session);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession(session);
        }

        return model;
    }

    /**
     * Retrieve an {@link IModel} object from the database by Class and database id.
     *
     * @param theClass
     * @param id
     * @return IModel object from the database.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    protected T get(Class<T> theClass, long id) throws DAOException {
        T result = null;
        Session session = newSession();

        try {
            session.getTransaction().begin();
            result = (T) session.get(theClass, id);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("dbGet failed for " + theClass.getCanonicalName() + " and id="
                                           + id, e);
        } catch (Exception e1) {
            // Something really bad happened.
            session.getTransaction().rollback();
            Logger.error(e1);
            resetSessionFactory(session);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession(session);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    protected T getByUUID(Class<T> theClass, String uuid) throws DAOException {
        T result;

        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery("from " + theClass.getName() + " where uuid = :uuid");
            query.setString("uuid", uuid);
            result = (T) query.uniqueResult();
            session.getTransaction().commit();

        } catch (HibernateException e) {
            throw new DAOException("dbGet failed for " + theClass.getCanonicalName() + " and uuid="
                                           + uuid, e);
        } catch (Exception e1) {
            // Something really bad happened.
            session.getTransaction().rollback();
            Logger.error(e1);
            resetSessionFactory(session);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession(session);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<T> retrieveAll(Class<T> theClass) throws DAOException {
        Session session = newSession();

        try {
            List<T> results;
            session.getTransaction().begin();
            Query query = session.createQuery("from " + theClass.getName());
            results = new ArrayList<T>(query.list());
            session.getTransaction().commit();
            return results;
        } catch (HibernateException he) {
            session.getTransaction().rollback();
            throw new DAOException("retrieve all failed for " + theClass.getCanonicalName(), he);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Disconnect the session and reset the SessionFactory.
     *
     * @param session reference to session being disconnected and closed
     */
    private static void resetSessionFactory(Session session) {
        Logger.error("Closing session factory in DAO.java.");
        session.disconnect();
        session.close();
        session.getSessionFactory().close();
    }

    protected void closeSession(Session session) {
        if (session != null && session.isOpen())
            session.close();
    }
}
