package org.jbei.ice.controllers;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.search.blast.RebuildBlastIndexTask;
import org.jbei.ice.lib.utils.Utils;

import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

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
    public static void scheduleBlastIndexRebuildTask() {
        RebuildBlastIndexTask task = new RebuildBlastIndexTask();
        IceExecutorService.getInstance().runTask(task);
    }

    public static void initializeHibernateSearch() {
        Session session = HibernateHelper.newSession();
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        try {
            fullTextSession.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            Logger.error("Indexer interrupted", e);
        }
    }

    public static void upgradeDatabaseIfNecessary() {
        ConfigurationController controller = new ConfigurationController();

        try {
            String dbVersion = controller.retrieveDatabaseVersion();
            if (RELEASE_DATABASE_SCHEMA_VERSION.equalsIgnoreCase(dbVersion)) {
                Logger.info("Application version: " + RELEASE_DATABASE_SCHEMA_VERSION);
                return;
            }

            // check if previous supported versions
            boolean previousVersionSupported = isPreviousVersionSupported(dbVersion);
            if (!previousVersionSupported) // TODO : fatal db cannot be upgraded
                return;

            // upgrade the db and save new version
            initializeHibernateSearch();
            upgradePermissions();
            upgradeAccounts();
            upgradeConfiguration();
            controller.updateDatabaseVersion(RELEASE_DATABASE_SCHEMA_VERSION);
            Logger.info("Application upgraded from " + dbVersion + " to " + RELEASE_DATABASE_SCHEMA_VERSION);
        } catch (ControllerException e) {
            Logger.error(e);
            // TODO : Fatal exception. Database cannot be upgraded
        }
    }

    private static void upgradePermissions() throws ControllerException {
        // convert all read/write user/group to permission
        PermissionsController controller = new PermissionsController();
        controller.upgradePermissions();
    }

    private static boolean isPreviousVersionSupported(String version) {
        for (String prevVersion : SUPPORTED_PREVIOUS_DB_VERSIONS) {
            if (prevVersion.equalsIgnoreCase(version))
                return true;
        }

        return false;
    }

    private static void upgradeAccounts() throws ControllerException {
        Logger.info("Upgrading accounts....please wait");
        AccountController accountController = new AccountController();
        GroupController groupController = new GroupController();

        for (Account account : accountController.retrieveAllAccounts()) {
            if (account.getSalt() == null || account.getSalt().isEmpty()) {
                account.setSalt(Utils.generateUUID());
            }

            Group everyoneGroup = groupController.createOrRetrievePublicGroup();
            account.getGroups().add(everyoneGroup);
            accountController.save(account);
        }
        Logger.info("Accounts upgrade complete");
    }

    // upgrade to system settings
    private static void upgradeConfiguration() throws ControllerException {
        ConfigurationController configurationController = new ConfigurationController();
        configurationController.upgradeConfiguration();
    }
}
