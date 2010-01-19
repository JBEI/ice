package org.jbei.ice.lib.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.Search;

public class JobCue implements Runnable {

	private static HashMap<Integer, Long> cue = new HashMap<Integer, Long>();
	private long counter = 0L;
	private static long wakeupInterval = 1000L;
	// wakeupInterval: Time elapsed before checking if something needs immediate 
	// attention. Usually one second.

	private static class SingletonHolder {
		private static final JobCue INSTANCE = new JobCue();
	}

	private static long DELAY = Long.parseLong(JbeirSettings
			.getSetting("JOB_CUE_DELAY"));

	private JobCue() {
	}

	public static JobCue getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void addJob(Job job) {
		Logger.info("adding job: " + job.toString());
		getCue().put(job.getJob(), Calendar.getInstance().getTimeInMillis());
	}

	public HashMap<Integer, Long> getCue() {
		return cue;
	}

	private synchronized void processCue() {
		// TODO use reflection or something
		Set<Integer> processedJobs = cue.keySet();
		Logger.info("Proccesing jobs. There are " + processedJobs.size()
				+ " jobs.");
		for (Integer jobType : processedJobs) {
			if (jobType == 1) {
				Logger.info("Running rebuildIndex");
				try {
					Search s = Search.getInstance();
					s.rebuildIndex();
				} catch (Exception e) {
					String msg = "Could not create search index";
					Logger.error(msg);
					e.printStackTrace();
				}

			} else if (jobType == 2) {
				Logger.info("Rebuilding blast index");
				// TODO: rebuild blast index
			}
			cue.remove(jobType);

		}
	}

	/**
	 * I hope you know what you are doing
	 * 
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

	public long getCounter() {
		return counter;
	}

}
