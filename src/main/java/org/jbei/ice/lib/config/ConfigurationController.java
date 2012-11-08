package org.jbei.ice.lib.config;

import java.util.HashMap;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Configuration;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.shared.dto.ConfigurationKey;

/**
 * @author Hector Plahar
 */
public class ConfigurationController {
    private final ConfigurationDAO dao;

    public ConfigurationController() {
        dao = new ConfigurationDAO();
    }

    public String retrieveDatabaseVersion() throws ControllerException {

        try {
            Configuration configuration = dao.get(ConfigurationKey.DATABASE_SCHEMA_VERSION);
            if (configuration == null)
                throw new ControllerException("Could not retrieve database schema record");

            return configuration.getValue();
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public void updateDatabaseVersion(String newVersion) throws ControllerException {
        try {
            Configuration configuration = dao.get(ConfigurationKey.DATABASE_SCHEMA_VERSION);
            configuration.setValue(newVersion);
            dao.save(configuration);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    // upgrade config from pre version 3.3.0. goes through the propery file and just
    // saves the value in the database
    public void upgradeConfiguration() throws ControllerException {
        Logger.info("Upgrading configuration");
        try {
            for (ConfigurationKey type : ConfigurationKey.values()) {
                String value;
                try {
                    value = JbeirSettings.getSetting(type.name());
                } catch (Exception e) {
                    Logger.warn("Skipping adding " + type.name() + " to configuration database ");
                    continue;
                    // assuming it is alread in the db  e.g. plasmid storage types
                }

                Configuration config = dao.get(type);
                if (config == null)
                    config = new Configuration(type.name(), value);
                else
                    config.setValue(value);
                Logger.info("Adding {key, value} -> " + type.name() + ", " + value);
                dao.save(config);
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public String getPropertyValue(ConfigurationKey key) throws ControllerException {

        try {
            Configuration config = dao.get(key);
            if (config == null)
                return null;
            return config.getValue();
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public String getPropertyValue(String key) throws ControllerException {
        try {
            Configuration config = dao.get(key);
            if (config == null)
                throw new ControllerException("Could not retrieve config with key " + key);
            return config.getValue();
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public HashMap<String, String> retrieveSystemSettings() throws ControllerException {
        try {
            HashMap<String, String> results = new HashMap<String, String>();
            for (Configuration configuration : dao.getAllSettings()) {
                results.put(configuration.getKey(), configuration.getValue());
            }
            return results;
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public void setPropertyValue(ConfigurationKey key, String value) throws ControllerException {
        try {
            Configuration configuration = dao.get(key);
            if (configuration == null) {
                configuration = new Configuration();
                configuration.setKey(key.toString());
            }

            configuration.setValue(value);
            dao.save(configuration);
        } catch (DAOException de) {
            Logger.error(de);
            throw new ControllerException();
        }

    }
}
