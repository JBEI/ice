package org.jbei.ice.controllers;

import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;

public class ApplicationContoller {
    public static final long DEFAULT_PROCESS_IN_TIME = 5000; // ms

    public static void scheduleSearchIndexRebuildJob() {
        scheduleSearchIndexRebuildJob(DEFAULT_PROCESS_IN_TIME);
    }

    public static void scheduleBlastIndexRebuildJob() {
        scheduleBlastIndexRebuildJob(DEFAULT_PROCESS_IN_TIME);
    }

    public static void scheduleSearchIndexRebuildJob(long timeToProcessIn) {
        JobCue jobcue = JobCue.getInstance();
        jobcue.addJob(Job.REBUILD_SEARCH_INDEX);
        jobcue.processIn(timeToProcessIn);
    }

    public static void scheduleBlastIndexRebuildJob(long timeToProcessIn) {
        JobCue jobcue = JobCue.getInstance();
        jobcue.addJob(Job.REBUILD_BLAST_INDEX);
        jobcue.processIn(timeToProcessIn);
    }
}
