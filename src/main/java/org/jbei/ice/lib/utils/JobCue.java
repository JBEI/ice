package org.jbei.ice.lib.utils;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Set;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.lucene.LuceneSearch;
import org.jbei.ice.lib.search.lucene.SearchException;

public class JobCue implements Runnable {
    private final Hashtable<Integer, Long> cue = new Hashtable<Integer, Long>();
    private long counter = 0L;
    private static long wakeupInterval = 1000L;

    // wakeupInterval: Time elapsed before checking if something needs immediate 
    // attention. Usually one second.

    private static class SingletonHolder {
        private static final JobCue INSTANCE = new JobCue();
    }

    private static long DELAY = Long.parseLong(JbeirSettings.getSetting("JOB_CUE_DELAY"));

    private JobCue() {
    }

    public static JobCue getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addJob(Job job) {
        Logger.info("adding job: " + job.toString());
        getCue().put(job.getJob(), Calendar.getInstance().getTimeInMillis());
    }

    public Hashtable<Integer, Long> getCue() {
        return cue;
    }

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

            cue.remove(jobType);
        }
    }

    /**
     * I hope you know what you are doing
     */
    public void processNow() {
        getInstance().setCounter(wakeupInterval);
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
            Logger.error("Failed to run jobcue!", e);
        }
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public long getCounter() {
        return counter;
    }

}
