package org.jbei.ice.controllers;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.lib.search.blast.RebuildBlastIndexTask;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;
import org.jbei.ice.shared.dto.ConfigurationKey;

/**
 * Application wide controller
 *
 * @author Hector Plahar
 */
public class ApplicationController {

    public static final String RELEASE_DATABASE_SCHEMA_VERSION = "3.3.0";
    public static final String[] SUPPORTED_PREVIOUS_DB_VERSIONS = {"3.1.0"};

    /**
     * Schedule task to rebuild the blast index
     */
    public static void scheduleBlastIndexRebuildTask(boolean force) {
        RebuildBlastIndexTask task = new RebuildBlastIndexTask(force);
        IceExecutorService.getInstance().runTask(task);
    }

    public static void initializeHibernateSearch() {
        SearchController controller = new SearchController();
        try {
            controller.initHibernateSearch();
        } catch (ControllerException ce) {
            Logger.error(ce);
        }
    }

    public static void upgradeDatabaseIfNecessary() {
        ConfigurationController controller = ControllerFactory.getConfigurationController();
        String dbVersion = null;

        try {
            dbVersion = controller.retrieveDatabaseVersion();
        } catch (ControllerException e) {
            Logger.info("New database");
        }

        try {
            if (dbVersion == null) {
                controller.setPropertyValue(ConfigurationKey.DATABASE_SCHEMA_VERSION, RELEASE_DATABASE_SCHEMA_VERSION);
                dbVersion = RELEASE_DATABASE_SCHEMA_VERSION;
            }

            if (RELEASE_DATABASE_SCHEMA_VERSION.equalsIgnoreCase(dbVersion)) {
                Logger.info("Application version: " + RELEASE_DATABASE_SCHEMA_VERSION);
            } else {
                // check if previous supported versions
                boolean previousVersionSupported = isPreviousVersionSupported(dbVersion);
                if (!previousVersionSupported) //TODO : fatal db cannot be upgraded
                    return;

                // upgrade the db and save new version
                upgradePermissions();
                initializeHibernateSearch();
                upgradeConfiguration();
                controller.updateDatabaseVersion(RELEASE_DATABASE_SCHEMA_VERSION);
                Logger.info("Application upgraded from " + dbVersion + " to " + RELEASE_DATABASE_SCHEMA_VERSION);
            }

            String value = controller.getPropertyValue(ConfigurationKey.WEB_PARTNERS);
            if (value == null)
                return;
            for (String split : value.split(";")) {
                if (split.isEmpty())
                    continue;
                RegistryAPIServiceClient.getInstance().addPortName(split);
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
        }
    }

    public static void upgradePermissions() throws ControllerException {
        // convert all read/write user/group to permission
        PermissionsController controller = ControllerFactory.getPermissionController();
        controller.upgradePermissions();
    }

    private static boolean isPreviousVersionSupported(String version) {
        for (String prevVersion : SUPPORTED_PREVIOUS_DB_VERSIONS) {
            if (prevVersion.equalsIgnoreCase(version))
                return true;
        }
        return false;
    }

    // upgrade to system settings
    private static void upgradeConfiguration() throws ControllerException {
        ConfigurationController configurationController = new ConfigurationController();
        configurationController.upgradeConfiguration();
    }
}
