package org.jbei.ice.controllers;

import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;

/**
 * ABI to manipulate system wide events.
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
public class ApplicationController {

    /**
     * Interval for processing events in milliseconds.
     */
    public static final long DEFAULT_PROCESS_IN_TIME = 5000; // ms

    /**
     * Schedule to rebuild the full text search index.
     */
    public static void scheduleSearchIndexRebuildJob() {
//        scheduleSearchIndexRebuildJob(DEFAULT_PROCESS_IN_TIME);
    }

    /**
     * Schedule to rebuild the BLAST search index.
     */
    public static void scheduleBlastIndexRebuildJob() {
        scheduleBlastIndexRebuildJob(DEFAULT_PROCESS_IN_TIME);
    }

    /**
     * Schedule to rebuild the full text search index in the given time interval.
     *
     * @param timeToProcessIn
     */
    public static void scheduleSearchIndexRebuildJob(long timeToProcessIn) {
        JobCue jobcue = JobCue.getInstance();
        jobcue.addJob(Job.REBUILD_SEARCH_INDEX);
        jobcue.processIn(timeToProcessIn);
    }

    /**
     * Schedule to rebuild the BLAST search index in the given time interval.
     *
     * @param timeToProcessIn
     */
    public static void scheduleBlastIndexRebuildJob(long timeToProcessIn) {
        JobCue jobcue = JobCue.getInstance();
        jobcue.addJob(Job.REBUILD_BLAST_INDEX);
        jobcue.processIn(timeToProcessIn);
    }
}
