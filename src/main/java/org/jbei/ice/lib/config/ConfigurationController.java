package org.jbei.ice.lib.config;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.net.WoRController;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ConfigurationDAO;
import org.jbei.ice.storage.model.Configuration;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author Hector Plahar
 */
public class ConfigurationController {

    public static final String UI_CONFIG_DIR = "asset";

    private final ConfigurationDAO dao;

    public ConfigurationController() {
        dao = DAOFactory.getConfigurationDAO();
    }

    public Setting getSystemVersion(String url) {
        String version = getPropertyValue(ConfigurationKey.APPLICATION_VERSION);
        return new Setting("version", version);
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

    public Setting updateSetting(String userId, Setting setting, String url) {
        AccountController accountController = new AccountController();
        if (!accountController.isAdministrator(userId))
            throw new PermissionException("Cannot update system setting without admin privileges");

        ConfigurationKey key = ConfigurationKey.valueOf(setting.getKey());
        if (key == null)
            throw new IllegalArgumentException("Invalid system key " + setting.getKey());

        Configuration configuration = setPropertyValue(key, setting.getValue());

        // check if the setting being updated is related to the web of registries
        if (key == ConfigurationKey.JOIN_WEB_OF_REGISTRIES) {
            WoRController woRController = new WoRController();
            boolean enable = "yes".equalsIgnoreCase(setting.getValue()) || "true".equalsIgnoreCase(setting.getValue());
            woRController.setEnable(userId, enable, url);
        }

        return configuration.toDataTransferObject();
    }

    // update the setting automatically. Currently works only for blast installations
    public Setting autoUpdateSetting(String userId, Setting setting) {
        AccountController accountController = new AccountController();
        if (!accountController.isAdministrator(userId))
            throw new PermissionException("Cannot auto update system setting without admin privileges");

        Configuration configuration = dao.get(setting.getKey());
        if (configuration == null) {
            Logger.error("Could not retrieve setting " + setting.getKey());
            return null;
        }

        String osName = System.getProperty("os.name").replaceAll("\\s+", "").toLowerCase();
        String blast = "ncbi-blast-2.6.0+-x64-" + osName + ".tar.gz";
        Path path = Paths.get(dao.get(ConfigurationKey.TEMPORARY_DIRECTORY).getValue(), blast);
        Path dest = Paths.get(dao.get(ConfigurationKey.DATA_DIRECTORY).getValue());
        if (!Files.exists(dest)) {
            Logger.error("Cannot access access dir : " + dest.toString());
            return null;
        }

        try {
            URL url = new URL("ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/" + blast);
            try (InputStream is = url.openStream();
                 ByteArrayOutputStream os = new ByteArrayOutputStream()) {

                if (Files.exists(path))
                    Files.delete(path);
                byte[] buf = new byte[4096];
                int n;

                while ((n = is.read(buf)) >= 0) {
                    os.write(buf, 0, n);
                }
                Files.write(path, os.toByteArray());
            }

            Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
            archiver.extract(path.toFile(), dest.toFile());

            configuration.setValue(dest.toString() + File.separatorChar + "ncbi-blast-2.6.0+");
            return dao.update(configuration).toDataTransferObject();
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
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

    public SiteSettings getSiteSettings() {
        SiteSettings settings = new SiteSettings();
        String dataDirectory = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        final String LOGO_NAME = "logo.png";
        final String LOGIN_MESSAGE_FILENAME = "institution.html";
        final String FOOTER_FILENAME = "footer.html";

        settings.setHasLogo(Files.exists(Paths.get(dataDirectory, UI_CONFIG_DIR, LOGO_NAME)));
        settings.setHasLoginMessage(Files.exists(Paths.get(dataDirectory, UI_CONFIG_DIR, LOGIN_MESSAGE_FILENAME)));
        settings.setHasFooter(Files.exists(Paths.get(dataDirectory, UI_CONFIG_DIR, FOOTER_FILENAME)));
        settings.setAssetName(UI_CONFIG_DIR);

        return settings;
    }

    public File getUIAsset(String assetName) {
        if (StringUtils.isEmpty(assetName))
            throw new IllegalArgumentException("Cannot retrieve asset with no name");
        String dataDirectory = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        Path path = Paths.get(dataDirectory, UI_CONFIG_DIR, assetName);
        if (Files.exists(path)) {
            return path.toFile();
        }

        return null;
    }
}
