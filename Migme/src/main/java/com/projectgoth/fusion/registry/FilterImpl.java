package com.projectgoth.fusion.registry;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.ObjectCacheStats;

abstract class FilterImpl {
   private FilterType type;

   public abstract double apply(double var1, ObjectCacheStats var3);

   public FilterImpl(FilterType type) {
      this.type = type;
   }

   protected static double getMultiplier(SystemPropertyEntities.ObjectCacheLoadBalancing entry) {
      return SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)entry);
   }

   public FilterType getType() {
      return this.type;
   }
}
