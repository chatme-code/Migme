package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _ObjectCacheAdminOperations {
   int ping(Current var1);

   ObjectCacheStats getStats(Current var1) throws FusionException;

   String[] getUsernames(Current var1);

   void reloadEmotes(Current var1);

   void setLoadWeightage(int var1, Current var2);

   int getLoadWeightage(Current var1);
}
