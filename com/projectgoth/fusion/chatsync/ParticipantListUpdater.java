package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class ParticipantListUpdater extends ParticipantList {
   private static final LogFilter log;
   private String addUsername;
   private String removeUsername;

   public ParticipantListUpdater(ChatDefinition chatID) {
      super(chatID);
   }

   public ParticipantListUpdater(ChatDefinition chatID, String[] participants) {
      super(chatID, participants);
   }

   public ParticipantListUpdater(ChatDefinition chatID, String pAddUsername, String pRemoveUsername) {
      super(chatID);
      this.addUsername = pAddUsername;
      this.removeUsername = pRemoveUsername;
   }

   public void update(ChatSyncStore store, String pAddUsername, String pRemoveUsername) throws FusionException {
      this.addUsername = pAddUsername;
      this.removeUsername = pRemoveUsername;
      ArrayList<ChatSyncPipelineOp> writeOps = new ArrayList();
      writeOps.add(new ChatSyncPipelineOp(this, ChatSyncPipelineOp.OpType.WRITE));
      store.setMaster();
      store.pipelined((List)writeOps);
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
      this.writeOpCount = 0;
      if (this.addUsername != null) {
         pipelineStore.zadd(this, (double)System.currentTimeMillis(), (String)this.addUsername);
         ++this.writeOpCount;
      }

      if (this.removeUsername != null) {
         pipelineStore.zrem(this, this.removeUsername);
         ++this.writeOpCount;
      }

      this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_WRITE_ENABLED, pipelineStore);
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return startIndex + this.writeOpCount;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ParticipantListUpdater.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
