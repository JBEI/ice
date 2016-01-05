package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Configuration;

import java.util.List;

/**
 * Manage {@link Configuration} objects in the database.
 *
 * @author Timothy Ham, Hector Plahar
 */
public class ConfigurationDAO extends HibernateRepository<Configuration> {

    /**
     * Retrieve the {@link Configuration} object with the given {@link org.jbei.ice.lib.dto.ConfigurationKey}.
     *
     * @param key
     * @return Configuration
     * @throws DAOException
     */
    public Configuration get(ConfigurationKey key) throws DAOException {
        return get(key.name());
    }

    public Configuration get(String key) throws DAOException {
        Configuration configuration = null;
        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + Configuration.class.getName() + " where key = :key");
            query.setParameter("key", key);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                configuration = (Configuration) queryResult;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to get Configuration using key: " + key, e);
        }

        return configuration;
    }

    @Override
    public Configuration get(long id) {
        return get(Configuration.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Configuration> getAll() {
        Session session = currentSession();

        try {
            Query query = session.createQuery("from " + Configuration.class.getName());
            return query.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}

