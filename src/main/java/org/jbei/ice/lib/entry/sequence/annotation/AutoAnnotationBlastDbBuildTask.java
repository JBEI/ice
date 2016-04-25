package org.jbei.ice.lib.entry.sequence.annotation;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.search.blast.BlastPlus;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Task to rebuild blast database of features for auto annotation
 * <p>
 * Uses a lock object to wait until a specific time of day and then re-builds.
 * </p>
 *
 * @author Hector Plahar
 */
public class AutoAnnotationBlastDbBuildTask extends Task {

    private Timer timer = new Timer(true);
    private boolean stopped;
    private final Object LOCK_OBJECT = new Object();
    private int exceptionCount;
    private final int RUN_HOUR = 1;    // make config param
    private final boolean runOnce;

    public AutoAnnotationBlastDbBuildTask(boolean runOnce) {
        this.runOnce = runOnce;
    }

    public AutoAnnotationBlastDbBuildTask() {
        this(false);
    }

    @Override
    public void execute() {
        Logger.info("Running Annotation rebuild task");

        // first run on task start up
        try {
            BlastPlus.rebuildFeaturesBlastDatabase("auto-annotation");
        } catch (IOException e) {
            Logger.error(e);
            exceptionCount += 1;
        }

        if (this.runOnce)
            return;

        while (!stopped) {
            waitUntil(timeTillRun());

            try {
                Logger.info("Rebuilding auto annotation blast database");
                BlastPlus.rebuildFeaturesBlastDatabase("auto-annotation");
            } catch (IOException ioe) {
                Logger.error(ioe);
                if (exceptionCount++ >= 10) {
                    Logger.error(exceptionCount + " exceptions encountered. Aborting annotation rebuild");
                    stopped = true;
                }
            }
        }
        Logger.info("Annotation rebuild task stopped");
        timer.cancel();
        timer.purge();
    }

    public void stop() {
        this.stopped = true;
    }

    protected Date timeTillRun() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (currentHour >= RUN_HOUR) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
//        int min = calendar.get(Calendar.MINUTE);
//        calendar.set(Calendar.MINUTE, 5 * (min / 5 + 1));
        calendar.set(Calendar.HOUR_OF_DAY, RUN_HOUR);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Uses the defined lock object to wait until a specified time
     *
     * @param date date to notify lock object (awake thread)
     */
    public void waitUntil(Date date) {
        Logger.info("Waiting till " + date);

        // lock object
        synchronized (LOCK_OBJECT) {
            try {
                LOCK_OBJECT.wait();
            } catch (InterruptedException ie) {
                Logger.warn(ie.getMessage());
                stopped = true;
                return;
            }
        }

        // define and schedule task to "notify" at scheduled time
        timer.schedule(new TimerTask() {
            public void run() {
                synchronized (LOCK_OBJECT) {
                    LOCK_OBJECT.notify();
                }
            }
        }, date);
    }
}
