package org.jbei.ice;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.blast.RebuildBlastIndexTask;

/**
 * Application wide controller with responsibilities for also system initialization
 *
 * @author Hector Plahar
 */
public class ApplicationController {

    /**
     * Schedule task to rebuild the blast index
     */
    public static void scheduleBlastIndexRebuildTask(boolean force) {
        RebuildBlastIndexTask task = new RebuildBlastIndexTask(force);
        IceExecutorService.getInstance().runTask(task);
    }

    /**
     * Responsible for initializing the system and checking for the existence of needed
     * data (such as settings) and creating as needed
     */
    public static void initialize() {
        // check for and create public group
        GroupController groupController = new GroupController();
        groupController.createOrRetrievePublicGroup();

        // check for and create admin account
        AccountController accountController = new AccountController();
        accountController.createAdminAccount();

        // check for and create default settings
        ConfigurationController configurationController = new ConfigurationController();
        configurationController.initPropertyValues();

        // check blast
        Logger.info("Checking blast database");
        scheduleBlastIndexRebuildTask(false);
    }
}
