package org.jbei.ice.lib.config;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Configuration;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

/**
 * Manage {@link Configuration} objects in the database.
 *
 * @author Timothy Ham
 */
public class ConfigurationDAO extends HibernateRepository {

    /**
     * Save the given {@link Configuration} object in the database.
     *
     * @param configuration
     * @return Saved Configuration.
     * @throws DAOException
     */
    public Configuration save(Configuration configuration) throws DAOException {
        return (Configuration) super.saveOrUpdate(configuration);
    }

    /**
     * Retrieve the {@link Configuration} object with the given {@link ConfigurationKey}.
     *
     * @param key
     * @return Configuration
     * @throws DAOException
     */
    public Configuration get(ConfigurationKey key) throws DAOException {
        Configuration configuration = null;
        Session session = newSession();

        try {
            Query query = session.createQuery("from " + Configuration.class.getName()
                                                      + " where key = :key");
            query.setParameter("key", key);
            Object queryResult = query.uniqueResult();

            if (queryResult != null) {
                configuration = (Configuration) queryResult;
            }
        } catch (HibernateException e) {
            throw new DAOException("Failed to get Configuration using key: " + key.name(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return configuration;
    }
}