package org.jbei.ice.lib.managers;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Configuration;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;

public class ConfigurationManager {

    public static Configuration save(Configuration configuration) throws ManagerException {
        if (configuration == null) {
            return null;
        } else {
            try {
                configuration = (Configuration) DAO.save(configuration);
            } catch (DAOException e) {
                throw new ManagerException("Failed to save configuration", e);
            }
            return configuration;
        }
    }

    public static void delete(Configuration configuration) throws ManagerException {
        if (configuration != null) {
            try {
                DAO.delete(configuration);
            } catch (DAOException e) {
                throw new ManagerException("Failed to delete configuration", e);
            }
        }
    }

    public static Configuration get(ConfigurationKey key) throws ManagerException {
        Configuration configuration = null;

        if (key != null) {
            Session session = DAO.newSession();
            try {
                Query query = session.createQuery("from " + Configuration.class.getName()
                        + " where key = :key");
                query.setParameter("key", key);
                Object queryResult = query.uniqueResult();

                if (queryResult != null) {
                    configuration = (Configuration) queryResult;
                }
            } catch (HibernateException e) {
                throw new ManagerException("Failed to get Configuration using key: " + key.name(),
                        e);
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }

            return configuration;
        } else {
            return null;
        }
    }

}