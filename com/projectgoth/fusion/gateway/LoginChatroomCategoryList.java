package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatroomCategoryData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class LoginChatroomCategoryList {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(LoginChatroomCategoryList.class));
   private static Map<String, List<ChatroomCategoryData>> chatroomCategories = new ConcurrentHashMap();
   private static Semaphore semaphore = new Semaphore(1);
   private static long lastUpdated;

   private static long getCacheTimeInMillis() {
      return SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.CATEGORIES_LIST_CACHE_TIME_IN_MILLIS);
   }

   private static void loadCategories() {
      if (lastUpdated == 0L) {
         semaphore.acquireUninterruptibly();
      } else if (!semaphore.tryAcquire()) {
         return;
      }

      try {
         int maxNewbieLevel = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MAX_NEWBIE_MIG33_LEVEL);
         int[] alwaysLoadedCategoryIds = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.CATEGORIES_ALWAYS_INCLUDED_IN_LOGIN);
         List<Integer> alwaysLoadedCategoryIdsInt = new ArrayList();
         int[] arr$ = alwaysLoadedCategoryIds;
         int len$ = alwaysLoadedCategoryIds.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            int categoryId = arr$[i$];
            alwaysLoadedCategoryIdsInt.add(categoryId);
         }

         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         List<ChatroomCategoryData> categories = messageEJB.getLoginChatroomCategories();
         List<ChatroomCategoryData> newbieChatrooms = new ArrayList();
         List<ChatroomCategoryData> nonNewbieChatrooms = new ArrayList();
         Iterator i$ = categories.iterator();

         while(true) {
            while(i$.hasNext()) {
               ChatroomCategoryData category = (ChatroomCategoryData)i$.next();
               if (alwaysLoadedCategoryIdsInt.contains(category.id)) {
                  newbieChatrooms.add(category);
                  nonNewbieChatrooms.add(category);
               } else if (category.maxLevel != 0 && category.maxLevel <= maxNewbieLevel) {
                  newbieChatrooms.add(category);
               } else {
                  nonNewbieChatrooms.add(category);
               }
            }

            chatroomCategories.put("newbie", newbieChatrooms);
            chatroomCategories.put("chatrooms", nonNewbieChatrooms);
            lastUpdated = System.currentTimeMillis();
            break;
         }
      } catch (Exception var13) {
         log.warn("Unable to load chatroom categories", var13);
      } finally {
         semaphore.release();
      }

   }

   public static List<ChatroomCategoryData> get(String username, int userID) throws CreateException, RemoteException {
      boolean nueEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.NUE_NEWBIE_CATEGORIES_ENABLED);
      int maxNewbieLevel = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MAX_NEWBIE_MIG33_LEVEL);
      if (nueEnabled) {
         int migLevel = MemCacheOrEJB.getUserReputationLevel(username, userID);
         if (migLevel <= maxNewbieLevel) {
            return getNewbieChatroomCategories();
         }
      }

      return getNonNewbieChatroomCategories();
   }

   private static List<ChatroomCategoryData> getNewbieChatroomCategories() {
      checkAndLoadCategories();
      return (List)chatroomCategories.get("newbie");
   }

   private static List<ChatroomCategoryData> getNonNewbieChatroomCategories() {
      checkAndLoadCategories();
      return (List)chatroomCategories.get("chatrooms");
   }

   public static ChatroomCategoryData getChatroomCategory(Integer categoryId) throws Exception {
      checkAndLoadCategories();
      Iterator i$ = ((List)chatroomCategories.get("newbie")).iterator();

      ChatroomCategoryData category;
      do {
         if (!i$.hasNext()) {
            i$ = ((List)chatroomCategories.get("chatrooms")).iterator();

            do {
               if (!i$.hasNext()) {
                  log.error("Unable to get chatroom category [" + categoryId + "]");
                  throw new Exception("Unable to get chatroom category.");
               }

               category = (ChatroomCategoryData)i$.next();
            } while(categoryId != category.id);

            return category;
         }

         category = (ChatroomCategoryData)i$.next();
      } while(categoryId != category.id);

      return category;
   }

   private static void checkAndLoadCategories() {
      if (System.currentTimeMillis() - lastUpdated > getCacheTimeInMillis()) {
         loadCategories();
      }

   }

   static {
      chatroomCategories.put("newbie", new ArrayList());
      chatroomCategories.put("chatrooms", new ArrayList());
   }
}
