package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import java.util.List;
import org.apache.log4j.Logger;

public class GroupChatCreationHandler implements ChatSyncEntity {
   private static final LogFilter log;
   private ChatDefinition cdGroupChat;
   private String creatorUsername;
   private String privateChatPartnerUsername;
   private GroupChatPrx groupChat;
   private ChatDefinition previousPrivateChatIdIfAny;

   public GroupChatCreationHandler(ChatDefinition cdGroupChat, String creatorUsername, String privateChatPartnerUsername, GroupChatPrx groupChat, ChatDefinition previousPrivateChatIdIfAny) throws FusionException {
      this.cdGroupChat = cdGroupChat;
      this.creatorUsername = creatorUsername;
      this.privateChatPartnerUsername = privateChatPartnerUsername;
      this.groupChat = groupChat;
      this.previousPrivateChatIdIfAny = previousPrivateChatIdIfAny;
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.USER;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
      this.cdGroupChat.store(stores);
      log.debug("GroupChatCreationHandler.store: group chat stored");
      ChatSyncStore store = stores[0];
      int[] participantUserIDs = this.groupChat.getParticipantUserIDs();
      int creatorUserID = this.groupChat.getCreatorUserID();
      Integer privateChatPartnerUserID = this.privateChatPartnerUsername != null ? this.groupChat.getPrivateChatPartnerUserID() : null;
      int[] arr$ = participantUserIDs;
      int len$ = participantUserIDs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         int userID = arr$[i$];
         CurrentChatList ccl = new CurrentChatList(userID);
         if (userID != creatorUserID && (privateChatPartnerUserID == null || userID != privateChatPartnerUserID)) {
            ccl.update(store, this.cdGroupChat, (ChatDefinition)null);
         } else {
            ccl.update(store, this.cdGroupChat, this.previousPrivateChatIdIfAny);
         }
      }

   }

   public String getKey() {
      return null;
   }

   public String getValue() {
      return null;
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      return 0;
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return 0;
   }

   public boolean canRetryReads() throws FusionException {
      return false;
   }

   public boolean canRetryWrites() throws FusionException {
      return true;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(GroupChatCreationHandler.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
