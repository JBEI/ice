package org.jbei.ice.lib.utils;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Set;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.lucene.LuceneSearch;
import org.jbei.ice.lib.search.lucene.SearchException;

/**
 * Job cue that wakes up, checks for pending jobs, and runs them.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
public class JobCue implements Runnable {
    private final Hashtable<Integer, Long> cue = new Hashtable<Integer, Long>();
    /**
     * Counts towards the next job run.
     */
    private long counter = 0L;
    /**
     * wakeupInterval: Time elapsed before checking if something needs immediate attention. Usually
     * one second.
     */
    private static long wakeupInterval = 1000L;

    private static class SingletonHolder {
        private static final JobCue INSTANCE = new JobCue();
    }

    private static long DELAY = Long.parseLong(JbeirSettings.getSetting("JOB_CUE_DELAY"));

    private JobCue() {
    }

    /**
     * Get an instance of the singleton.
     *
     * @return JobCue object.
     */
    public static JobCue getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Add a job to the cue.
     *
     * @param job
     */
    public void addJob(Job job) {
        Logger.info("adding job: " + job.toString());
        getCue().put(job.getJob(), Calendar.getInstance().getTimeInMillis());
    }

    /**
     * Retrieve the job cue.
     *
     * @return Hashtable of Integer and Long.
     */
    public Hashtable<Integer, Long> getCue() {
        return cue;
    }

    /**
     * Look through the job cue and process them.
     * <p/>
     * Aggregate duplicate jobs. For example, two scheduled blast rebuild will be run only once.
     */
    @SuppressWarnings("unchecked")
    private synchronized void processCue() {
        // TODO: Tim; use reflection or something

        Hashtable<Integer, Long> newCue = (Hashtable<Integer, Long>) cue.clone();

        Set<Integer> processedJobs = newCue.keySet();

        Logger.debug("Proccesing jobs. There are " + processedJobs.size() + " jobs.");

        if (processedJobs.size() > 0) {
            Logger.info("Processing jobs: " + processedJobs.toString());
        }

        if (processedJobs == null || processedJobs.size() == 0) {
            return;
        }

        for (Integer jobType : processedJobs) {
            if (jobType == 1) {
                try {
                    LuceneSearch.getInstance().rebuildIndex();
                } catch (SearchException e) {
                    Logger.error("Failed to rebuild search database!", e);
                }
            } else if (jobType == 2) {
                try {
                    Blast blast = new Blast();

                    blast.rebuildDatabase();
                } catch (BlastException e) {
                    Logger.error("Failed to rebuild blast database!", e);
                }
            }
            Logger.info("Completed job");
            cue.remove(jobType);
        }
    }

    /**
     * Process the cue in fixed milliseconds instead of waiting for
     * the next job cue interval
     *
     * @param interval
     */
    public void processIn(long interval) {
        getInstance().setCounter(interval);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                JobCue instance = getInstance();
                if (instance.getCounter() < 1) {
                    // DELAY is set by settings, usually minutes
                    // wakeupInterval is set internally, usually 1 second.
                    instance.setCounter(DELAY);
                    instance.processCue();
                } else {
                    long newCounter = instance.getCounter() - wakeupInterval;
                    instance.setCounter(newCounter);
                }

                Thread.sleep(wakeupInterval);
            }

        } catch (InterruptedException e) {
            Logger.warn("Job cue interrupted!");
        }
    }

    /**
     * Set the job cue delay counter.
     *
     * @param counter
     */
    public void setCounter(long counter) {
        this.counter = counter;
    }

    /**
     * Retrieve the job cue delay counter.
     *
     * @return Delay counter.
     */
    public long getCounter() {
        return counter;
    }

}
