package org.jbei.ice.lib.config;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.net.RemoteAccessController;
import org.jbei.ice.lib.net.WoRController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ConfigurationDAO;
import org.jbei.ice.storage.model.Configuration;

import java.util.ArrayList;

/**
 * @author Hector Plahar
 */
public class ConfigurationController {

    private final ConfigurationDAO dao;

    public ConfigurationController() {
        dao = DAOFactory.getConfigurationDAO();
    }

    public Setting getSystemVersion(String url) {
        String version = getPropertyValue(ConfigurationKey.APPLICATION_VERSION);

        if (url.equalsIgnoreCase(getPropertyValue(ConfigurationKey.WEB_OF_REGISTRIES_MASTER))) {
            return new Setting("version", version);
        }

        // request version from master
        try {
            return new RemoteAccessController().getMasterVersion();
        } catch (Exception e) {
            return new Setting("version", version);
        }
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

    public ArrayList<Setting> retrieveSystemSettings(String userId) {
        ArrayList<Setting> settings = new ArrayList<>();
        if (!new AccountController().isAdministrator(userId))
            return settings;

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

        // check if the setting being updated is related to the web of registries
        if (key == ConfigurationKey.JOIN_WEB_OF_REGISTRIES) {
            WoRController woRController = new WoRController();
            boolean enable = "yes".equalsIgnoreCase(setting.getValue()) || "true".equalsIgnoreCase(setting.getValue());
            woRController.setEnable(enable);
        }

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
