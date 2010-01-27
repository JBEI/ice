package org.jbei.ice.lib.utils;

public class Job {
    public static final Job REBUILD_SEARCH_INDEX = new Job(1);
    public static final Job REBUILD_BLAST_INDEX = new Job(2);

    private final int job;

    private Job(int job) {
        this.job = job;
    }

    public int getJob() {
        return job;
    }
}
