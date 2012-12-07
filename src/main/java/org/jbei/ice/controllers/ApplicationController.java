package org.jbei.ice.controllers;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.bulkupload.BulkUploadController;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.StorageController;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.group.GroupController;
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

    private static PermissionsController permissionsController;
    private static AccountController accountController;
    private static AttachmentController attachmentController;
    private static SampleController sampleController;
    private static SequenceAnalysisController sequenceAnalysisController;
    private static SequenceController sequenceController;
    private static EntryController entryController;
    private static GroupController groupController;
    private static StorageController storageController;
    private static FolderController folderController;
    private static BulkUploadController bulkUploadController;
    private static ConfigurationController configurationController;
    private static PreferencesController preferencesController;
    private static SearchController searchController;

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
        ConfigurationController controller = new ConfigurationController();
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
        PermissionsController controller = ApplicationController.getPermissionController();
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

    public static PermissionsController getPermissionController() {
        if (permissionsController == null)
            permissionsController = new PermissionsController();
        return permissionsController;
    }

    public static AccountController getAccountController() {
        if (accountController == null) {
            accountController = new AccountController();
        }
        return accountController;
    }

    public static AttachmentController getAttachmentController() {
        if (attachmentController == null)
            attachmentController = new AttachmentController();
        return attachmentController;
    }

    public static SampleController getSampleController() {
        if (sampleController == null)
            sampleController = new SampleController();
        return sampleController;
    }

    public static SequenceAnalysisController getSequenceAnalysisController() {
        if (sequenceAnalysisController == null)
            sequenceAnalysisController = new SequenceAnalysisController();
        return sequenceAnalysisController;
    }

    public static SequenceController getSequenceController() {
        if (sequenceController == null)
            sequenceController = new SequenceController();
        return sequenceController;
    }

    public static EntryController getEntryController() {
        if (entryController == null)
            entryController = new EntryController();
        return entryController;
    }

    public static GroupController getGroupController() {
        if (groupController == null)
            groupController = new GroupController();
        return groupController;
    }

    public static StorageController getStorageController() {
        if (storageController == null)
            storageController = new StorageController();
        return storageController;
    }

    public static FolderController getFolderController() {
        if (folderController == null)
            folderController = new FolderController();
        return folderController;
    }

    public static BulkUploadController getBulkUploadController() {
        if (bulkUploadController == null)
            bulkUploadController = new BulkUploadController();
        return bulkUploadController;
    }

    public static ConfigurationController getConfigurationController() {
        if (configurationController == null)
            configurationController = new ConfigurationController();
        return configurationController;
    }

    public static PreferencesController getPreferencesController() {
        if (preferencesController == null)
            preferencesController = new PreferencesController();
        return preferencesController;
    }

    public static SearchController getSearchController() {
        if (searchController == null)
            searchController = new SearchController();
        return searchController;
    }
}
