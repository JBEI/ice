package org.jbei.ice;

import org.jbei.ice.account.Accounts;
import org.jbei.ice.config.ConfigurationSettings;
import org.jbei.ice.entry.sequence.annotation.AutoAnnotationBlastDbBuildTask;
import org.jbei.ice.executor.IceExecutorService;
import org.jbei.ice.group.GroupController;
import org.jbei.ice.search.blast.RebuildBlastIndexTask;

/**
 * @author Hector Plahar
 */
public class Application {

    /**
     * Responsible for initializing the system and checking for the existence of needed
     * data (such as settings) and creating as needed
     */
    public static void start() {

        // check for and create public group
        GroupController groupController = new GroupController();
        groupController.createOrRetrievePublicGroup();

        // check for and create administrative account
        Accounts accounts = new Accounts();
        accounts.createDefaultAdminAccount();

        // check for and create default settings
        ConfigurationSettings settings = new ConfigurationSettings();
        settings.initPropertyValues();

        // check blast database exists and build if it doesn't
        RebuildBlastIndexTask task = new RebuildBlastIndexTask();
        IceExecutorService.getInstance().runTask(task);

        // similarly, check for auto-annotation database
        AutoAnnotationBlastDbBuildTask autoAnnotationBlastDbBuildTask = new AutoAnnotationBlastDbBuildTask();
        IceExecutorService.getInstance().runTask(autoAnnotationBlastDbBuildTask);
    }
}
