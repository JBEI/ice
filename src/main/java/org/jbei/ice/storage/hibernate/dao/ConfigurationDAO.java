package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.ConfigurationModel;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * Manage {@link ConfigurationModel} objects in the database.
 *
 * @author Timothy Ham, Hector Plahar
 */
public class ConfigurationDAO extends HibernateRepository<ConfigurationModel> {

    @Override
    public ConfigurationModel get(long id) {
        return get(ConfigurationModel.class, id);
    }

    /**
     * Retrieve the {@link ConfigurationModel} object with the given {@link org.jbei.ice.lib.dto.ConfigurationKey}.
     *
     * @param key unique configuration key for retrieval
     * @return Configuration
     * @throws DAOException
     */
    public ConfigurationModel get(ConfigurationKey key) {
        return get(key.name());
    }

    public ConfigurationModel get(String key) {
        try {
            CriteriaQuery<ConfigurationModel> query = getBuilder().createQuery(ConfigurationModel.class);
            Root<ConfigurationModel> root = query.from(ConfigurationModel.class);
            query.where(getBuilder().equal(root.get("key"), key));
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException("Failed to get Configuration using key: " + key, e);
        }
    }
}

