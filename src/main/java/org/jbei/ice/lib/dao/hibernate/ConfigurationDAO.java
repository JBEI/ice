package org.jbei.ice.lib.dao.hibernate;

import java.util.List;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.models.Configuration;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Configuration> getAll() {
        return super.getAll(Configuration.class);
    }
}

