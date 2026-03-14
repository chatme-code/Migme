/**
 * Copyright (c) 2013 Project Goth
 *
 * ScheduledJobsHandler.java
 * Created Jan 21, 2014, 10:04:18 AM
 */

package com.projectgoth.util.scheduler;

import com.projectgoth.common.Logger;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.scheduler.JobScheduler.ScheduleListener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a class that can handle starting and stopping of known jobs
 * identified by String keys. Uses the {@link JobScheduler}
 * 
 * @author angelorohit
 */
public class ScheduledJobsHandler {

    /**
     * The different keys to uniquely identify jobs that can be scheduled.
     * @author angelorohit
     *
     */
    public enum ScheduledJobKeys {
        GET_UNREAD_ALERTS("JOBSCHED_GET_UNREAD_ALERTS"),
        GET_ADDRESSBOOK_CONTACTS("JOBSCHED_GET_ADDRESSBOOK_CONTACTS"),
        SYNC_ADDRESSBOOK_CONTACTS("JOBSCHED_SYNC_ADDRESSBOOK_CONTACTS"),
        GET_CURRENT_LOCATION("JOBSCHED_GET_CURRENT_LOCATION"),
        GET_REVERSE_GEOCODED_ADDRESSES("JOBSCHED_GET_REVERSE_GEOCODED_ADDRESSES"),
        SAVE_PHOTO_TO_EXTERNAL_STORAGE("JOBSCHED_SAVE_PHOTO_TO_EXTERNAL_STORAGE");
        
        
        private String value;
        private ScheduledJobKeys(final String value) {
            this.value = value;
        }
        
        public String value() {
            return this.value;
        }
    }
    
    private static final String         LOG_TAG  = AndroidLogger.makeLogTag(ScheduledJobsHandler.class);
    
    /**
     * A table containing all the currently running jobs.
     * Jobs that are no longer running will not be found in this table.
     * The key is of type {@link ScheduledJobKeys} and the value is a job id.
     */
    private ConcurrentHashMap<ScheduledJobKeys, Integer>   runningJobsTable = null;   

    private static ScheduledJobsHandler instance = null;

    /**
     * Constructor.
     */
    private ScheduledJobsHandler() {
        runningJobsTable = new ConcurrentHashMap<ScheduledJobKeys, Integer>();
    }

    public static synchronized ScheduledJobsHandler getInstance() {
        if (instance == null) {
            instance = new ScheduledJobsHandler();
        }

        return instance;
    }

    /**
     * Creates and starts a job with the given key. Nothing is done if the job is already running.
     * @param key           A {@link ScheduledJobKeys} that uniquely represents the job to be started.
     * @param schedListener A {@link ScheduleListener} that will be invoked when the job is done.
     * @param delay         The time to wait before the job gets executed or re-executed (in the case of repeating jobs).
     * @param isRepeating   Whether this job is to be continually repeated after the delay.
     * @return              true on successfully starting the job and false in case of failure.
     */
    public boolean startJobWithKey(final ScheduledJobKeys key, final ScheduleListener schedListener, final long delay,
            final boolean isRepeating) {
        Logger.debug.log(LOG_TAG, "startJobWithKey - key:" +  key.value + " delay:" + delay + " isRepearting:" + isRepeating);
        
        try {
            // Don't do anything if the job is already running
            if (!runningJobsTable.containsKey(key)) {
                // Create job if it does not exist already...
                final int jobId = JobScheduler.getInstance().createJob(-1, new ScheduleListener() {

                    @Override
                    public void processJob() {
                        // If this is not a repeating job, then we stop it
                        // and remove it's associated id from the
                        // runningJobsTable.
                        if (!isRepeating) {
                            stopJobWitKey(key);
                        }
                        schedListener.processJob();
                    }
                }, delay, isRepeating);

                Logger.debug.log(LOG_TAG, "created job - key:" + key + " jobId:" + jobId);
                runningJobsTable.put(key, jobId);
                
                // Start job that must have been previously created.
                return JobScheduler.getInstance().startJob(jobId);
            }
        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
            return false;
        }

        return false;
    }

    /**
     * Stops a job. If the job is not already running, then nothing is done.
     * @param key A {@link ScheduledJobKeys} that uniquely identifies the job to be stopped.
     */
    public void stopJobWitKey(final ScheduledJobKeys key) {
        if (runningJobsTable.containsKey(key)) {
            try {
                JobScheduler.getInstance().stopJob(runningJobsTable.get(key));
                runningJobsTable.remove(key);
            } catch (Exception ex) {
                Logger.error.log(LOG_TAG, ex);
            }
        }
    }
    
    /**
     * Checks whether a job is currently running or not.
     * @param key A {@link ScheduledJobKeys} that uniquely identifies the job
     * @return  true if a matching job is currently running and false otherwise.
     */
    public boolean isJobWithKeyRunning(final ScheduledJobKeys key) {
        return runningJobsTable.containsKey(key);
    }
    
    /**
     * Stops all running jobs.
     */
    public void stopAllJobs() {
        final Set<ScheduledJobKeys> keySet = runningJobsTable.keySet(); 
        for (ScheduledJobKeys key : keySet) {
            stopJobWitKey(key);
        }
    }
}
