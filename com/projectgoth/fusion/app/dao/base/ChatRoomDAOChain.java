package com.projectgoth.fusion.app.dao.base;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.data.ChatRoomData;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatRoomDAOChain implements DAOChain {
   private ChatRoomDAOChain nextRead;
   private ChatRoomDAOChain nextWrite;

   public void setNextRead(DAOChain a) {
      this.nextRead = (ChatRoomDAOChain)a;
   }

   public void setNextWrite(DAOChain a) {
      this.nextWrite = (ChatRoomDAOChain)a;
   }

   public String[] getGroupChatRooms(int groupId) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getGroupChatRooms(groupId);
      } else {
         throw new DAOException(String.format("Failed to get GroupChatRooms for groupid:%s", groupId));
      }
   }

   public Map<Integer, List<String>> getChatroomNamesPerCategory(boolean activeChatroomsAndCategoriesOnly) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getChatroomNamesPerCategory(activeChatroomsAndCategoriesOnly);
      } else {
         throw new DAOException(String.format("Failed to get ChatroomNamesPerCategory for activeChatroomsAndCategoriesOnly:%s", activeChatroomsAndCategoriesOnly));
      }
   }

   public List<ChatRoomData> getChatRooms(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
      } else {
         throw new DAOException(String.format("Failed to get ChatRooms for countryID:%s, search:%s, language:%s, includeAdultOnly:%s, searchKeywords:%s", countryID, search, language, includeAdultOnly, search));
      }
   }

   public List<ChatRoomData> getFavouriteChatRooms(UserObject user) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getFavouriteChatRooms(user);
      } else {
         throw new DAOException(String.format("Failed to get FavouriteChatRooms for user:%s", user));
      }
   }

   public List<String> getAllRecentChatRooms(UserObject user) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getAllRecentChatRooms(user);
      } else {
         throw new DAOException(String.format("Failed to get AllRecentChatRooms for user:%s", user));
      }
   }

   public List<ChatRoomData> getRecentChatRooms(List<String> allRecentChatRooms) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getRecentChatRooms(allRecentChatRooms);
      } else {
         throw new DAOException(String.format("Failed to get RecentChatRooms for chatrooms:%s", allRecentChatRooms));
      }
   }

   public List<ChatRoomData> getRecentChatRooms(UserObject user) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getRecentChatRooms(user);
      } else {
         throw new DAOException(String.format("Failed to get RecentChatRooms for user:%s", user));
      }
   }

   public ChatRoomData getChatRoom(String chatRoomName) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getChatRoom(chatRoomName);
      } else {
         throw new DAOException(String.format("Failed to get ChatRoom for chat room name:%s", chatRoomName));
      }
   }

   public ChatRoomData getSimpleChatRoomData(String normalizedChatRoomName) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getSimpleChatRoomData(normalizedChatRoomName);
      } else {
         throw new DAOException(String.format("Failed to get SimpleChatRoomData for chat room name:%s", normalizedChatRoomName));
      }
   }

   public Map<String, String> getChatRoomTheme(int themeID) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getChatRoomTheme(themeID);
      } else {
         throw new DAOException(String.format("Failed to get chatroom theme for themeID:%s", themeID));
      }
   }

   public Set<String> getChatRoomModerators(ChatRoomData chatroom) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getChatRoomModerators(chatroom);
      } else {
         throw new DAOException(String.format("Failed to get ChatRoomModerators theme for chatroom:%s", chatroom.id));
      }
   }

   public Set<String> getChatRoomBannedUsers(ChatRoomData chatroom) throws DAOException {
      if (this.nextRead != null) {
         return this.nextRead.getChatRoomBannedUsers(chatroom);
      } else {
         throw new DAOException(String.format("Failed to get ChatRoomBannedUsers theme for chatroom:%s", chatroom.id));
      }
   }
}
