package org.jbei.ice.controllers;

import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.search.blast.RebuildBlastIndexTask;

/**
 * ABI to manipulate system wide events.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class ApplicationController {

    /**
     * Schedule to rebuild the BLAST search index.
     */
    public static void scheduleBlastIndexRebuildJob() {
        RebuildBlastIndexTask task = new RebuildBlastIndexTask();
        IceExecutorService.getInstance().runTask(task);
    }
}
