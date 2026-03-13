package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import java.util.List;
import org.apache.log4j.Logger;

public class ChatListVersion implements ChatSyncEntity {
   private static final LogFilter log;
   private int userID;
   private Integer version;
   private int storeOps;

   public ChatListVersion(int userID) {
      this.userID = userID;
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.USER;
   }

   public int getUserID() {
      return this.userID;
   }

   public int getVersion() {
      return this.version;
   }

   public String getKey() {
      return Integer.toString(this.userID);
   }

   public String getValue() {
      return null;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   public void renewExpiry(SystemPropertyEntities.ChatSyncSettings toggle, ChatSyncStore store) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)toggle)) {
         int hrs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHAT_LIST_VERSION_EXPIRY_HOURS);
         store.expire(this, hrs * 3600);
         ++this.storeOps;
      }

   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      stores[0].setMaster();
      this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_READ_ENABLED, stores[0]);
      stores[0].setSlave();
      String value = stores[0].get(this);
      this.version = value == null ? 0 : Integer.parseInt(value);
   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
      pipelineStore.get(this);
   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      String sValue = Redis.getPipelineResult(pipelineResults, startIndex);
      this.version = sValue == null ? 0 : Integer.parseInt(sValue);
      return startIndex + 1;
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
      this.storeOps = 0;
      pipelineStore.incr(this);
      ++this.storeOps;
      this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_WRITE_ENABLED, pipelineStore);
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return startIndex + this.storeOps;
   }

   public boolean canRetryReads() throws FusionException {
      return false;
   }

   public boolean canRetryWrites() throws FusionException {
      return false;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ChatListVersion.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
