package org.jbei.ice.lib.config;

import java.util.ArrayList;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.ConfigurationDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.models.Configuration;

/**
 * @author Hector Plahar
 */
public class ConfigurationController {

    private final ConfigurationDAO dao;

    public ConfigurationController() {
        dao = DAOFactory.getConfigurationDAO();
    }

    public String retrieveDatabaseVersion() throws ControllerException {
        Configuration configuration = dao.get(ConfigurationKey.DATABASE_SCHEMA_VERSION);
        if (configuration == null)
            return null;
        return configuration.getValue();
    }

    public void updateDatabaseVersion(String newVersion) throws ControllerException {
        Configuration configuration = dao.get(ConfigurationKey.DATABASE_SCHEMA_VERSION);
        configuration.setValue(newVersion);
        dao.create(configuration);
    }

    public Configuration getConfiguration(ConfigurationKey key) throws ControllerException {
        return dao.get(key);
    }

    public String getPropertyValue(ConfigurationKey key) {
        Configuration config = dao.get(key);
        if (config == null)
            return key.getDefaultValue();
        return config.getValue();
    }

    public String getPropertyValue(String key) throws ControllerException {
        Configuration config = dao.get(key);
        if (config == null)
            throw new ControllerException("Could not retrieve config with key " + key);
        return config.getValue();
    }

    public ArrayList<Setting> retrieveSystemSettings() {
        ArrayList<Setting> settings = new ArrayList<>();
        for (Configuration configuration : dao.getAll()) {
            settings.add(new Setting(configuration.getKey(), configuration.getValue()));
        }
        return settings;
    }

    public void setPropertyValue(ConfigurationKey key, String value) throws ControllerException {
        // TODO : must be an administrator
        Configuration configuration = dao.get(key);
        if (configuration == null) {
            configuration = new Configuration();
            configuration.setKey(key.name());
        }

        configuration.setValue(value);
        dao.create(configuration);
    }

    public Setting updateSetting(String userId, Setting setting) {
        AccountController accountController = new AccountController();
        if (!accountController.isAdministrator(userId))
            return null;

        ConfigurationKey key = ConfigurationKey.valueOf(setting.getKey());
        Configuration configuration =  dao.get(key);
        if(configuration == null) {
            return null;
//            configuration = new Configuration(setting.getKey(), setting.getValue());
//            dao.create(configuration.)
        }
        configuration.setValue(setting.getValue());
        return dao.update(configuration).toDataTransferObject();
    }

    /**
     * Initializes the database on new install
     */
    public void initPropertyValues() throws ControllerException {
        for (ConfigurationKey key : ConfigurationKey.values()) {
            Configuration config = dao.get(key);
            if (config != null || key.getDefaultValue().isEmpty())
                continue;

            Logger.info("Setting value for " + key.toString() + " to " + key.getDefaultValue());
            setPropertyValue(key, key.getDefaultValue());
        }
    }
}
