package com.projectgoth.fusion.cache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedUtils;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class BroadcastList {
   public static final String GROUP_BROADCAST_LIST_NAMESPACE = "GBL";
   public static final String BROADCAST_LIST_NAMESPACE = "BL";
   public static final String BROADCAST_LIST_DISTRIBUTED_LOCK_NAMESPACE = "BLDL";

   public static Set<String> newBroadcastList() {
      return new HashSet();
   }

   public static Set<String> newBroadcastList(Collection<String> broadcastList) {
      return broadcastList == null ? null : new HashSet(broadcastList);
   }

   public static String[] asArray(Set<String> broadcastList) {
      return broadcastList == null ? new String[0] : (String[])broadcastList.toArray(new String[broadcastList.size()]);
   }

   public static String getKey(String username) {
      return MemCachedUtils.getCacheKeyInNamespace("BL", username);
   }

   public static String getGroupKey(int groupId) {
      return MemCachedUtils.getCacheKeyInNamespace("GBL", Integer.toString(groupId));
   }

   public static Set<String> getBroadcastList(MemCachedClient instance, String username) {
      if (instance == null) {
         instance = MemCachedClientWrapper.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
      }

      return (Set)instance.get(getKey(username));
   }

   public static Set<String> getGroupBroadcastList(MemCachedClient instance, int groupId) {
      return (Set)instance.get(getGroupKey(groupId));
   }

   public static boolean setBroadcastList(MemCachedClient instance, String username, Set<String> bcl) {
      if (instance == null) {
         instance = MemCachedClientWrapper.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
      }

      Calendar now = Calendar.getInstance();
      now.add(6, 5);
      return instance.set(getKey(username), bcl, now.getTime());
   }

   public static boolean setGroupBroadcastList(MemCachedClient instance, int groupId, Set<String> bcl) {
      Calendar now = Calendar.getInstance();
      now.add(6, 5);
      return instance.set(getGroupKey(groupId), bcl, now.getTime());
   }

   public static boolean deleteBroadcastList(MemCachedClient instance, String username) {
      return instance.delete(getKey(username));
   }

   public static boolean deleteGroupBroadcastList(MemCachedClient instance, int groupId) {
      return instance.delete(getGroupKey(groupId));
   }

   public static SortedSet<String> sort(Set<String> broadcastList) {
      SortedSet<String> sortedSet = new TreeSet();
      sortedSet.addAll(broadcastList);
      return sortedSet;
   }
}
