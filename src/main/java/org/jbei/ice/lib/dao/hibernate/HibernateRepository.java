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
    protected static Session currentSession() {
        return HibernateHelper.currentSession();
    }

    /**
     * Delete an {@link IModel} object from the database.
     *
     * @param model model to delete
     * @throws DAOException
     */
    public void delete(T model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to delete null model!");
        }

        Session session = currentSession();

        try {
            session.delete(model);
        } catch (HibernateException e) {
            throw new DAOException("dbDelete failed!", e);
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException("Unknown database exception ", e);
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

        Session session = currentSession();
        try {
            session.saveOrUpdate(model);
        } catch (HibernateException e) {
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
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
    public T save(T model) throws DAOException {
        if (model == null) {
            throw new DAOException("Failed to save null model!");
        }

        Session session = currentSession();
        try {
            session.save(model);
        } catch (HibernateException e) {
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
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

        T result;
        Session session = currentSession();

        try {
            result = (T) session.get(theClass, id);
        } catch (HibernateException e) {
            throw new DAOException("dbGet failed for " + theClass.getCanonicalName() + " and id=" + id, e);
        } catch (Exception e1) {
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    protected T getByUUID(Class<T> theClass, String uuid) throws DAOException {
        T result;

        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + theClass.getName() + " where uuid = :uuid");
            query.setString("uuid", uuid);
            result = (T) query.uniqueResult();

        } catch (HibernateException e) {
            throw new DAOException("dbGet failed for " + theClass.getCanonicalName() + " and uuid=" + uuid, e);
        } catch (Exception e1) {
            // Something really bad happened.
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<T> retrieveAll(Class<T> theClass) throws DAOException {
        Session session = currentSession();

        try {
            List<T> results;
            Query query = session.createQuery("from " + theClass.getName());
            results = new ArrayList<T>(query.list());
            return results;
        } catch (HibernateException he) {
            throw new DAOException("retrieve all failed for " + theClass.getCanonicalName(), he);
        }
    }

    protected void closeSession() {
    }
}
