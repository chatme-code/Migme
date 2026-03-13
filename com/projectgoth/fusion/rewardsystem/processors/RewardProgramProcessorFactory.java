package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.RewardProgramProcessorMappingData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class RewardProgramProcessorFactory {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RewardProgramProcessorFactory.class));
   private static Map<RewardProgramData.TypeEnum, List<RewardProgramProcessor>> procInstanceMap = new ConcurrentHashMap();
   private static Semaphore semaphore = new Semaphore(1);
   private static long lastUpdated;
   private static final int LOCAL_CACHE_TIME_IN_MS = 60000;
   private static boolean refreshPeriodically = true;

   public static void setRewardProgramProcessorMap(Map<RewardProgramData.TypeEnum, List<RewardProgramProcessor>> map) {
      refreshPeriodically = false;
      procInstanceMap = map;
   }

   public static void resetProgramProcessorMap() {
      refreshPeriodically = true;
      lastUpdated = 0L;
      procInstanceMap.clear();
   }

   private static void loadProcessors() {
      if (lastUpdated == 0L) {
         semaphore.acquireUninterruptibly();
      } else if (!semaphore.tryAcquire()) {
         return;
      }

      try {
         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         List<RewardProgramProcessorMappingData> mapping = contentEJB.getRewardProgramProcessorMapping();
         if (mapping != null) {
            log.debug("Found [" + mapping.size() + "] mappings from contentBean");
            Iterator i$ = mapping.iterator();

            while(i$.hasNext()) {
               RewardProgramProcessorMappingData m = (RewardProgramProcessorMappingData)i$.next();
               RewardProgramData.TypeEnum type = m.getProgramType();
               log.debug("Preparing processor list for type [" + type + "]");
               List<RewardProgramProcessor> processors = new LinkedList();
               log.debug("Found processor list: " + m.getProcessorList());
               Iterator i$ = m.getProcessorList().iterator();

               while(i$.hasNext()) {
                  String clazzName = (String)i$.next();
                  RewardProgramProcessor p = instantiate(clazzName);
                  log.debug("Adding :" + p);
                  if (p != null) {
                     processors.add(p);
                  }
               }

               log.debug("Completed adding [" + processors.size() + "] processors for [" + type + "]");
               procInstanceMap.put(type, processors);
            }
         }

         lastUpdated = System.currentTimeMillis();
      } catch (Exception var13) {
         log.error("Unable to load reward program processors", var13);
      } finally {
         semaphore.release();
      }

   }

   public static synchronized List<RewardProgramProcessor> getProcessorsForProgramType(RewardProgramData.TypeEnum type) {
      log.debug("refreshPeriodically[" + refreshPeriodically + "] lastUpdated[" + lastUpdated + "] ");
      if (refreshPeriodically && System.currentTimeMillis() - lastUpdated > 60000L) {
         log.debug("Reloading from the database");
         loadProcessors();
      }

      List<RewardProgramProcessor> list = (List)procInstanceMap.get(type);
      if (list == null) {
         log.debug("no processors configured for [" + type + "] returning an empty list");
         list = new LinkedList();
      }

      return (List)list;
   }

   private static RewardProgramProcessor instantiate(String className) {
      try {
         return (RewardProgramProcessor)RewardProgramProcessor.class.cast(Class.forName(className).newInstance());
      } catch (Exception var2) {
         log.error("Unable to load rewardprogramprocessor [" + className + "] :" + var2.getMessage(), var2);
         return null;
      }
   }
}
