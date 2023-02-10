package org.jbei.ice.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.Application;
import org.jbei.ice.config.ConfigurationSettings;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.executor.IceExecutorService;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DataStorageType;
import org.jbei.ice.storage.StorageConfiguration;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;

import java.nio.file.Path;

/**
 * Ice servlet context listener for running ICE initializing
 * and pre-shutdown instructions
 *
 * @author Hector Plahar
 */
public class IceServletContextListener implements ServletContextListener {
    // values are "LOCAL" (default), "MINIO"...
    private static final String ENV_STORE_TYPE = "ICE_STORE_TYPE";

    public void contextInitialized(ServletContextEvent event) {
        // fetch using factory based on user entered value for "ENV_STORE_TYPE"
        // this defaults "LOCAL" if not set
        DataStorageType type = determineStorageType();

        // configure storage params
        StorageConfiguration storageConfiguration = new StorageConfiguration();
        Path path = storageConfiguration.initialize();

        try {
            HibernateConfiguration.beginTransaction();
            if (path != null)
                saveConfigKeyValue(ConfigurationKey.DATA_DIRECTORY, path.toString());
            saveConfigKeyValue(ConfigurationKey.STORAGE_TYPE, type.name());
            IceExecutorService.getInstance().startService();
            Application.start();
            HibernateConfiguration.commitTransaction();
        } catch (Throwable e) {
            Logger.logErrorOnly(e);
            HibernateConfiguration.rollbackTransaction();
        }
    }

    private void saveConfigKeyValue(ConfigurationKey key, String value) {
        if (key == null || StringUtils.isEmpty(value))
            return;

        ConfigurationSettings settings = new ConfigurationSettings();

        String existingValue = settings.getPropertyValue(key);
        if (StringUtils.isEmpty(existingValue)) {
            Logger.info("Updating config " + key.name() + " to " + value);
            settings.setPropertyValue(key, value);
        }
    }

    private DataStorageType determineStorageType() {
        String storeType = System.getenv(ENV_STORE_TYPE);
        if (StringUtils.isEmpty(storeType)) {
            // check system property (-D in startup script)
            storeType = System.getProperty(ENV_STORE_TYPE);
        }
        return DataStorageType.fromString(storeType);
    }

    public void contextDestroyed(ServletContextEvent event) {
        // shutdown executor service
        IceExecutorService.getInstance().stopService();
        HibernateConfiguration.close();
    }
}
