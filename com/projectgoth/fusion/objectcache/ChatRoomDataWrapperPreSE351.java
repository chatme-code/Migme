package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import javax.ejb.CreateException;

class ChatRoomDataWrapperPreSE351 extends ChatRoomDataWrapper {
   private final ChatRoom chatRoom;
   private final ChatRoomData data;
   private GroupData groupData;

   public ChatRoomDataWrapperPreSE351(ChatRoom chatRoom, ChatRoomData chatRoomData, GroupData groupData) {
      super(chatRoom, chatRoomData, groupData);
      this.chatRoom = chatRoom;
      this.data = chatRoomData;
      this.groupData = groupData;
   }

   public ChatRoomData snapshotChatRoomData() {
      synchronized(this.data) {
         return new ChatRoomData(this.data);
      }
   }

   public ChatRoomDataIce getRoomData() {
      synchronized(this.data) {
         this.data.size = this.chatRoom.getNumParticipants();
         return this.data.toIceObject();
      }
   }

   public String getName() {
      return this.data.name;
   }

   public Integer getGroupID() {
      synchronized(this.data) {
         return this.data.groupID;
      }
   }

   public void updateExtraData(ChatRoomDataIce newChatRoomDataWithExtraData) {
      ChatRoomData newData = new ChatRoomData(newChatRoomDataWithExtraData);
      synchronized(this.data) {
         this.data.updateExtraData(newData);
      }
   }

   public void addModerator(String username) {
      synchronized(this.data) {
         this.data.moderators.add(username);
      }
   }

   public void removeModerator(String username) {
      synchronized(this.data) {
         this.data.moderators.remove(username);
      }
   }

   public boolean isModerator(String username) {
      synchronized(this.data) {
         return this.data.isModerator(username);
      }
   }

   public Map<String, String> getTheme() {
      synchronized(this.data) {
         return this.data.theme;
      }
   }

   public boolean isAllowBots() {
      return this.data.allowBots;
   }

   public Integer botID() {
      return this.data.botID;
   }

   public boolean isAllowKicking() {
      return this.data.allowKicking;
   }

   public boolean isStadium() {
      synchronized(this.data) {
         return this.data.isStadium();
      }
   }

   public boolean isUserOwned() {
      synchronized(this.data) {
         return this.data.isUserOwned();
      }
   }

   public String getCreator() {
      synchronized(this.data) {
         return this.data.creator;
      }
   }

   public void setMaximumSize(int maximumSize) {
      synchronized(this.data) {
         this.data.maximumSize = maximumSize;
      }
   }

   public void setAllowKicking(boolean allowKicking) {
      synchronized(this.data) {
         this.data.allowKicking = allowKicking;
      }
   }

   public void setDescription(String description) {
      synchronized(this.data) {
         this.data.description = description;
      }
   }

   public void setAdultOnly(boolean adultOnly) {
      synchronized(this.data) {
         this.data.adultOnly = adultOnly;
      }
   }

   public Integer getPrimaryCountryID() {
      return this.data.primaryCountryID;
   }

   public Integer getSecondaryCountryID() {
      return this.data.secondaryCountryID;
   }

   public int getID() {
      return this.data.id;
   }

   public boolean isOnBannedList(String username) {
      synchronized(this.data) {
         return this.data.isOnBannedList(username);
      }
   }

   public void addBannedUser(String username) {
      synchronized(this.data) {
         this.data.bannedUsers.add(username);
      }
   }

   public void removeBannedUser(String username) {
      synchronized(this.data) {
         this.data.bannedUsers.remove(username);
      }
   }

   public void setGroupID(Integer id) {
   }

   public void clearModerators() {
   }

   public void setCreator(String newOwnerUsername) {
      synchronized(this.data) {
         this.data.creator = newOwnerUsername;
      }
   }

   public boolean updateDBAccessed(int dbUpdateInterval) {
      synchronized(this.data) {
         if (System.currentTimeMillis() - this.data.dateLastAccessed.getTime() <= (long)dbUpdateInterval) {
            return false;
         } else {
            this.data.dateLastAccessed = new Date();
            return true;
         }
      }
   }

   public boolean isLocked() {
      return this.data.isLocked();
   }

   public void unlock() {
      this.data.unlock();
   }

   public String getLocker() {
      return this.data.getLocker();
   }

   public void lock(String locker) {
      this.data.lock(locker);
   }

   public void tryLock(String locker) throws FusionException {
      synchronized(this.data) {
         if (this.data.isLocked()) {
            throw new FusionException("This chat room has already been locked by " + (locker.equals(this.data.getLocker()) ? "you" : "[" + this.data.getLocker() + "]"));
         } else {
            this.data.lock(locker);
         }
      }
   }

   public String tryUnlock() throws FusionException {
      synchronized(this.data) {
         if (!this.data.isLocked()) {
            throw new FusionException("This chat room is not locked. You can not unlock an unlocked room.");
         } else {
            return this.data.unlock();
         }
      }
   }

   public String getAnnouncer() {
      return this.data.getAnnouncer();
   }

   public void verifySpecificBotOnly(String botName) throws FusionException, RemoteException, CreateException {
      if (this.data.botID != null) {
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         BotData botData = messageEJB.getBot(this.data.botID);
         if (botData != null && !botData.getCommandName().equals(botName)) {
            throw new FusionException("Only " + botData.getDisplayName() + " is allowed to be run in this room");
         }
      }

   }

   public void verifyBotsAllowed() throws FusionException {
      if (this.data.allowBots == null || !this.data.allowBots) {
         throw new FusionException("Bots are not supported in this room");
      }
   }

   public int getMaximumSize() {
      return this.data.maximumSize;
   }

   public int getSecondsToBlock() {
      return this.data.blockPeriodByIpInSeconds != null ? this.data.blockPeriodByIpInSeconds : SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.BLOCK_PERIOD_BY_IP_IN_SECONDS);
   }

   public void verifyMigLevel(UserData userData, boolean hasAdminOrModeratorRights) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MULTI_ID_CONTROL_ENABLED) && !hasAdminOrModeratorRights && this.data.minMigLevel != null) {
         boolean var3 = true;

         int migLevel;
         try {
            migLevel = MemCacheOrEJB.getUserReputationLevel(userData.username, userData.userID);
         } catch (Exception var5) {
            throw new FusionException("Unable to check your mig level for entering the " + this.data.name + " room at this time. Please try again later");
         }

         if (migLevel < this.data.minMigLevel) {
            throw new FusionException(String.format("You must have mig level of %d or above to enter the chat room", this.data.minMigLevel));
         }
      }

   }

   public String getRateLimitByIp() {
      return this.data.rateLimitByIp;
   }

   public ChatRoomData getNewChatRoomData() {
      return new ChatRoomData(this.getRoomData());
   }

   public void makeUserOwned() {
      synchronized(this.data) {
         this.data.groupID = null;
         this.data.userOwned = true;
      }
   }

   public boolean replaceMessageDescription(MessageData messageData) {
      if (this.data.description != null && this.data.description.length() > 0) {
         messageData.messageText = this.data.description;
         return true;
      } else {
         return false;
      }
   }

   public String getAnnounceMessage() {
      return this.data.getAnnounceMessage();
   }

   public boolean isAnnouncementOn() {
      return this.data.isAnnouncementOn();
   }

   public void setAnnouncementOff() {
      this.data.setAnnouncementOff();
   }

   public void setAnnouncementOn(String announcer, String announceMessage) {
      this.data.setAnnouncementOn(announcer, announceMessage);
   }

   public GroupData getGroupData() {
      return this.groupData;
   }

   public ChatRoomInfo getInfo() {
      ChatRoomInfo info = new ChatRoomInfo();
      synchronized(this.data) {
         info.chatRoomName = this.data.name;
         info.groupID = this.data.groupID;
         info.groupName = this.groupData.name;
         info.groupOwner = this.groupData.createdBy;
         return info;
      }
   }

   public GroupData convertIntoGroupChatRoom(GroupData tmpGroupData) {
      this.groupData = tmpGroupData;
      this.setGroupID(this.groupData.id);
      this.clearModerators();
      return this.groupData;
   }

   public boolean hasGroupData() {
      return this.groupData != null;
   }

   public GroupData convertIntoUserChatRoom() {
      if (this.groupData == null) {
         return null;
      } else {
         GroupData old = this.groupData;
         this.groupData = null;
         return old;
      }
   }
}
