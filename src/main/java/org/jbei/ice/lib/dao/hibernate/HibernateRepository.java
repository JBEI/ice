package org.jbei.ice.lib.dao.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dao.IRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Parent abstract class for Hibernate Persistence
 *
 * @author Hector Plahar
 */

@SuppressWarnings("unchecked")
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
     */
    public void delete(T object) {
        try {
            currentSession().delete(object);
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    /**
     * Updates an existing object in the database, if found
     *
     * @param object Object to update
     * @return updated object
     */
    public T update(T object) {
        try {
            currentSession().update(object);
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
        return object;
    }


    /**
     * Creates new object in the database
     *
     * @param model {@link IDataModel} object to create
     * @return Object created {@link IDataModel} object
     */
    public T create(T model) {
        try {
            currentSession().save(model);
            return model;
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    /**
     * Retrieve an {@link org.jbei.ice.lib.dao.IDataModel} object from the database by Class and database id.
     *
     * @param clazz class type for {@link IDataModel}
     * @param id    unique synthetic identifier for {@link IDataModel}
     * @return IModel object from the database.
     */
    protected T get(Class<T> clazz, long id) throws DAOException {
        try {
            return currentSession().get(clazz, id);
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Error retrieving " + clazz.getSimpleName() + " with id \"" + id + "\"", e);
        }
    }

    protected T getByUUID(Class<T> theClass, String uuid) throws DAOException {
        T result;
        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + theClass.getName() + " where uuid = :uuid");
            query.setString("uuid", uuid);
            result = (T) query.uniqueResult();

        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
        return result;
    }

    protected List<T> getAll(Class<T> theClass) throws DAOException {
        Session session = currentSession();

        try {
            List<T> results;
            Query query = session.createQuery("from " + theClass.getName());
            results = new ArrayList<T>(query.list());
            return results;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
