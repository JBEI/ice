package org.jbei.ice;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.account.Accounts;
import org.jbei.ice.config.ConfigurationSettings;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.entry.sequence.annotation.AutoAnnotationBlastDbBuildTask;
import org.jbei.ice.executor.IceExecutorService;
import org.jbei.ice.group.GroupController;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.search.blast.RebuildBlastIndexTask;

import java.nio.file.Path;

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
    public static void start(Path dataDirectory) {

        IceExecutorService.getInstance().startService();

        // check for and create public group
        GroupController groupController = new GroupController();
        groupController.createOrRetrievePublicGroup();

        // check for and create admin account
        Accounts accounts = new Accounts();
        accounts.createDefaultAdminAccount();

        // check for and create default settings
        ConfigurationSettings settings = new ConfigurationSettings();
        settings.initPropertyValues();

        // check and set data directory
        setDataDirectory(dataDirectory);

        try {
            // check blast database exists and build if it doesn't
            RebuildBlastIndexTask task = new RebuildBlastIndexTask();
            IceExecutorService.getInstance().runTask(task);

            AutoAnnotationBlastDbBuildTask autoAnnotationBlastDbBuildTask = new AutoAnnotationBlastDbBuildTask();
            IceExecutorService.getInstance().runTask(autoAnnotationBlastDbBuildTask);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    /**
     * Set the data directory configuration value (if empty)
     *
     * @param dataDirectory data directory location
     */
    private static void setDataDirectory(Path dataDirectory) {
        ConfigurationSettings settings = new ConfigurationSettings();
        String value = settings.getPropertyValue(ConfigurationKey.DATA_DIRECTORY);
        if (!StringUtils.isEmpty(value))
            return;

        settings.setPropertyValue(ConfigurationKey.DATA_DIRECTORY, dataDirectory.toString());
    }
}
