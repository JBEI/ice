package org.jbei.ice.lib.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dao.IRepository;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Hibernate Persistence
 *
 * @author Hector Plahar, Zinovii Dmytriv, Timothy Ham
 */

public abstract class HibernateRepository<T extends IDataModel> implements IRepository<T> {

    /**
     * Obtain the current hibernate {@link Session}.
     *
     * @return {@link Session}
     */
    protected static Session currentSession() {
        return HibernateUtil.currentSession();
    }

    /**
     * Deletes an {@link IDataModel} from the database.
     *
     * @param object model to delete
     * @throws DAOException on Hibernate Exception or invalid parameter
     */
    public void delete(T object) {
        try {
            currentSession().delete(object);
        } catch (HibernateException e) {
            throw new DAOException("dbDelete failed!", e);
        }
    }

    /**
     * Updates an existing object in the database
     *
     * @param object Object to update
     * @return updated object
     * @throws DAOException on Hibernate Exception or invalid parameter
     */
    public T update(T object) {
        try {
            currentSession().update(object);
        } catch (HibernateException e) {
            throw new DAOException("dbDelete failed!", e);
        }
        return object;
    }


    /**
     * Creates new object in the database
     *
     * @param model {@link IDataModel} object to create
     * @return Object created {@link IDataModel} object
     * @throws DAOException in the event of a problem saving or invalid parameter
     */
    public T create(T model) {
        try {
            currentSession().save(model);
        } catch (HibernateException e) {
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            throw new DAOException("Unknown database exception ", e1);
        }

        return model;
    }

    /**
     * Retrieve an {@link org.jbei.ice.lib.dao.IDataModel} object from the database by Class and database id.
     *
     * @param clazz class type for {@link IDataModel}
     * @param id    unique synthetic identifier for {@link IDataModel}
     * @return IModel object from the database.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    protected T get(Class<T> clazz, long id) throws DAOException {
        try {
            return (T) currentSession().get(clazz, id);
        } catch (HibernateException e) {
            throw new DAOException("Could not retrieve " + clazz.getSimpleName() + " with id \"" + id + "\"", e);
        }
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
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected List<T> getAll(Class<T> theClass) throws DAOException {
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
}
