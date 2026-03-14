/**
 * Copyright (c) 2013 Project Goth
 *
 * JobScheduler.java
 * Created Aug 28, 2013, 2:24:23 PM
 */

package com.projectgoth.util.scheduler;

import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.projectgoth.common.Logger;

/**
 * @author warrenbalcos
 * 
 */
public class JobScheduler {

    private static final String       TAG      = "JobScheduler";

    private SparseArray<Job>          jobList;

    private static final JobScheduler INSTANCE = new JobScheduler();

    private static final byte[]       lock     = new byte[0];
    
    private AtomicInteger             idGen;

    public synchronized static JobScheduler getInstance() {
        return INSTANCE;
    }

    private JobScheduler() {
        jobList = new SparseArray<Job>();
        idGen = new AtomicInteger(0);
        looperThread.start();
    }

    public interface ScheduleListener {

        public void processJob();
    }

    //@formatter:off
    private LooperThread looperThread = new LooperThread();
    
    class LooperThread extends Thread {
        private Handler mHandler;

        public void run() {
            Looper.prepare();

            synchronized (this) {
                mHandler = new Handler(callBackHandler);
                notifyAll();
            }

            Looper.loop();
        }
        
        /** in this way, we make the we got the handler not null */
        public synchronized Handler getHandler() {
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    //Ignore and try again.
                }
            }
            return mHandler;
        }
        
    };
    
    private Callback callBackHandler = new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            synchronized (lock) {
                Job job = jobList.get(msg.what);
                Logger.debug.log(TAG, "handle msg id:", msg.what);
                if (job != null) {
                    Logger.debug.log(TAG, "job.work()");
                    job.work();
                    if (job.isRepeating()) {
                        Logger.debug.log(TAG, "repeating job:", job.getId());
                        startJob(job.getId());
                    }
                    return true;
                }
                return false;
            }
        }
    };
    //@formatter:on

    /**
     * Create a job to be scheduled. If job already exists for the id supplied,
     * the old job would just be updated. Otherwise, the new job will be created
     * and the newly generated id will be returned via the id argument. Set id
     * as to a negative value (-1) to force generate a job object
     * 
     * @param id
     * @param schedListener
     * @param delay
     * @param repeating
     * 
     * @return
     * @throws Exception
     */
    public int createJob(int id, ScheduleListener schedListener, long delay, boolean repeating) {
        Logger.debug.log(TAG, "createJob: ", id);
        if (delay <= 0) {
            throw new RuntimeException("delay must be greater the zero: " + delay);
        }
        synchronized (lock) {
            Job job = null;
            if (id >= 0) {
                job = jobList.get(id);
            }
            if (job == null) {
                job = new Job();
                id = idGen.getAndAdd(1);
            }
            job.setId(id);
            job.setScheduleListener(schedListener);
            job.setDelay(delay);
            job.setRepeating(repeating);

            jobList.put(id, job);
            Logger.debug.log(TAG, "createJob - created job with id:", id);
            return id;
        }
    }

    /**
     * starts or restarts a {@link Job}
     * 
     * @param id
     * @return
     */
    public boolean restartJob(int id) {
        return startJob(id, true);
    }

    /**
     * starts a {@link Job}
     * 
     * @param id
     * @return
     */
    public boolean startJob(int id) {
        return startJob(id, false);
    }

    private boolean startJob(int id, boolean restart) {
        Logger.debug.log(TAG, "startJob: ", id);
        synchronized (lock) {
            Handler handler = looperThread.getHandler();
            Job job = jobList.get(id);
            if (job != null && handler != null) {
                if (restart) {
                    if (handler.hasMessages(id)) {
                        handler.removeMessages(id);
                    }
                    return handler.sendEmptyMessageDelayed(id, job.getDelay());
                } else {
                    Logger.debug.log(TAG, "send message id:", id, " delay:", job.getDelay());
                    return handler.sendEmptyMessageDelayed(id, job.getDelay());
                }
            }
            return false;
        }
    }

    public boolean isScheduled(int id) {
        return looperThread.getHandler().hasMessages(id);
    }
    
    /**
     * Stops a {@link Job}
     * 
     * @param id
     * @return
     */
    public boolean stopJob(int id) {
        synchronized (lock) {
            Handler handler = looperThread.getHandler();
            if (handler != null && handler.hasMessages(id)) {
                handler.removeMessages(id);
                return true;
            }
            return false;
        }
    }

    // public void onDestroy() {
    // synchronized (lock) {
    // // Clean up code
    // for (int i = 0; i < jobList.size(); i++) {
    // stopJob(i);
    // }
    // jobList.clear();
    // handler = null;
    // }
    // }

}
