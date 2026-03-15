package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface ObjectCacheAdminPrx extends ObjectPrx {
   int ping();

   int ping(Map<String, String> var1);

   ObjectCacheStats getStats() throws FusionException;

   ObjectCacheStats getStats(Map<String, String> var1) throws FusionException;

   String[] getUsernames();

   String[] getUsernames(Map<String, String> var1);

   void reloadEmotes();

   void reloadEmotes(Map<String, String> var1);

   void setLoadWeightage(int var1);

   void setLoadWeightage(int var1, Map<String, String> var2);

   int getLoadWeightage();

   int getLoadWeightage(Map<String, String> var1);
}
