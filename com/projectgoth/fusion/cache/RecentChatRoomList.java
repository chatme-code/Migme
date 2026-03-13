package com.projectgoth.fusion.cache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.StringUtil;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

public class RecentChatRoomList {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RecentChatRoomList.class));
   public static final String RECENT_CHATROOM_LIST_NAMESPACE = "RCL";
   public static final String RECENT_CHATROOM_LIST_DISTRIBUTED_LOCK_NAMESPACE = "RCLDL";
   public static final int MAX_RECENT_CHAT_ROOMS = 15;

   public static List<String> newRecentChatRoomList() {
      return new LinkedList();
   }

   public static String getKey(String username) {
      return MemCachedUtils.getCacheKeyInNamespace("RCL", username);
   }

   public static List<String> getRecentChatRoomList(MemCachedClient instance, String username) {
      return (List)instance.get(getKey(username));
   }

   public static List<String> getRecentChatRoomList(MemCachedClient instance, String[] usernameArray) {
      String[] keyArray = new String[usernameArray.length];

      for(int i = 0; i < usernameArray.length; ++i) {
         keyArray[i] = getKey(usernameArray[i]);
      }

      List<String> recentChatRoomList = new LinkedList();
      Object[] arr$ = instance.getMultiArray(keyArray);
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Object o = arr$[i$];
         if (o != null) {
            recentChatRoomList.addAll((List)o);
         }
      }

      return recentChatRoomList;
   }

   public static boolean setRecentChatRoomList(MemCachedClient instance, String username, List<String> recentChatRoomList) {
      return instance.set(getKey(username), recentChatRoomList);
   }

   public static boolean addRecentChatRoom(MemCachedClient instance, String username, String recentChatRoom) {
      boolean var4;
      try {
         if (!MemCachedUtils.getLock(instance, "RCLDL", username, 15000)) {
            log.error("Failed to get a lock to update user [" + username + "]'s recent chat room list in 15 seconds");
            boolean var9 = false;
            return var9;
         }

         List<String> recentChatRoomList = getRecentChatRoomList(instance, username);
         if (recentChatRoomList == null) {
            recentChatRoomList = newRecentChatRoomList();
         }

         addRecentChatRoomToList((LinkedList)recentChatRoomList, recentChatRoom);
         setRecentChatRoomList(instance, username, recentChatRoomList);
         var4 = true;
      } finally {
         MemCachedUtils.releaseLock(instance, "RCLDL", username);
      }

      return var4;
   }

   private static void addRecentChatRoomToList(LinkedList<String> recentChatRoomList, String recentChatRoom) {
      if (recentChatRoomList.size() > 0 && !recentChatRoomList.remove(recentChatRoom) && recentChatRoomList.size() >= 15) {
         recentChatRoomList.removeLast();
      }

      recentChatRoomList.addFirst(recentChatRoom);
   }

   public static boolean deleteRecentChatRoomList(MemCachedClient instance, String username) {
      return instance.delete(getKey(username));
   }

   public static String asString(List<String> recentChatRoomList) {
      return StringUtil.asString((Collection)recentChatRoomList);
   }

   public static void main(String[] args) {
      LinkedList<String> recents = (LinkedList)newRecentChatRoomList();

      for(int i = 0; i < 16; ++i) {
         addRecentChatRoomToList(recents, i + "");
      }

      System.out.println(StringUtil.asString((Collection)recents));
      System.out.println((String)recents.get(0));
      addRecentChatRoomToList(recents, "10");
      System.out.println(StringUtil.asString((Collection)recents));
   }
}
