package org.jbei.ice.lib.config;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.net.WoRController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.hibernate.dao.ConfigurationDAO;
import org.jbei.ice.storage.model.ConfigurationModel;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class ConfigurationSettings {

    private static final String UI_CONFIG_DIR = "asset";
    private static final String BLAST_FTP_DIR = "ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/2.6.0/";

    private final ConfigurationDAO dao;

    public ConfigurationSettings() {
        dao = DAOFactory.getConfigurationDAO();
    }

    public Setting getSystemVersion(String url) {
        String version = getPropertyValue(ConfigurationKey.APPLICATION_VERSION);
        return new Setting("version", version);
    }

    public String getPropertyValue(ConfigurationKey key) {
        ConfigurationModel config = dao.get(key);
        if (config == null)
            return key.getDefaultValue();
        return config.getValue();
    }

    public Setting getPropertyValue(String key) {
        ConfigurationModel config = dao.get(key);
        if (config == null)
            return null;
        return config.toDataTransferObject();
    }

    public ArrayList<Setting> retrieveSystemSettings(String userId) {
        ArrayList<Setting> settings = new ArrayList<>();
        if (!new AccountController().isAdministrator(userId))
            return settings;

        for (ConfigurationKey key : ConfigurationKey.values()) {
            Setting setting = getConfigValue(key);
            settings.add(setting);
        }
        return settings;
    }

    public List<Setting> getSampleRequestSettings(String userId) {
        List<Setting> settings = new ArrayList<>();
        if (!new AccountController().isAdministrator(userId))
            return settings;

        settings.add(getConfigValue(ConfigurationKey.SAMPLE_CREATE_APPROVAL_MESSAGE));
        return settings;
    }

    private Setting getConfigValue(ConfigurationKey key) {
        ConfigurationModel configurationModel = dao.get(key);
        if (configurationModel == null)
            return new Setting(key.name(), "");
        return new Setting(configurationModel.getKey(), configurationModel.getValue());
    }

    public ConfigurationModel setPropertyValue(ConfigurationKey key, String value) {
        ConfigurationModel configurationModel = dao.get(key);
        if (configurationModel == null) {
            configurationModel = new ConfigurationModel();
            configurationModel.setKey(key.name());
            configurationModel.setValue(value);
            return dao.create(configurationModel);
        }

        configurationModel.setValue(value);
        return dao.update(configurationModel);
    }

    public Setting updateSetting(String userId, Setting setting, String url) {
        AccountController accountController = new AccountController();
        if (!accountController.isAdministrator(userId))
            throw new PermissionException("Cannot update system setting without admin privileges");

        ConfigurationKey key = ConfigurationKey.valueOf(setting.getKey());
        ConfigurationModel configurationModel = setPropertyValue(key, setting.getValue());

        // check if the setting being updated is related to the web of registries
        if (key == ConfigurationKey.JOIN_WEB_OF_REGISTRIES) {
            WoRController woRController = new WoRController();
            boolean enable = "yes".equalsIgnoreCase(setting.getValue()) || "true".equalsIgnoreCase(setting.getValue());
            woRController.setEnable(userId, enable, url);
        }

        return configurationModel.toDataTransferObject();
    }

    // update the setting automatically. Currently works only for blast installations
    public Setting autoUpdateSetting(String userId, Setting setting) {
        AccountController accountController = new AccountController();
        if (!accountController.isAdministrator(userId))
            throw new PermissionException("Cannot auto update system setting without admin privileges");

        ConfigurationModel configurationModel = dao.get(setting.getKey());
        if (configurationModel == null) {
            Logger.warn("Could not retrieve setting " + setting.getKey() + ". Creating...");
            if (setting.getValue() == null)
                setting.setValue("");
            configurationModel = new ConfigurationModel();
            configurationModel.setKey(setting.getKey());
            configurationModel.setValue(setting.getValue());
            configurationModel = dao.create(configurationModel);
        }

        String osName = System.getProperty("os.name").replaceAll("\\s+", "").toLowerCase();
        String blast = "ncbi-blast-2.6.0+-x64-" + osName + ".tar.gz";

        Path path = Paths.get(dao.get(ConfigurationKey.TEMPORARY_DIRECTORY).getValue(), blast);
        Path dest = Paths.get(dao.get(ConfigurationKey.DATA_DIRECTORY).getValue());
        if (!Files.exists(dest)) {
            Logger.error("Cannot access access dir : " + dest.toString());
            return null;
        }

        try (InputStream is = (new URL(BLAST_FTP_DIR + blast)).openStream()) {
            Files.copy(is, path.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);

            Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
            archiver.extract(path.toFile(), dest.toFile());

            Path valuePath = Paths.get(dest.toString(), "ncbi-blast-2.6.0+", "bin");
            configurationModel.setValue(valuePath.toString());
            Files.list(valuePath).forEach(dirPath -> {
                try {
                    Files.setPosixFilePermissions(dirPath, PosixFilePermissions.fromString("rwxrwxrwx"));
                } catch (IOException e) {
                    Logger.error(e);
                }
            });

            return dao.update(configurationModel).toDataTransferObject();
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
            ConfigurationModel config = dao.get(key);
            if (config != null || key.getDefaultValue().isEmpty())
                continue;

            Logger.info("Setting value for " + key.name() + " to " + key.getDefaultValue());
            setPropertyValue(key, key.getDefaultValue());
        }
    }

    public SiteSettings getSiteSettings() {
        SiteSettings settings = new SiteSettings();
        String dataDirectory = dao.get(ConfigurationKey.DATA_DIRECTORY).getValue();
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
        String dataDirectory = dao.get(ConfigurationKey.DATA_DIRECTORY).getValue();
        Path path = Paths.get(dataDirectory, UI_CONFIG_DIR, assetName);
        if (Files.exists(path)) {
            return path.toFile();
        }

        return null;
    }

    public boolean hasDataDirectory() {
        // session valid?
        return HibernateConfiguration.isInitialized();
    }

    public SiteSettings getInitialValues() {

        SiteSettings siteSettings = new SiteSettings();

        // get the data directory home
        String propertyHome = System.getenv("ICE_DATA_HOME");
        Path iceHome;

        if (StringUtils.isBlank(propertyHome)) {
            // check system property (-D in startup script)
            propertyHome = System.getProperty("ICE_DATA_HOME");

            // still nothing, check home directory
            if (StringUtils.isBlank(propertyHome)) {
                String userHome = System.getProperty("user.home");
                iceHome = Paths.get(userHome, ".ICEData");
            } else {
                iceHome = Paths.get(propertyHome);
            }
        } else {
            iceHome = Paths.get(propertyHome);
        }
        siteSettings.setDataDirectory(iceHome.toString());

        // get the

        return siteSettings;
    }
}
