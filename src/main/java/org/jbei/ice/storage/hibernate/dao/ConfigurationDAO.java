package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Configuration;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Manage {@link Configuration} objects in the database.
 *
 * @author Timothy Ham, Hector Plahar
 */
public class ConfigurationDAO extends HibernateRepository<Configuration> {

    @Override
    public Configuration get(long id) {
        return get(Configuration.class, id);
    }

    /**
     * Retrieve the {@link Configuration} object with the given {@link org.jbei.ice.lib.dto.ConfigurationKey}.
     *
     * @param key unique configuration key for retrieval
     * @return Configuration
     * @throws DAOException
     */
    public Configuration get(ConfigurationKey key) {
        return get(key.name());
    }

    public Configuration get(String key) {
        try {
            CriteriaQuery<Configuration> query = getBuilder().createQuery(Configuration.class);
            Root<Configuration> root = query.from(Configuration.class);
            query.where(getBuilder().equal(root.get("key"), key));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to get Configuration using key: " + key, e);
        }
    }
}

