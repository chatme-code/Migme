package com.projectgoth.fusion.app.dao.db.memcache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.ChatRoomDAOChain;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.cache.ChatRoomSearch;
import com.projectgoth.fusion.cache.RecentChatRoomList;
import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomData;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class MemcacheChatRoomDAOChain extends ChatRoomDAOChain {
   private static final Logger log = Logger.getLogger(MemcacheChatRoomDAOChain.class);

   public List<ChatRoomData> getChatRooms(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) throws DAOException {
      boolean performingSearch = StringUtils.hasLength(search);
      if (performingSearch) {
         MemCachedClient client = MemCachedClientWrapper.getMemCachedClient(MemCachedUtils.Instance.chatRoomSearch);
         List<ChatRoomData> cachedChatRooms = ChatRoomSearch.getChatRoomSearch(client, countryID, search, language, includeAdultOnly, searchKeywords);
         if (cachedChatRooms == null) {
            cachedChatRooms = super.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
            ChatRoomSearch.setChatRoomSearchList(client, countryID, search, language, includeAdultOnly, searchKeywords, cachedChatRooms);
         }

         return cachedChatRooms;
      } else {
         return super.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
      }
   }

   public List<String> getAllRecentChatRooms(UserObject user) throws DAOException {
      MemCachedClient client = MemCachedClientWrapper.getMemCachedClient(MemCachedUtils.Instance.recentChatRooms);
      List<String> allRecentChatRooms = RecentChatRoomList.getRecentChatRoomList(client, user.getUsername());
      if (allRecentChatRooms == null) {
         allRecentChatRooms = super.getAllRecentChatRooms(user);
         RecentChatRoomList.setRecentChatRoomList(client, user.getUsername(), allRecentChatRooms);
      }

      return allRecentChatRooms;
   }

   public ChatRoomData getSimpleChatRoomData(String normalizedChatRoomName) throws DAOException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.NEGATIVE_CACHE_ENABLED) && MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_NEGATIVE_CACHE, normalizedChatRoomName) != null) {
         if (log.isDebugEnabled()) {
            log.debug("Attempt to get invalid chatroom : " + normalizedChatRoomName + ",found in negative cache");
         }

         return null;
      } else {
         ChatRoomData room = (ChatRoomData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM, normalizedChatRoomName);
         if (room == null) {
            room = super.getSimpleChatRoomData(normalizedChatRoomName);
            if (room == null) {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.NEGATIVE_CACHE_ENABLED)) {
                  MemCachedClientWrapper.add(MemCachedKeySpaces.CommonKeySpace.CHATROOM_NEGATIVE_CACHE, normalizedChatRoomName, 1);
               }
            } else {
               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM, normalizedChatRoomName, room);
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
                  MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BY_ID, Integer.toString(room.id), room);
               }
            }
         }

         return room;
      }
   }

   public Map<String, String> getChatRoomTheme(int themeID) throws DAOException {
      Map<String, String> theme = (Map)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_THEME, String.valueOf(themeID));
      if (theme == null) {
         theme = super.getChatRoomTheme(themeID);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM_THEME, String.valueOf(themeID), theme);
      }

      return theme;
   }

   public Set<String> getChatRoomModerators(ChatRoomData chatroom) throws DAOException {
      String normalizedChatRoomName = ChatRoomUtils.normalizeChatRoomName(chatroom.name);
      Set<String> moderators = (HashSet)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_MODERATORS, normalizedChatRoomName);
      if (moderators == null) {
         moderators = super.getChatRoomModerators(chatroom);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM_MODERATORS, normalizedChatRoomName, moderators);
      }

      return (Set)moderators;
   }

   public Set<String> getChatRoomBannedUsers(ChatRoomData chatroom) throws DAOException {
      String normalizedChatRoomName = ChatRoomUtils.normalizeChatRoomName(chatroom.name);
      Set<String> bannedUsers = (HashSet)MemCachedClientWrapper.getPaged(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BANNED_USERS, normalizedChatRoomName);
      if (bannedUsers == null) {
         bannedUsers = super.getChatRoomBannedUsers(chatroom);
         Integer pageSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.BANNED_USERS_PAGE_SIZE);
         MemCachedClientWrapper.setPaged(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BANNED_USERS, normalizedChatRoomName, bannedUsers, pageSize);
      }

      return (Set)bannedUsers;
   }
}
