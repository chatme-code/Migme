package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

public class ParticipantList implements ChatSyncEntity {
   private static final LogFilter log;
   protected int writeOpCount;
   protected ChatDefinition chatID;
   protected String[] participants = new String[0];

   public ParticipantList(ChatDefinition chatID) {
      this.chatID = chatID;
   }

   public ParticipantList(ChatDefinition chatID, String[] participants) {
      this.chatID = chatID;
      this.participants = participants;
   }

   public String getKey() {
      return this.chatID.getChatID();
   }

   public String getValue() {
      return null;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.CONVERSATION;
   }

   protected void renewExpiry(SystemPropertyEntities.ChatSyncSettings toggle, ChatSyncStore store) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)toggle)) {
         int hrs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHAT_DEFINITION_EXPIRY_HOURS);
         store.expire(this, hrs * 3600);
         ++this.writeOpCount;
      }

   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      ChatSyncStore store = stores[0];
      store.setMaster();
      this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_READ_ENABLED, store);
      ArrayList<ChatSyncPipelineOp> ops = new ArrayList();
      ops.add(new ChatSyncPipelineOp(this, ChatSyncPipelineOp.OpType.READ));
      store.setSlave();
      store.pipelined((List)ops);
   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
      pipelineStore.zrange(this, 0, -1);
   }

   public int loadPipeline(List<Object> results, int startIndex) throws FusionException {
      ArrayList<String> ps = new ArrayList();
      if (startIndex < results.size()) {
         List<Object> usernamesBytes = new ArrayList((Set)results.get(startIndex));
         Iterator i$ = usernamesBytes.iterator();

         while(i$.hasNext()) {
            Object username = i$.next();
            ps.add((String)username);
         }
      }

      this.participants = (String[])ps.toArray(new String[ps.size()]);
      return startIndex + 1;
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
      this.writeOpCount = 0;
      pipelineStore.zremrangeByRank(this, 0, -1);
      ++this.writeOpCount;
      if (this.participants != null) {
         String[] arr$ = this.participants;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String username = arr$[i$];
            pipelineStore.zadd(this, (double)System.currentTimeMillis(), (String)username);
            ++this.writeOpCount;
         }
      }

      this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_WRITE_ENABLED, pipelineStore);
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return startIndex + this.writeOpCount;
   }

   public boolean canRetryReads() throws FusionException {
      return false;
   }

   public boolean canRetryWrites() throws FusionException {
      return false;
   }

   public String[] getUsernames() {
      return this.participants;
   }

   public int getWriteOpCount() {
      return this.writeOpCount;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ParticipantList.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
