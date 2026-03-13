package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class UserChatLists implements ChatSyncEntity {
   private static final LogFilter log;
   protected int userID;
   protected CurrentChatList currentChatList;
   protected OldChatLists oldChatLists;

   public UserChatLists(int userID) {
      if (log.isDebugEnabled()) {
         log.debug("Entering UserChatLists.UserChatLists with userID=" + userID);
      }

      this.userID = userID;
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.USER;
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("Entering UserChatLists.retrieve with userID=" + this.userID);
      }

      ArrayList ops = new ArrayList();

      try {
         this.oldChatLists = new OldChatLists(this.userID);
         this.currentChatList = new CurrentChatList(this.userID);
         ops.add(new ChatSyncPipelineOp(this.oldChatLists, ChatSyncPipelineOp.OpType.READ));
         ops.add(new ChatSyncPipelineOp(this.currentChatList, ChatSyncPipelineOp.OpType.READ));
         ChatSyncStore[] arr$ = stores;
         int len$ = stores.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ChatSyncStore store = arr$[i$];
            store.setSlave();
            ChatSyncPipelineOp[] opsArray = (ChatSyncPipelineOp[])ops.toArray(new ChatSyncPipelineOp[0]);
            store.pipelinedBinary(opsArray);
            store.setMaster();
            this.oldChatLists.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_READ_ENABLED, store);
            this.currentChatList.renewExpiry(store);
            if (log.isDebugEnabled()) {
               log.debug("Found " + this.currentChatList.getChatIDs().length + " current chats and " + this.oldChatLists.size() + " old chat lists " + "for userID=" + this.userID + " in store " + store);
            }
         }
      } catch (FusionException var8) {
         log.error(var8);
      }

   }

   public String getKey() {
      return Integer.toString(this.userID);
   }

   public String getValue() {
      return null;
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
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
      return false;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(UserChatLists.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
