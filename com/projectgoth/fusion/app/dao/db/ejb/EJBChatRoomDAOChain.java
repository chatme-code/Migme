package com.projectgoth.fusion.app.dao.db.ejb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.ChatRoomDAOChain;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class EJBChatRoomDAOChain extends ChatRoomDAOChain {
   private static final Logger log = Logger.getLogger(EJBChatRoomDAOChain.class);

   public String[] getGroupChatRooms(int groupId) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETGROUPCHATROOMS)) {
         return super.getGroupChatRooms(groupId);
      } else {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            return messageEJB.getGroupChatRooms(groupId);
         } catch (Exception var3) {
            log.warn(String.format("Failed to get GroupChatRooms for group:%s", groupId), var3);
            return super.getGroupChatRooms(groupId);
         }
      }
   }

   public Map<Integer, List<String>> getChatroomNamesPerCategory(boolean activeChatroomsAndCategoriesOnly) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETCHATROOMNAMESPERCATEGORY)) {
         return super.getChatroomNamesPerCategory(activeChatroomsAndCategoriesOnly);
      } else {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            return messageEJB.getChatroomNamesPerCategory(activeChatroomsAndCategoriesOnly);
         } catch (Exception var3) {
            log.warn(String.format("Failed to get ChatroomNamesPerCategory for activeChatroomsAndCategoriesOnly:%s", activeChatroomsAndCategoriesOnly), var3);
            return super.getChatroomNamesPerCategory(activeChatroomsAndCategoriesOnly);
         }
      }
   }

   public List<ChatRoomData> getChatRooms(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETCHATROOMS)) {
         return super.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
      } else {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            return messageEJB.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
         } catch (Exception var7) {
            log.warn(String.format("Failed to get ChatRooms for countryID:%s, search:%s, language:%s, includeAdultOnly:%s, searchKeywords:%s", countryID, search, language, includeAdultOnly, search), var7);
            return super.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
         }
      }
   }

   public List<ChatRoomData> getFavouriteChatRooms(UserObject user) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETFAVCHATROOMS)) {
         return super.getFavouriteChatRooms(user);
      } else {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            return messageEJB.getFavouriteChatRooms(user.getUsername());
         } catch (Exception var3) {
            log.warn(String.format("Failed to ge tFavouriteChatRooms for user:%s", user), var3);
            return super.getFavouriteChatRooms(user);
         }
      }
   }

   public List<ChatRoomData> getRecentChatRooms(UserObject user) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETRECENTCHATROOMS)) {
         return super.getRecentChatRooms(user);
      } else {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            return messageEJB.getRecentChatRooms(user.getUsername());
         } catch (Exception var3) {
            log.warn(String.format("Failed to get RecentChatRooms for user:%s", user), var3);
            return super.getRecentChatRooms(user);
         }
      }
   }

   public ChatRoomData getSimpleChatRoomData(String normalizedChatRoomName) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETSIMPLECHATROOM)) {
         return super.getSimpleChatRoomData(normalizedChatRoomName);
      } else {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            return messageEJB.getSimpleChatRoomData((String)normalizedChatRoomName, (Connection)null);
         } catch (Exception var3) {
            log.warn(String.format("Failed to get SimpleChatRoomData for chatroom:%s", normalizedChatRoomName), var3);
            return super.getSimpleChatRoomData(normalizedChatRoomName);
         }
      }
   }

   public ChatRoomData getChatRoom(String chatRoomName) throws DAOException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_EJB_NODE_GETCHATROOM)) {
         return super.getChatRoom(chatRoomName);
      } else {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            return messageEJB.getChatRoom(chatRoomName);
         } catch (Exception var3) {
            log.warn(String.format("Failed to get ChatRoomData for chatroom:%s", chatRoomName), var3);
            return super.getChatRoom(chatRoomName);
         }
      }
   }
}
