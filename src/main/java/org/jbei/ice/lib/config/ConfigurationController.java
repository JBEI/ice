package org.jbei.ice.lib.config;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Configuration;

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
            Configuration configuration = dao.get(Configuration.ConfigurationKey.DATABASE_SCHEMA_VERSION);
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
            Configuration configuration = dao.get(Configuration.ConfigurationKey.DATABASE_SCHEMA_VERSION);
            configuration.setValue(newVersion);
            dao.save(configuration);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }
}
