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

    public String getPropertyValue(ConfigurationKey key) {
        Configuration config = dao.get(key);
        if (config == null)
            return key.getDefaultValue();
        return config.getValue();
    }

    public Setting getPropertyValue(String key) {
        Configuration config = dao.get(key);
        if (config == null)
            return null;
        return config.toDataTransferObject();
    }

    public ArrayList<Setting> retrieveSystemSettings() {
        ArrayList<Setting> settings = new ArrayList<>();
        for (ConfigurationKey key : ConfigurationKey.values()) {
            Configuration configuration = dao.get(key);
            Setting setting;
            if (configuration == null)
                setting = new Setting(key.name(), "");
            else
                setting = new Setting(configuration.getKey(), configuration.getValue());

            settings.add(setting);
        }
        return settings;
    }

    public Configuration setPropertyValue(ConfigurationKey key, String value) {
        Configuration configuration = dao.get(key);
        if (configuration == null) {
            configuration = new Configuration();
            configuration.setKey(key.name());
            configuration.setValue(value);
            return dao.create(configuration);
        }

        configuration.setValue(value);
        return dao.update(configuration);
    }

    public Setting updateSetting(String userId, Setting setting) {
        AccountController accountController = new AccountController();
        if (!accountController.isAdministrator(userId))
            return null;

        ConfigurationKey key = ConfigurationKey.valueOf(setting.getKey());
        if (key == null)
            return null;

        Configuration configuration = setPropertyValue(key, setting.getValue());
        return configuration.toDataTransferObject();
    }

    /**
     * Initializes the database on new install
     */
    public void initPropertyValues() {
        for (ConfigurationKey key : ConfigurationKey.values()) {
            Configuration config = dao.get(key);
            if (config != null || key.getDefaultValue().isEmpty())
                continue;

            Logger.info("Setting value for " + key.name() + " to " + key.getDefaultValue());
            setPropertyValue(key, key.getDefaultValue());
        }
    }
}
