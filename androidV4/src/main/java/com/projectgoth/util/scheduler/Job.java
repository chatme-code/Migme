/**
 * Copyright (c) 2013 Project Goth
 *
 * Job.java
 * Created Aug 28, 2013, 2:30:37 PM
 */

package com.projectgoth.util.scheduler;

import com.projectgoth.util.scheduler.JobScheduler.ScheduleListener;

/**
 * @author warrenbalcos
 * 
 */
public class Job {

    private int              id;

    private boolean          isRepeating;

    private long             delay;

    private ScheduleListener scheduleListener;

    public Job() {
    }

    protected void work() {
        if (scheduleListener != null) {
            scheduleListener.processJob();
        }
    }

    /**
     * @param schedListener
     *            the schedListener to set
     */
    public void setScheduleListener(ScheduleListener scheduleListener) {
        this.scheduleListener = scheduleListener;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the isRepeating
     */
    public boolean isRepeating() {
        return isRepeating;
    }

    /**
     * @param isRepeating the isRepeating to set
     */
    public void setRepeating(boolean isRepeating) {
        this.isRepeating = isRepeating;
    }

    /**
     * @return the delay
     */
    public long getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }
}
