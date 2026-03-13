package com.projectgoth.fusion.registry;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.ObjectCacheStats;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

enum Filter {
   USER_OBJECT_FILTER(new FilterImpl(FilterType.ADDITIVE) {
      public double apply(double previous, ObjectCacheStats stats) {
         return previous + getMultiplier(SystemPropertyEntities.ObjectCacheLoadBalancing.USER_OBJECT_MULTIPLIER) * (double)stats.numUserObjects;
      }
   }, 1),
   SESSION_OBJECT_FILTER(new FilterImpl(FilterType.ADDITIVE) {
      public double apply(double previous, ObjectCacheStats stats) {
         return previous + getMultiplier(SystemPropertyEntities.ObjectCacheLoadBalancing.SESSION_OBJECT_MULTIPLIER) * (double)stats.numSessionObjects;
      }
   }, 2),
   GROUP_CHAT_FILTER(new FilterImpl(FilterType.ADDITIVE) {
      public double apply(double previous, ObjectCacheStats stats) {
         return previous + getMultiplier(SystemPropertyEntities.ObjectCacheLoadBalancing.GC_MULTIPLIER) * (double)stats.numGroupChatObjects;
      }
   }, 4),
   CHAT_ROOM_FILTER(new FilterImpl(FilterType.ADDITIVE) {
      public double apply(double previous, ObjectCacheStats stats) {
         return previous + getMultiplier(SystemPropertyEntities.ObjectCacheLoadBalancing.CHATROOM_MULTIPLIER) * (double)stats.numChatRoomObjects;
      }
   }, 8),
   WEIGHTAGE_FILTER(new FilterImpl(FilterType.MULTIPLICATIVE) {
      public double apply(double previous, ObjectCacheStats stats) {
         if (stats.weightage > 0) {
            return previous / (double)stats.weightage;
         } else {
            Filter.log.error("invalid weightage supplied" + stats.weightage);
            return previous;
         }
      }
   }, 16),
   MEMORY_UTILIZATION_FILTER(new FilterImpl(FilterType.MULTIPLICATIVE) {
      public double apply(double previous, ObjectCacheStats stats) {
         double utilization = getMultiplier(SystemPropertyEntities.ObjectCacheLoadBalancing.MEMORY_UTILIZATION_MULTIPLIER) * (double)stats.jvmFreeMemory / (double)stats.jvmTotalMemory;
         return utilization > 0.0D ? previous / utilization : previous / Math.abs(Double.MIN_VALUE);
      }
   }, 32);

   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ObjectCacheLoadBalancer.class));
   private FilterImpl filter;
   private int value;
   private static Set<Filter> additives = new HashSet();
   private static Set<Filter> multiplicatives = new HashSet();

   private Filter(FilterImpl filter, int value) {
      this.filter = filter;
      this.value = value;
   }

   public static Set<Filter> getAdditiveFilters() {
      return additives;
   }

   public static Set<Filter> getMultiplicativeFilters() {
      return multiplicatives;
   }

   public FilterImpl getImpl() {
      return this.filter;
   }

   public int getValue() {
      return this.value;
   }

   static {
      Filter[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Filter filter = arr$[i$];
         switch(filter.getImpl().getType()) {
         case ADDITIVE:
            additives.add(filter);
            break;
         case MULTIPLICATIVE:
            multiplicatives.add(filter);
         }
      }

   }
}
