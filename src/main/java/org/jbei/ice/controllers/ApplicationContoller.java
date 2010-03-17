package org.jbei.ice.controllers;

import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;

public class ApplicationContoller {
    public static void scheduleBlastIndexRebuildJob() {
        JobCue.getInstance().addJob(Job.REBUILD_BLAST_INDEX);
    }

    public static void scheduleSearchIndexRebuildJob() {
        JobCue.getInstance().addJob(Job.REBUILD_SEARCH_INDEX);
    }
}
