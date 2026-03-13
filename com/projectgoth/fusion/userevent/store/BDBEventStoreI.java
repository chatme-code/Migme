package com.projectgoth.fusion.userevent.store;

import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RequestAndRateLongCounter;
import com.projectgoth.fusion.slice.EventPrivacySettingIce;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice._EventStoreDisp;
import com.projectgoth.fusion.userevent.domain.EventPrivacySetting;
import com.projectgoth.fusion.userevent.domain.UserEvent;
import com.projectgoth.fusion.userevent.domain.UserEventFactory;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

public class BDBEventStoreI extends _EventStoreDisp implements EventStoreStatistics, InitializingBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BDBEventStoreI.class));
   private static final int MAXIMUM_LOCKS_CAPACITY = 1000;
   public static final int TWO_WEEKS_MS = 1209600000;
   private String environmentHome;
   private boolean allowStoreCreation;
   private int eventsPerUser = 10;
   private int bdbCachePercent = 25;
   private boolean useBuffer = true;
   private boolean printStats = false;
   private int bufferCapacity = 100000;
   private Environment environment;
   private EntityStore feedStore;
   private EntityStore generatedStore;
   private StoreUserEventDA storeUserEventDA;
   private StoreGeneratorEventDA storeGeneratorEventDA;
   private BDBEventStoreI.StatsThread statsThread;
   private BDBEventStoreI.PersistThread persistThread;
   private Map<String, StoreUserEvent> buffer;
   private Map<String, ReentrantLock> locks;
   private ConcurrentMap<String, StoreUserEvent> persistBuffer;
   private RequestAndRateLongCounter eventsCounter;
   private RequestAndRateLongCounter generatorEventsCounter;
   private RequestAndRateLongCounter cacheExpiredCounter;
   private RequestAndRateLongCounter persistBufferCounter;

   public void afterPropertiesSet() throws Exception {
      this.eventsCounter = new RequestAndRateLongCounter(1);
      this.generatorEventsCounter = new RequestAndRateLongCounter(1);
      this.cacheExpiredCounter = new RequestAndRateLongCounter(1);
      this.persistBufferCounter = new RequestAndRateLongCounter(1);
      this.persistBuffer = new ConcurrentHashMap();
      this.buffer = new LinkedHashMap<String, StoreUserEvent>(this.bufferCapacity + 10) {
         protected boolean removeEldestEntry(Entry<String, StoreUserEvent> eldest) {
            if (this.size() > BDBEventStoreI.this.bufferCapacity) {
               BDBEventStoreI.this.persistBuffer.put(eldest.getKey(), eldest.getValue());
               BDBEventStoreI.this.persistBufferCounter.add();
               return true;
            } else {
               return false;
            }
         }
      };
      this.locks = new LinkedHashMap<String, ReentrantLock>(1000) {
         protected boolean removeEldestEntry(Entry<String, ReentrantLock> eldest) {
            return this.size() > 1000;
         }
      };
      this.setup(new File(this.environmentHome), this.allowStoreCreation);
   }

   public UserEventIce[] getUserEventsForUser(String username, Current __current) throws FusionException {
      UserEventIce[] array = null;

      try {
         StoreUserEvent userEvent = null;
         userEvent = (StoreUserEvent)this.persistBuffer.remove(username);
         if (this.useBuffer && userEvent == null) {
            synchronized(this.buffer) {
               userEvent = (StoreUserEvent)this.buffer.get(username);
            }
         }

         if (userEvent == null) {
            log.debug("falling back to disk");
            userEvent = (StoreUserEvent)this.storeUserEventDA.primaryIndex.get(username);
         }

         if (userEvent != null && userEvent.getEvents() != null) {
            synchronized(userEvent) {
               List<UserEvent> list = userEvent.getEvents();
               array = new UserEventIce[list.size()];
               int index = 0;

               UserEventIce userEventIce;
               for(Iterator i$ = list.iterator(); i$.hasNext(); array[index++] = userEventIce) {
                  UserEvent event = (UserEvent)i$.next();
                  if (log.isDebugEnabled()) {
                     log.debug("Getting event [" + event + "] from storage for user [" + username + "] to send back");
                  }

                  userEventIce = event.toIceEvent();
               }

               return array;
            }
         } else {
            if (log.isDebugEnabled()) {
               log.debug("nothing stored yet for user [" + username + "]");
            }

            return null;
         }
      } catch (Exception var14) {
         log.error("Failed to read events for user [" + username + "]", var14);
         throw new FusionException("Failed to read events for user");
      }
   }

   public UserEventIce[] getUserEventsGeneratedByUser(String username, Current __current) throws FusionException {
      UserEventIce[] array = null;

      try {
         StoreGeneratorEvent generatorEvent = null;
         generatorEvent = (StoreGeneratorEvent)this.storeGeneratorEventDA.primaryIndex.get(username);
         if (generatorEvent != null && generatorEvent.getEvents() != null) {
            ReentrantLock lock = null;
            synchronized(this.locks) {
               if (!this.locks.containsKey(username)) {
                  this.locks.put(username, new ReentrantLock());
               }

               lock = (ReentrantLock)this.locks.get(username);
            }

            try {
               lock.lock();
               List<UserEvent> list = generatorEvent.getEvents();
               array = new UserEventIce[list.size()];
               int index = 0;
               Iterator i$ = list.iterator();

               while(i$.hasNext()) {
                  UserEvent event = (UserEvent)i$.next();
                  if (!event.getClass().equals(UserEvent.class)) {
                     UserEventIce iceEvent = event.toIceEvent();
                     array[index++] = iceEvent;
                  }
               }

               if (index < list.size()) {
                  log.debug("found dodgy entry, recreating a shorter array");
                  UserEventIce[] shorterArray = new UserEventIce[index];
                  System.arraycopy(array, 0, shorterArray, 0, index);
                  UserEventIce[] var20 = shorterArray;
                  return var20;
               }
            } finally {
               lock.unlock();
            }

            return array;
         } else {
            if (log.isDebugEnabled()) {
               log.debug("nothing stored yet for user [" + username + "]");
            }

            return new UserEventIce[0];
         }
      } catch (Exception var18) {
         log.error("Failed to read generator events for user [" + username + "]", var18);
         throw new FusionException("Failed to read generator events for user");
      }
   }

   private void transactionallyPersistEvent(String username, UserEvent event) throws FusionException {
      StoreUserEvent storeUserEvent = null;
      Transaction transaction = null;

      try {
         transaction = this.environment.beginTransaction((Transaction)null, (TransactionConfig)null);
         storeUserEvent = (StoreUserEvent)this.storeUserEventDA.primaryIndex.get(transaction, username, LockMode.RMW);
         storeUserEvent.addEvent(this.eventsPerUser, event);
         this.storeUserEventDA.primaryIndex.put(transaction, storeUserEvent);
         transaction.commit();
      } catch (DatabaseException var8) {
         log.error("failed to store events " + storeUserEvent + "]", var8);
         if (transaction != null) {
            try {
               transaction.abort();
            } catch (DatabaseException var7) {
               log.error("failed to roll back transaction", var8);
            }
         }

         throw new FusionException("failed to store events");
      }
   }

   private StoreUserEvent getStoreUserEventFromDB(String username) {
      try {
         return (StoreUserEvent)this.storeUserEventDA.primaryIndex.get(username);
      } catch (DatabaseException var3) {
         log.error("failed to read events from BDB for user [" + username + "]", var3);
         return null;
      }
   }

   private void addEventToBuffer(String username, UserEvent event) {
      StoreUserEvent storeUserEvent = null;
      storeUserEvent = (StoreUserEvent)this.persistBuffer.remove(username);
      synchronized(this.buffer) {
         if (storeUserEvent != null) {
            this.buffer.put(username, storeUserEvent);
         } else if (!this.buffer.containsKey(username)) {
            this.buffer.put(username, new StoreUserEvent(username));
         }

         storeUserEvent = (StoreUserEvent)this.buffer.get(username);
      }

      synchronized(storeUserEvent) {
         if (storeUserEvent.getEvents() == null) {
            StoreUserEvent forUser = this.getStoreUserEventFromDB(username);
            if (forUser != null) {
               storeUserEvent.setEvents(forUser.getEvents());
            }
         }

         storeUserEvent.addEvent(this.eventsPerUser, event);
      }
   }

   public void storeUserEvent(String username, UserEventIce event, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("storing user event [" + event + "] for user [" + username + "]");
         }

         if (!this.useBuffer) {
            this.transactionallyPersistEvent(username, UserEventFactory.getUserEvent(event));
         } else {
            this.addEventToBuffer(username, UserEventFactory.getUserEvent(event));
         }
      } finally {
         this.eventsCounter.add();
      }

   }

   public void storeGeneratorEvent(String username, UserEventIce event, Current __current) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("storing generator event [" + event + "] for user [" + username + "]");
      }

      ReentrantLock lock = null;
      synchronized(this.locks) {
         if (!this.locks.containsKey(username)) {
            this.locks.put(username, new ReentrantLock());
         }

         lock = (ReentrantLock)this.locks.get(username);
      }

      try {
         lock.lock();
         StoreGeneratorEvent storeGeneratorEvent = (StoreGeneratorEvent)this.storeGeneratorEventDA.primaryIndex.get(username);
         if (storeGeneratorEvent == null) {
            storeGeneratorEvent = new StoreGeneratorEvent(username);
         }

         storeGeneratorEvent.addEvent(this.eventsPerUser, UserEventFactory.getUserEvent(event));
         this.storeGeneratorEventDA.primaryIndex.put(storeGeneratorEvent);
      } catch (Exception var12) {
         log.error("failed to store generated event ", var12);
         throw new FusionException("Failed to store generated event for user [" + username + "]");
      } finally {
         lock.unlock();
         this.generatorEventsCounter.add();
      }

   }

   public void deleteUserEvents(String username, Current __current) throws FusionException {
      try {
         this.storeUserEventDA.primaryIndex.delete(username);
         this.storeGeneratorEventDA.primaryIndex.delete(username);
      } catch (DatabaseException var4) {
         log.error("failed to delete events for user [" + username + "]", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public EventPrivacySettingIce getPublishingPrivacyMask(String username, Current __current) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("getting publishing mask for generator [" + username + "]");
      }

      ReentrantLock lock = null;
      synchronized(this.locks) {
         if (!this.locks.containsKey(username)) {
            this.locks.put(username, new ReentrantLock());
         }

         lock = (ReentrantLock)this.locks.get(username);
      }

      EventPrivacySettingIce var5;
      try {
         lock.lock();
         StoreGeneratorEvent storeGeneratorEvent = (StoreGeneratorEvent)this.storeGeneratorEventDA.primaryIndex.get(username);
         if (storeGeneratorEvent != null) {
            var5 = storeGeneratorEvent.getPublishingMask().toEventPrivacySettingIce();
            return var5;
         }

         var5 = (new EventPrivacySetting()).toEventPrivacySettingIce();
      } catch (Exception var11) {
         log.error("failed to retrieve publishing mask for user [" + username + "]", var11);
         throw new FusionException("Failed to retrieve publishing mask for user [" + username + "]");
      } finally {
         lock.unlock();
      }

      return var5;
   }

   public EventPrivacySettingIce getReceivingPrivacyMask(String username, Current __current) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("getting receiving mask for user [" + username + "]");
      }

      try {
         StoreUserEvent userEvent = null;
         userEvent = (StoreUserEvent)this.persistBuffer.remove(username);
         if (this.useBuffer && userEvent == null) {
            synchronized(this.buffer) {
               userEvent = (StoreUserEvent)this.buffer.get(username);
            }
         }

         if (userEvent == null) {
            log.debug("falling back to disk");
            userEvent = (StoreUserEvent)this.storeUserEventDA.primaryIndex.get(username);
            if (userEvent == null) {
               return (new EventPrivacySetting()).toEventPrivacySettingIce();
            }
         }

         synchronized(userEvent) {
            return userEvent.getReceivingMask().toEventPrivacySettingIce();
         }
      } catch (Exception var9) {
         log.error("failed to load receiving mask for user [" + username + "]", var9);
         return (new EventPrivacySetting()).toEventPrivacySettingIce();
      }
   }

   public void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask, Current __current) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("setting publishing mask for generator [" + username + "]");
      }

      ReentrantLock lock = null;
      synchronized(this.locks) {
         if (!this.locks.containsKey(username)) {
            this.locks.put(username, new ReentrantLock());
         }

         lock = (ReentrantLock)this.locks.get(username);
      }

      try {
         lock.lock();
         StoreGeneratorEvent storeGeneratorEvent = (StoreGeneratorEvent)this.storeGeneratorEventDA.primaryIndex.get(username);
         if (storeGeneratorEvent == null) {
            storeGeneratorEvent = new StoreGeneratorEvent(username, EventPrivacySetting.fromEventPrivacySettingIce(mask));
         } else {
            storeGeneratorEvent.setPublishingMask(EventPrivacySetting.fromEventPrivacySettingIce(mask));
         }

         this.storeGeneratorEventDA.primaryIndex.put(storeGeneratorEvent);
      } catch (Exception var12) {
         log.error("failed to retrieve publishing mask for user [" + username + "]", var12);
         throw new FusionException("Failed to retrieve publishing mask for user [" + username + "]");
      } finally {
         lock.unlock();
      }

   }

   public void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask, Current __current) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("setting receiving mask for user [" + username + "]");
      }

      StoreUserEvent storeUserEvent = null;
      storeUserEvent = (StoreUserEvent)this.persistBuffer.remove(username);
      synchronized(this.buffer) {
         if (storeUserEvent != null) {
            this.buffer.put(username, storeUserEvent);
         } else if (!this.buffer.containsKey(username)) {
            this.buffer.put(username, new StoreUserEvent(username));
         }

         storeUserEvent = (StoreUserEvent)this.buffer.get(username);
      }

      synchronized(storeUserEvent) {
         if (storeUserEvent.getEvents() == null) {
            StoreUserEvent forUser = this.getStoreUserEventFromDB(username);
            if (forUser != null) {
               storeUserEvent.setEvents(forUser.getEvents());
            }
         }

         storeUserEvent.setReceivingMask(EventPrivacySetting.fromEventPrivacySettingIce(mask));
      }
   }

   void persistToBDB(String username, StoreUserEvent storeUserEvent) {
      if (log.isDebugEnabled()) {
         log.info("persisting StoreUserEvent to BDB [" + storeUserEvent + "]");
      }

      try {
         this.storeUserEventDA.primaryIndex.put(storeUserEvent);
      } catch (DatabaseException var8) {
         log.error("Failed to store events for user [" + username + "]", var8);
      } finally {
         this.cacheExpiredCounter.add();
      }

   }

   public long freeMemory() {
      return 0L;
   }

   private void setup(File environmentHome, boolean allowStoreCreation) {
      try {
         log.info("EnvironmentHome[" + environmentHome.toString() + "] allowStoreCreation[" + allowStoreCreation + "]");
         EnvironmentConfig environmentConfig = new EnvironmentConfig();
         StoreConfig storeConfig = new StoreConfig();
         if (!this.useBuffer) {
            environmentConfig.setTransactional(true);
            environmentConfig.setTxnSerializableIsolation(true);
            storeConfig.setTransactional(true);
         }

         environmentConfig.setAllowCreate(allowStoreCreation);
         environmentConfig.setCachePercent(this.bdbCachePercent);
         storeConfig.setAllowCreate(allowStoreCreation);
         this.environment = new Environment(environmentHome, environmentConfig);
         this.feedStore = new EntityStore(this.environment, "FeedStore", storeConfig);
         this.generatedStore = new EntityStore(this.environment, "GeneratedStore", storeConfig);
         this.storeUserEventDA = new StoreUserEventDA(this.feedStore);
         this.storeGeneratorEventDA = new StoreGeneratorEventDA(this.generatedStore);
         log.info("Store opened successfully");
         log.info("Maximum cache memory: " + (int)((double)environmentConfig.getCachePercent() / 100.0D * (double)Runtime.getRuntime().maxMemory() / 1000000.0D) + " MB");
         if (this.printStats) {
            this.statsThread = new BDBEventStoreI.StatsThread(this.environment, this.eventsCounter);
            this.statsThread.start();
         }

         this.persistThread = new BDBEventStoreI.PersistThread(this.persistBuffer, this);
         this.persistThread.start();
      } catch (DatabaseException var5) {
         log.fatal("Error opening environment and store, exiting... ", var5);
      }

   }

   private void persistBufferToBDB() {
      log.info("Stand by... we're flushing the buffer to disk, this can take up to a few minutes...");
      synchronized(this.buffer) {
         int total = this.buffer.keySet().size();
         int count = 0;
         Iterator i$ = this.buffer.keySet().iterator();

         while(i$.hasNext()) {
            String username = (String)i$.next();

            try {
               if (log.isDebugEnabled()) {
                  log.debug("persisting " + this.buffer.get(username));
               }

               this.storeUserEventDA.primaryIndex.put(this.buffer.get(username));
            } catch (DatabaseException var8) {
               log.error("failed to persist buffer event for user [" + username + "]");
            }

            if (count++ == total / 2) {
               log.info("half way done flushing buffer to disk, " + (total - count) + " remaining");
            }
         }
      }

      log.info("Done flushing buffer");
   }

   public void shutdownStore() {
      log.info("Shutting down " + this.getClass().getCanonicalName());
      if (this.printStats) {
         this.statsThread.stopRunning();
      }

      this.persistThread.stopRunning();
      if (this.useBuffer) {
         this.persistBufferToBDB();
      }

      try {
         if (this.feedStore != null) {
            this.feedStore.sync();
            this.feedStore.close();
         }

         if (this.generatedStore != null) {
            this.generatedStore.sync();
            this.generatedStore.close();
         }
      } catch (DatabaseException var3) {
         log.fatal("Error closing store, exiting... ", var3);
         return;
      }

      if (this.environment != null) {
         try {
            this.environment.close();
         } catch (DatabaseException var2) {
            log.fatal("Error closing Environment, exiting... ", var2);
            return;
         }
      }

   }

   public void setEnvironmentHome(String environmentHome) {
      this.environmentHome = environmentHome;
   }

   public void setAllowStoreCreation(boolean allowStoreCreation) {
      this.allowStoreCreation = allowStoreCreation;
   }

   public void setEventsPerUser(int eventsPerUser) {
      this.eventsPerUser = eventsPerUser;
   }

   public void setBdbCachePercent(int bdbCachePercent) {
      this.bdbCachePercent = bdbCachePercent;
   }

   public void setBufferCapacity(int bufferCapacity) {
      this.bufferCapacity = bufferCapacity;
   }

   public Environment getEnvironment() {
      return this.environment;
   }

   public EntityStore getFeedStore() {
      return this.feedStore;
   }

   public Map<String, StoreUserEvent> getBuffer() {
      return this.buffer;
   }

   public ConcurrentMap<String, StoreUserEvent> getPersistBuffer() {
      return this.persistBuffer;
   }

   public RequestAndRateLongCounter getEventsCounter() {
      return this.eventsCounter;
   }

   public RequestAndRateLongCounter getGeneratorEventsCounter() {
      return this.generatorEventsCounter;
   }

   public RequestAndRateLongCounter getCacheExpiredCounter() {
      return this.cacheExpiredCounter;
   }

   public RequestAndRateLongCounter getPersistBufferCounter() {
      return this.persistBufferCounter;
   }

   class StatsThread extends Thread {
      private Environment environment;
      private RequestAndRateLongCounter counter;
      private boolean running = true;

      public StatsThread(Environment environment, RequestAndRateLongCounter counter) {
         this.environment = environment;
         this.counter = counter;
      }

      public void stopRunning() {
         this.running = false;
      }

      public void run() {
         int var1 = 0;

         while(this.running) {
            try {
               sleep(10000L);
               if (var1++ % 6 == 0) {
                  BDBEventStoreI.log.info(this.environment.getStats((StatsConfig)null).toString());
               }

               BDBEventStoreI.log.info(this.counter.toString());
            } catch (Exception var3) {
               BDBEventStoreI.log.error("failed to get stats", var3);
            }
         }

      }
   }

   class PersistThread extends Thread {
      private ConcurrentMap<String, StoreUserEvent> persistBuffer;
      private BDBEventStoreI store;
      private boolean running = true;

      public PersistThread(ConcurrentMap<String, StoreUserEvent> persistBuffer, BDBEventStoreI store) {
         this.persistBuffer = persistBuffer;
         this.store = store;
      }

      public void stopRunning() {
         BDBEventStoreI.log.info("explicitely stopping PersistThread");
         this.running = false;
      }

      public void run() {
         try {
            while(this.running) {
               try {
                  while(!this.persistBuffer.isEmpty()) {
                     Iterator i$ = this.persistBuffer.keySet().iterator();

                     while(i$.hasNext()) {
                        String username = (String)i$.next();
                        StoreUserEvent event = (StoreUserEvent)this.persistBuffer.remove(username);
                        if (event != null) {
                           this.store.persistToBDB(username, event);
                        } else {
                           BDBEventStoreI.log.debug("event removed from persistBuffer was null");
                        }
                     }

                     Thread.sleep(500L);
                  }

                  Thread.sleep(1000L);
               } catch (InterruptedException var8) {
                  BDBEventStoreI.log.error("interrupted?", var8);
               }
            }
         } finally {
            BDBEventStoreI.log.warn("Exiting PersistThread...");
         }

      }
   }
}
