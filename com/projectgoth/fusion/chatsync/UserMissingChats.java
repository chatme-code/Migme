package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

public class UserMissingChats extends UserChatLists {
   private static final LogFilter log;
   protected int chatListVersion;
   protected Integer limit;
   protected Byte chatType;
   protected ConnectionPrx connection;
   protected ArrayList<ChatDefinition> missingChatDefs = new ArrayList();

   public UserMissingChats(int userID, int chatListVersion, Integer limit, Byte chatType, ConnectionPrx connection) {
      super(userID);
      this.chatListVersion = chatListVersion;
      this.limit = limit;
      this.chatType = chatType;
      this.connection = connection;
      if (log.isDebugEnabled()) {
         log.debug("UserMissingChats.UserMissingChats: chatType=" + chatType + " for userID=" + userID);
      }

   }

   public ChatDefinition[] getMissingChats() {
      return (ChatDefinition[])this.missingChatDefs.toArray(new ChatDefinition[0]);
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      super.retrieve(stores);
      if (log.isDebugEnabled()) {
         log.debug("UserMissingChats.retrieve: current chat list for userID=" + this.userID + " with chat ids");
         String[] chatIDs = this.currentChatList.getChatIDs();
         String[] arr$ = chatIDs;
         int len$ = chatIDs.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String id = arr$[i$];
            log.debug("chatID=" + id + " for userID=" + this.userID);
         }

         log.debug("");
      }

      if (this.chatListVersion != this.currentChatList.getVersion()) {
         if (log.isDebugEnabled()) {
            log.debug("UserMissingChats.retrieve: userID=" + this.userID + " has chat list ver=" + this.chatListVersion);
         }

         this.findMissingChats(this.oldChatLists, this.currentChatList, this.chatListVersion, this.chatType);
         this.markPassivatedChatrooms();
      }

   }

   private void findMissingChats(OldChatLists oldChatLists, CurrentChatList newestChatList, int clientsChatListVersion, Byte chatType) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("findMissingChats-- clientsChatListVersion=" + clientsChatListVersion + " newestChatList version=" + newestChatList.getVersion() + " oldChatLists=" + oldChatLists + (oldChatLists != null ? " oldChatLists.size=" + oldChatLists.size() : "") + " for userID=" + this.userID);
      }

      if (clientsChatListVersion != newestChatList.getVersion()) {
         OldChatList clientsList = null;

         for(int i = 0; i < oldChatLists.size(); ++i) {
            OldChatList list = oldChatLists.get(i);
            if (log.isDebugEnabled()) {
               log.debug("findMissingChats--Checking OldChatList with version=" + list.getVersion() + "for userID=" + this.userID);
            }

            if (list.getVersion() == clientsChatListVersion) {
               clientsList = list;
               break;
            }
         }

         if (log.isDebugEnabled()) {
            log.debug("findMissingChats-- found matching OldChatList=" + clientsList + "for userID=" + this.userID);
         }

         String[] missingIDs;
         if (clientsList != null) {
            if (log.isDebugEnabled()) {
               log.debug("findMissingChats-- clientsList version=" + clientsList.getVersion() + " clientsList.length=" + clientsList.getChatIDs().length + " for userID=" + this.userID);
            }

            missingIDs = clientsList.findMissingChats(newestChatList.getChatIDs());
         } else {
            missingIDs = newestChatList.getChatIDs();
         }

         if (log.isDebugEnabled()) {
            log.debug("findMissingChats-- no of chats to push=" + missingIDs.length + " for userID=" + this.userID);
         }

         RedisChatSyncStore store = new RedisChatSyncStore(ChatSyncStore.StorePrimacy.MASTER);
         String[] arr$ = missingIDs;
         int len$ = missingIDs.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String id = arr$[i$];
            if (log.isDebugEnabled()) {
               log.debug("findMissingChats-- loading chat def for chat id=" + id + " for userID=" + this.userID);
            }

            try {
               ChatDefinition chatDef = new ChatDefinition(id, store);
               if (log.isDebugEnabled()) {
                  log.debug("findMissingChats-- retrieved chatdef for chatID=" + id + " for userID=" + this.userID);
               }

               if (chatType == null || chatDef.getChatType() == chatType) {
                  this.missingChatDefs.add(chatDef);
               }
            } catch (ChatDefinition.ChatDefinitionNotFoundException var14) {
               log.info("Chat id=" + id + " not found in redis (probably expired), " + " removing from current chat list of user id=" + this.userID);
               ChatDefinition chatKey = new ChatDefinition(id);
               newestChatList.update(store, (ChatDefinition)null, chatKey);
            }
         }
      }

   }

   private void markPassivatedChatrooms() {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
         if (this.connection != null) {
            UserPrx user = this.connection.getUserObject();
            String[] currentChatrooms = user.getCurrentChatrooms();
            Set<String> set = new HashSet(Arrays.asList(currentChatrooms));
            Iterator i$ = this.missingChatDefs.iterator();

            while(i$.hasNext()) {
               ChatDefinition def = (ChatDefinition)i$.next();
               if (def.getChatType() == (byte)MessageDestinationData.TypeEnum.CHAT_ROOM.value()) {
                  boolean inChatList = set.contains(def.getChatName());
                  def.setIsPassivatedChat(!inChatList);
               }
            }

         }
      }
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(UserMissingChats.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
