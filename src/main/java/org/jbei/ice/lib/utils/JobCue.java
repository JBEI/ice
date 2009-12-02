package org.jbei.ice.lib.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.search.Search;

public class JobCue implements Runnable {
	
	private static HashMap<Integer, Long> cue = new HashMap<Integer, Long> ();
	private static JobCue jobCue = new JobCue();
	private static long DELAY =600000L; // 600000ms = 10 minutes

	private JobCue() {
	}
	
	public static JobCue getInstance() {
		return jobCue;
	}
	
	public void addJob(Job job) {
		getCue().put(job.getJob(), Calendar.getInstance().getTimeInMillis());
	}

	public HashMap<Integer, Long> getCue() {
		return cue;
	}
	
	private synchronized void processCue() {
		//TODO use reflection or something
		Set<Integer> processedJobs = cue.keySet();
		Logger.info("Proccesing jobs. There are " + processedJobs.size() + " jobs.");
		try {
			Search s = Search.getInstance();
			s.rebuildIndex();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		for (Integer jobType : processedJobs) {
			if (jobType == 1) {
				Logger.info("Rebuilding search index");
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
				//TODO: rebuild blast index
			}
			cue.remove(jobType);
			
		}
	}
	
	public void run() {
		try {
			while(!Thread.currentThread().isInterrupted()) {
				JobCue instance = getInstance();
				instance.processCue();
				Thread.sleep(DELAY);
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
