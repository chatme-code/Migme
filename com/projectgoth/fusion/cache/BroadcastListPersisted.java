package com.projectgoth.fusion.cache;

import com.danga.MemCached.MemCachedClient;

public class BroadcastListPersisted {
   public static String getKey(String username) {
      return username;
   }

   public static boolean isBroadcastListPersisted(MemCachedClient instance, String username) {
      return instance.keyExists(username);
   }

   public static boolean setBroadcastListPersisted(MemCachedClient instance, String username, int broadcastListSize) {
      return instance.set(username, broadcastListSize);
   }
}
