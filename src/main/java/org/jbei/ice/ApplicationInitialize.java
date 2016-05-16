package org.jbei.ice;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.entry.sequence.annotation.AutoAnnotationBlastDbBuildTask;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.blast.BlastPlus;

/**
 * Responsible for initializing the ICE application
 * on startup
 *
 * @author Hector Plahar
 */
public class ApplicationInitialize {

    /**
     * Responsible for initializing the system and checking for the existence of needed
     * data (such as settings) and creating as needed
     */
    public static void startUp() {
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
        BlastPlus.scheduleBlastIndexRebuildTask(false);

        AutoAnnotationBlastDbBuildTask autoAnnotationBlastDbBuildTask = new AutoAnnotationBlastDbBuildTask();
        IceExecutorService.getInstance().runTask(autoAnnotationBlastDbBuildTask);
    }
}
