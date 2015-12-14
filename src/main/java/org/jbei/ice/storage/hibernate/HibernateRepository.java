package org.jbei.ice.storage.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IRepository;

/**
 * Parent abstract class for Hibernate Persistence
 *
 * @author Hector Plahar
 */
public abstract class HibernateRepository<T extends DataModel> implements IRepository<T> {

    /**
     * Obtain the current hibernate {@link Session}.
     *
     * @return {@link Session}
     */
    protected static Session currentSession() {
        return HibernateUtil.currentSession();
    }

    /**
     * Deletes an {@link DataModel} from the database.
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
     * @param model {@link DataModel} object to create
     * @return Object created {@link DataModel} object
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
     * Retrieve an {@link DataModel} object from the database by Class and database id.
     *
     * @param clazz class type for {@link DataModel}
     * @param id    unique synthetic identifier for {@link DataModel}
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
}
