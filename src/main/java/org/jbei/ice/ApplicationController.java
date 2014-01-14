package org.jbei.ice;

import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.search.blast.RebuildBlastIndexTask;

/**
 * Application wide controller
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
     * Responsible for initializing the system
     */
    public static void initialize() {
    }
}
