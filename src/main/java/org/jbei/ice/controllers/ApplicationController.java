package org.jbei.ice.controllers;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.blast.RebuildBlastIndexTask;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;

/**
 * Application wide controller
 *
 * @author Hector Plahar
 */
public class ApplicationController {

    public static final String RELEASE_DATABASE_SCHEMA_VERSION = "3.4";
    public static final String[] SUPPORTED_PREVIOUS_DB_VERSIONS = {"3.1.0", "3.3.0"};

    /**
     * Schedule task to rebuild the blast index
     */
    public static void scheduleBlastIndexRebuildTask(boolean force) {
        RebuildBlastIndexTask task = new RebuildBlastIndexTask(force);
        IceExecutorService.getInstance().runTask(task);
    }

    public static void initializeHibernateSearch() {
        try {
            ControllerFactory.getSearchController().initHibernateSearch();
        } catch (ControllerException ce) {
            Logger.error(ce);
        }
    }

    /**
     * Responsible for initializing the system
     */
    public static void initialize() {
        ConfigurationController controller = ControllerFactory.getConfigurationController();

        try {
            String dbVersion = controller.retrieveDatabaseVersion();
            if (dbVersion == null) {
                // new database
                controller.setPropertyValue(ConfigurationKey.DATABASE_SCHEMA_VERSION, RELEASE_DATABASE_SCHEMA_VERSION);
                controller.initPropertyValues();
                return;
            }

            if (RELEASE_DATABASE_SCHEMA_VERSION.equalsIgnoreCase(dbVersion)) {
                Logger.info("Application version: " + RELEASE_DATABASE_SCHEMA_VERSION);
            } else {
                // check if previous supported versions
                boolean previousVersionSupported = isPreviousVersionSupported(dbVersion);
                if (!previousVersionSupported)
                    throw new RuntimeException("Upgrade from " + dbVersion + " is not supported");

                // perform custom upgrade depending on version
                upgradeDatabase(dbVersion);

                controller.updateDatabaseVersion(RELEASE_DATABASE_SCHEMA_VERSION);
                Logger.info("Application upgraded from " + dbVersion + " to " + RELEASE_DATABASE_SCHEMA_VERSION);
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
        }
    }

    private static void upgradeDatabase(String versionToUpgrade) throws ControllerException {
        switch (versionToUpgrade) {
            // UPGRADE from 3.1.0 to 3.4
            case "3.1.0":
                ControllerFactory.getPermissionController().upgradePermissions();
                initializeHibernateSearch();
                ControllerFactory.getConfigurationController().upgradeConfiguration();
                break;

            // upgrade from 3.3 to 3.4
            case "3.3.0":
                // upgrade web of registries partners
                ControllerFactory.getWebController().upgradeWebOfRegistries();
                break;
        }
    }

    private static boolean isPreviousVersionSupported(String version) {
        for (String prevVersion : SUPPORTED_PREVIOUS_DB_VERSIONS) {
            if (prevVersion.equalsIgnoreCase(version))
                return true;
        }
        return false;
    }
}
