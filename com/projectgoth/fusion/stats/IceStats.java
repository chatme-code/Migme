package com.projectgoth.fusion.stats;

import Ice.Communicator;
import Ice.ObjectAdapter;
import Ice.ObjectAdapterI;
import Ice.Request;
import Ice.Util;
import IceInternal.Instance;
import IceInternal.ThreadPool;
import com.projectgoth.fusion.chatsync.WallclockTime;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.common.LazyStats;
import com.projectgoth.fusion.slice.BaseServiceStats;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;

public class IceStats extends LazyStats {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(IceStats.class));
   private AtomicReference<ObjectAdapter> iceObjectAdapter;
   private AtomicReference<Communicator> iceCommunicator;
   private AtomicReference<ConfigurableExecutor> iceAmdExecutor;
   private volatile long requestCount;
   private ConcurrentHashMap<String, AtomicLong> requestCountByOrigin;
   private ConcurrentHashMap<String, AtomicLong> requestCountByOperation;
   private ConcurrentHashMap<String, WallclockTime> requestProcessingWallclockTimeByOp;
   private static final long HIGH_WATERMARK_RESET_INTERVAL_MILLIS = 900000L;
   private IceStats.ResettingHighWatermark izeObjectAdapterThreadPoolInUseHighWatermark;
   private IceStats.ResettingHighWatermark izeClientThreadPoolInUseHighWatermark;
   private IceStats.ResettingHighWatermark amdObjectAdapterThreadPoolInUseHighWatermark;

   protected boolean isStatsEnabled() {
      return false;
   }

   protected int getStatsIntervalMinutes() {
      return 0;
   }

   protected void doLog() {
   }

   private IceStats() {
      this.iceObjectAdapter = new AtomicReference();
      this.iceCommunicator = new AtomicReference();
      this.iceAmdExecutor = new AtomicReference();
      this.requestCount = 0L;
      this.requestCountByOrigin = new ConcurrentHashMap();
      this.requestCountByOperation = new ConcurrentHashMap();
      this.requestProcessingWallclockTimeByOp = new ConcurrentHashMap();
      this.izeObjectAdapterThreadPoolInUseHighWatermark = new IceStats.ResettingHighWatermark();
      this.izeClientThreadPoolInUseHighWatermark = new IceStats.ResettingHighWatermark();
      this.amdObjectAdapterThreadPoolInUseHighWatermark = new IceStats.ResettingHighWatermark();
   }

   public static IceStats getInstance() {
      return IceStats.SingletonHolder.INSTANCE;
   }

   public void setIceObjects(Communicator communicator, ObjectAdapter adapter, ConfigurableExecutor exec) {
      this.iceObjectAdapter.compareAndSet((Object)null, adapter);
      this.iceCommunicator.compareAndSet((Object)null, communicator);
      this.iceAmdExecutor.compareAndSet((Object)null, exec);
   }

   public void addRequest(Request request) {
      String origin;
      if (request.isCollocated()) {
         origin = "COLLOCATED";
      } else {
         String cxn = request.getCurrent().con.toString();
         String pattern = "remote address = ";
         origin = cxn.substring(cxn.indexOf("remote address = ") + "remote address = ".length());
         origin = origin.substring(0, origin.indexOf(58));
      }

      ++this.requestCount;
      this.incrementRequestCountByOrigin(origin);
      this.incrementRequestCountByOperation(request.getCurrent().operation);
   }

   public void onRequestDispatched(Request request, long startTimeMillis) {
      String op = request.getCurrent().operation;
      if (this.requestProcessingWallclockTimeByOp.get(op) == null) {
         this.requestProcessingWallclockTimeByOp.putIfAbsent(op, new WallclockTime());
      }

      ((WallclockTime)this.requestProcessingWallclockTimeByOp.get(op)).addRequest(System.currentTimeMillis() - startTimeMillis);
   }

   private void incrementRequestCountByOrigin(String origin) {
      if (this.requestCountByOrigin.get(origin) == null) {
         this.requestCountByOrigin.putIfAbsent(origin, new AtomicLong());
      }

      AtomicLong count = (AtomicLong)this.requestCountByOrigin.get(origin);
      count.incrementAndGet();
   }

   private void incrementRequestCountByOperation(String op) {
      if (this.requestCountByOperation.get(op) == null) {
         this.requestCountByOperation.putIfAbsent(op, new AtomicLong());
      }

      AtomicLong count = (AtomicLong)this.requestCountByOperation.get(op);
      count.incrementAndGet();
   }

   public Map<String, Long> getRequestCountByOrigin() {
      return this.cloneMap(this.requestCountByOrigin);
   }

   public Map<String, Long> getRequestCountByOperation() {
      return this.cloneMap(this.requestCountByOperation);
   }

   private Map<String, Long> cloneMap(ConcurrentHashMap<String, AtomicLong> source) {
      HashMap<String, Long> clone = new HashMap();
      Set<String> keys = source.keySet();
      Iterator i$ = keys.iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         clone.put(key, ((AtomicLong)source.get(key)).longValue());
      }

      return clone;
   }

   private Map<String, Double> cloneMeans(ConcurrentHashMap<String, WallclockTime> source) {
      HashMap<String, Double> clone = new HashMap();
      Set<String> keys = source.keySet();
      Iterator i$ = keys.iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         clone.put(key, ((WallclockTime)source.get(key)).getAverageWallclockTime());
      }

      return clone;
   }

   private Map<String, Double> cloneMaxes(ConcurrentHashMap<String, WallclockTime> source) {
      HashMap<String, Double> clone = new HashMap();
      Set<String> keys = source.keySet();
      Iterator i$ = keys.iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         clone.put(key, ((WallclockTime)source.get(key)).getMaxWallclockTime());
      }

      return clone;
   }

   private Map<String, Double> cloneStdevs(ConcurrentHashMap<String, WallclockTime> source) {
      HashMap<String, Double> clone = new HashMap();
      Set<String> keys = source.keySet();
      Iterator i$ = keys.iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         clone.put(key, ((WallclockTime)source.get(key)).getRunningStdev());
      }

      return clone;
   }

   private Map<String, Double> clone95thPercentiles(ConcurrentHashMap<String, WallclockTime> source) {
      HashMap<String, Double> clone = new HashMap();
      Set<String> keys = source.keySet();
      Iterator i$ = keys.iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         clone.put(key, ((WallclockTime)source.get(key)).getStats().getPercentile95th());
      }

      return clone;
   }

   private Map<String, Double> cloneTotals(ConcurrentHashMap<String, WallclockTime> source) {
      HashMap<String, Double> clone = new HashMap();
      Set<String> keys = source.keySet();
      Iterator i$ = keys.iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         clone.put(key, ((WallclockTime)source.get(key)).getTotalWallclockTime());
      }

      return clone;
   }

   public <TStats extends BaseServiceStats> void initializeRequestStats(TStats stats) {
      stats.izeRequestCount = this.requestCount;
      stats.izeRequestCountByOrigin = this.getRequestCountByOrigin();
      stats.izeRequestCountByOperation = this.getRequestCountByOperation();
      stats.izeRequestMeanProcessingTimeByOperation = this.cloneMeans(this.requestProcessingWallclockTimeByOp);
      stats.izeRequestMaxProcessingTimeByOperation = this.cloneMaxes(this.requestProcessingWallclockTimeByOp);
      stats.izeRequestStdevProcessingTimeByOperation = this.cloneStdevs(this.requestProcessingWallclockTimeByOp);
      stats.izeRequest95thPercentileProcessingTimeByOperation = this.clone95thPercentiles(this.requestProcessingWallclockTimeByOp);
      stats.izeRequestTotalProcessingTimeByOperation = this.cloneTotals(this.requestProcessingWallclockTimeByOp);
   }

   public <TStats extends BaseServiceStats> void initializeThreadStats(TStats stats) {
      ThreadPool iceClientPool;
      IceStats.ThreadPoolPrivateFields fields;
      if (this.iceObjectAdapter.get() != null) {
         try {
            ObjectAdapterI oai = (ObjectAdapterI)this.iceObjectAdapter.get();
            iceClientPool = oai.getThreadPool();
            fields = new IceStats.ThreadPoolPrivateFields(iceClientPool);
            fields.setObjectAdapterThreadStats(stats);
         } catch (Exception var6) {
            log.error("Unable to initialize ice objectadapter threadpool stats: " + var6, var6);
         }
      }

      if (this.iceCommunicator.get() != null) {
         try {
            Instance instance = Util.getInstance((Communicator)this.iceCommunicator.get());
            iceClientPool = instance.clientThreadPool();
            fields = new IceStats.ThreadPoolPrivateFields(iceClientPool);
            fields.setClientThreadPoolStats(stats);
         } catch (Exception var5) {
            log.error("Unable to initialize ice client threadpool stats: " + var5, var5);
         }
      }

      if (this.iceAmdExecutor.get() != null) {
         stats.amdObjectAdapterThreadPoolRunning = (long)((ConfigurableExecutor)this.iceAmdExecutor.get()).getPoolSize();
         int inUse = ((ConfigurableExecutor)this.iceAmdExecutor.get()).getActiveCount();
         stats.amdObjectAdapterThreadPoolInUse = (long)inUse;
         this.amdObjectAdapterThreadPoolInUseHighWatermark.casIfHigher(inUse);
         stats.amdObjectAdapterThreadPoolInUseHighWatermark = (long)this.amdObjectAdapterThreadPoolInUseHighWatermark.get();
         stats.amdObjectAdapterThreadPoolSize = (long)((ConfigurableExecutor)this.iceAmdExecutor.get()).getCorePoolSize();
         stats.amdObjectAdapterThreadPoolSizeMax = (long)((ConfigurableExecutor)this.iceAmdExecutor.get()).getMaximumPoolSize();
         stats.amdObjectAdapterThreadPoolQueueLength = (long)((ConfigurableExecutor)this.iceAmdExecutor.get()).getQueueLength();
      }

   }

   // $FF: synthetic method
   IceStats(Object x0) {
      this();
   }

   private static class SingletonHolder {
      public static final IceStats INSTANCE = new IceStats();
   }

   private class ThreadPoolPrivateFields {
      private ThreadPool pool;
      private int running;
      private int inUse;
      private double load;
      private int size;
      private int sizeMax;
      private int sizeWarn;

      public ThreadPoolPrivateFields(ThreadPool pool) throws Exception {
         this.pool = pool;
         this.running = this.getInt("_running");
         this.inUse = this.getInt("_inUse");
         this.load = this.getDouble("_load");
         this.size = this.getInt("_size");
         this.sizeMax = this.getInt("_sizeMax");
         this.sizeWarn = this.getInt("_sizeWarn");
      }

      private int getInt(String name) throws Exception {
         Field f = ThreadPool.class.getDeclaredField(name);
         f.setAccessible(true);
         return f.getInt(this.pool);
      }

      private double getDouble(String name) throws Exception {
         Field f = ThreadPool.class.getDeclaredField(name);
         f.setAccessible(true);
         return f.getDouble(this.pool);
      }

      public <TStats extends BaseServiceStats> void setObjectAdapterThreadStats(TStats stats) throws Exception {
         stats.izeThreadStatsEnabled = true;
         stats.izeObjectAdapterThreadPoolRunning = (long)this.running;
         stats.izeObjectAdapterThreadPoolInUse = (long)this.inUse;
         stats.izeObjectAdapterThreadPoolLoad = this.load;
         stats.izeObjectAdapterThreadPoolSize = (long)this.size;
         stats.izeObjectAdapterThreadPoolSizeMax = (long)this.sizeMax;
         stats.izeObjectAdapterThreadPoolSizeWarn = (long)this.sizeWarn;
         IceStats.this.izeObjectAdapterThreadPoolInUseHighWatermark.casIfHigher(this.inUse);
         stats.izeObjectAdapterThreadPoolInUseHighWatermark = (long)IceStats.this.izeObjectAdapterThreadPoolInUseHighWatermark.get();
      }

      public <TStats extends BaseServiceStats> void setClientThreadPoolStats(TStats stats) throws Exception {
         stats.izeClientThreadPoolStatsEnabled = true;
         stats.izeClientThreadPoolRunning = (long)this.running;
         stats.izeClientThreadPoolInUse = (long)this.inUse;
         stats.izeClientThreadPoolLoad = this.load;
         stats.izeClientThreadPoolSize = (long)this.size;
         stats.izeClientThreadPoolSizeMax = (long)this.sizeMax;
         stats.izeClientThreadPoolSizeWarn = (long)this.sizeWarn;
         IceStats.this.izeClientThreadPoolInUseHighWatermark.casIfHigher(this.inUse);
         stats.izeClientThreadPoolInUseHighWatermark = (long)IceStats.this.izeClientThreadPoolInUseHighWatermark.get();
      }
   }

   private class ResettingHighWatermark {
      private AtomicInteger highWatermark;
      private AtomicLong lastResetTime;

      private ResettingHighWatermark() {
         this.highWatermark = new AtomicInteger(0);
         this.lastResetTime = new AtomicLong(System.currentTimeMillis());
      }

      public int get() {
         return this.highWatermark.get();
      }

      private void casIfHigher(int newValue) {
         int current = this.highWatermark.get();
         long lastResetLocal = this.lastResetTime.get();
         boolean reset = false;
         long now = System.currentTimeMillis();
         if (now - lastResetLocal > 900000L) {
            reset = this.lastResetTime.compareAndSet(lastResetLocal, now);
         }

         if (newValue > current || reset) {
            this.highWatermark.compareAndSet(current, newValue);
         }

      }

      // $FF: synthetic method
      ResettingHighWatermark(Object x1) {
         this();
      }
   }
}
