package com.projectgoth.fusion.ice;

import Ice.ObjectAdapter;
import Ice.ObjectAdapterI;
import IceInternal.ThreadPool;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class IceThreadMonitor extends TimerTask {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(IceThreadMonitor.class));
   private Timer timer;
   private ThreadPool threadPool;
   private List<Thread> threadList;
   private long nextLogTime = 0L;
   private static final int THREAD_DEPTH_TO_SKIP = 0;
   private static final int THREAD_DEPTH_TO_TRACE = 25;
   private final SystemPropertyEntities.IceThreadMonitorSettings threadCountProperty;
   private final SystemPropertyEntities.IceThreadMonitorSettings interDumpDurationProperty;

   public IceThreadMonitor(ObjectAdapter adapter, SystemPropertyEntities.IceThreadMonitorSettings threadCountProperty, SystemPropertyEntities.IceThreadMonitorSettings interDumpDurationProperty) {
      this.threadCountProperty = threadCountProperty;
      this.interDumpDurationProperty = interDumpDurationProperty;

      try {
         this.timer = new Timer();
         ObjectAdapterI oai = (ObjectAdapterI)adapter;
         this.threadPool = oai.getThreadPool();
         Field threadListField = ThreadPool.class.getDeclaredField("_threads");
         threadListField.setAccessible(true);
         this.threadList = (List)threadListField.get(this.threadPool);
         log.info("Tracing Ice Threads started");
      } catch (Exception var6) {
         log.debug("Exception initializating IceThreadMonitor: " + var6 + " | " + var6.getMessage());
      }

   }

   private int getInt(String fieldName) {
      try {
         Field field = ThreadPool.class.getDeclaredField(fieldName);
         field.setAccessible(true);
         return field.getInt(this.threadPool);
      } catch (Exception var3) {
         log.debug("Exception on getInt for " + fieldName + ": " + var3 + " | " + var3.getMessage());
         return 0;
      }
   }

   public static String dumpThreadPoolString(List<Thread> threadList) {
      StringBuilder sb = new StringBuilder();
      sb.append("Dumping Ice Thread Pool: ");
      sb.append(threadList.size());
      sb.append(" threads\n");
      Iterator i$ = threadList.iterator();

      while(i$.hasNext()) {
         Thread t = (Thread)i$.next();
         sb.append(String.format("Thread %s\n", t));
         StackTraceElement[] stackTrace = t.getStackTrace();
         int start = 0;
         int end = 25;
         if (end > stackTrace.length) {
            end = stackTrace.length;
         }

         for(int i = start; i < end; ++i) {
            sb.append("  ");
            sb.append(stackTrace[i].toString());
            sb.append('\n');
         }

         sb.append('\n');
      }

      return sb.toString();
   }

   public void run() {
      int maxThreads = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)this.threadCountProperty);
      if (maxThreads >= 0) {
         if (System.currentTimeMillis() >= this.nextLogTime) {
            List<Thread> copyOfThreads = null;
            synchronized(this.threadPool) {
               int inUse = this.getInt("_inUse");
               if (inUse >= maxThreads) {
                  copyOfThreads = new ArrayList(this.threadList);
               }
            }

            if (copyOfThreads != null) {
               String logString = dumpThreadPoolString(copyOfThreads);
               log.info(logString);
               this.nextLogTime = System.currentTimeMillis() + (long)(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)this.interDumpDurationProperty) * 1000);
            }

         }
      }
   }

   public void start(int checkingIntervalInSeconds) {
      this.timer.schedule(this, 0L, (long)(checkingIntervalInSeconds * 1000));
   }
}
