package com.projectgoth.fusion.slice;

public interface _ObjectCacheAdminOperationsNC {
   int ping();

   ObjectCacheStats getStats() throws FusionException;

   String[] getUsernames();

   void reloadEmotes();

   void setLoadWeightage(int var1);

   int getLoadWeightage();
}
