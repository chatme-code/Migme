package com.projectgoth.fusion.cache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.data.ChatRoomData;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

public class ChatRoomSearch {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatRoomSearch.class));
   public static final String CHATROOM_SEARCH_NAMESPACE = "CS";
   public static final int MINUTES_TO_CACHE_FOR = 10;

   public static List<String> newChatRoomSearch() {
      return new LinkedList();
   }

   public static String getKey(Integer countryID, String search, String language, Boolean includeAdultOnly, Boolean searchKeywords) {
      return MemCachedUtils.getCacheKeyInNamespace("CS", countryID.toString() + ',' + search + ',' + language + ',' + includeAdultOnly.toString() + ',' + searchKeywords.toString());
   }

   public static List<ChatRoomData> getChatRoomSearch(MemCachedClient instance, int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) {
      if (instance == null) {
         log.warn("ChatRoomSearch.getChatRoomSearch(): ChatRoomSearch memcache instance is null");
         return null;
      } else {
         return (List)instance.get(getKey(countryID, search, language, includeAdultOnly, searchKeywords));
      }
   }

   public static boolean setChatRoomSearchList(MemCachedClient instance, Integer countryID, String search, String language, Boolean includeAdultOnly, Boolean searchKeywords, List<ChatRoomData> searchResults) {
      if (instance == null) {
         log.warn("ChatRoomSearch.setChatRoomSearch(): ChatRoomSearch memcache instance is null");
         return false;
      } else {
         Calendar expireTime = Calendar.getInstance();
         expireTime.add(12, 10);
         return instance.set(getKey(countryID, search, language, includeAdultOnly, searchKeywords), searchResults, expireTime.getTime());
      }
   }
}
