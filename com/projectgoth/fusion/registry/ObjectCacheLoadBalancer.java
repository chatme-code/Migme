package com.projectgoth.fusion.registry;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.ObjectCacheStats;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class ObjectCacheLoadBalancer {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ObjectCacheLoadBalancer.class));

   public static double getLoad(ObjectCacheStats stats, int filterMask) {
      if (null == stats) {
         return Double.MAX_VALUE;
      } else {
         double load = 0.0D;
         Iterator i$ = Filter.getAdditiveFilters().iterator();

         Filter filter;
         while(i$.hasNext()) {
            filter = (Filter)i$.next();
            if ((filter.getValue() & filterMask) == filter.getValue()) {
               load = filter.getImpl().apply(load, stats);
               if (log.isDebugEnabled()) {
                  log.debug("applied additive filter : " + filter + ": load : " + load);
               }
            }
         }

         if (load > 0.0D) {
            i$ = Filter.getMultiplicativeFilters().iterator();

            while(i$.hasNext()) {
               filter = (Filter)i$.next();
               if ((filter.getValue() & filterMask) == filter.getValue()) {
                  load = filter.getImpl().apply(load, stats);
                  if (log.isDebugEnabled()) {
                     log.debug("applied multiplicative filter : " + filter + ": load : " + load);
                  }
               }
            }
         }

         return load;
      }
   }

   public static double getLoad(ObjectCacheStats stats) {
      return getLoad(stats, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ObjectCacheLoadBalancing.FILTER_MASK));
   }

   public static ObjectCacheRef getLowestLoaded(Collection<ObjectCacheRef> objectCacheRefs) {
      return getLowestLoaded(objectCacheRefs, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ObjectCacheLoadBalancing.FILTER_MASK));
   }

   public static ObjectCacheRef getLowestLoaded(Collection<ObjectCacheRef> objectCaches, int filterMask) {
      try {
         return getLowestLoaded_Distribution(objectCaches, filterMask);
      } catch (Exception var3) {
         log.error("While getting lowest loaded objc: ", var3);
         if (var3.getCause() != null) {
            log.error("with root cause= ", var3.getCause());
         }

         return null;
      }
   }

   private static ObjectCacheRef getLowestLoaded_Distribution(Collection<ObjectCacheRef> objectCaches, int filterMask) {
      List<ObjectCacheRef> objectCacheList = new ArrayList(objectCaches);
      double[] weights = new double[objectCacheList.size()];
      double total = 0.0D;
      int maxTimeInMilliseconds = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ObjectCacheLoadBalancing.OLDEST_ADMISSIBLE_STATS_SECONDS) * 1000;

      for(int i = 0; i < weights.length; ++i) {
         ObjectCacheRef objectCacheRef = (ObjectCacheRef)objectCacheList.get(i);
         double weight;
         if (null != objectCacheRef && objectCacheRef.isOnline()) {
            ObjectCacheStats stats = objectCacheRef.getStats();
            if (stats != null && System.currentTimeMillis() - stats.lastUpdatedTime > (long)maxTimeInMilliseconds) {
               weight = 0.0D;
            } else {
               double load = getLoad(stats, filterMask);
               weight = load < 1.0D ? 1.0D : 1.0D / load;
            }
         } else {
            weight = 0.0D;
         }

         weights[i] = weight;
         total += weight;
      }

      if (total == 0.0D) {
         return null;
      } else {
         double random = Math.random() * total;
         double cumulative = 0.0D;

         int i;
         for(i = 0; i < weights.length; ++i) {
            cumulative += weights[i];
            if (cumulative > random) {
               return (ObjectCacheRef)objectCacheList.get(i);
            }
         }

         for(i = weights.length - 1; i >= 0; --i) {
            if (weights[i] != 0.0D) {
               return (ObjectCacheRef)objectCacheList.get(i);
            }
         }

         log.warn("Unable to find object cache using distribution mechanism");
         return null;
      }
   }
}
