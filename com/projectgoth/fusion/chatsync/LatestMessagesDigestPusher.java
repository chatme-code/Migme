package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.packet.chatsync.FusionPktLatestMessagesDigest;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SessionPrx;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

public class LatestMessagesDigestPusher implements ChatSyncEntity {
   private static final LogFilter log;
   private final int userID;
   private final SessionPrx session;
   private final short txnID;
   private final String parentUsername;

   public LatestMessagesDigestPusher(int userID, SessionPrx session, short txnID, String parentUsername) throws FusionException {
      this.userID = userID;
      this.session = session;
      this.txnID = txnID;
      this.parentUsername = parentUsername;
   }

   private void renewExpiry(ChatSyncEntity entity, SystemPropertyEntities.ChatSyncSettings toggle, ChatSyncStore store) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)toggle)) {
         int hrs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHAT_CONTENT_EXPIRY_HOURS);
         store.expire(entity, hrs * 3600);
      }

   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      if (!(Boolean)SystemPropertyEntities.Temp.Cache.se464LmdChangesEnabled.getValue()) {
         this.retrieve_beforeSE464(stores);
      } else {
         CurrentChatList ccl = new CurrentChatList(this.userID);
         ccl.retrieve(stores);
         ArrayList<FusionPktLatestMessagesDigest.LatestMessage> results = new ArrayList();
         String[] chatIDs = ccl.getChatIDs();
         ChatSyncStore[] arr$ = stores;
         int len$ = stores.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ChatSyncStore store = arr$[i$];
            String[] arr$ = chatIDs;
            int len$ = chatIDs.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               String cid = arr$[i$];
               ChatDefinition chatKey = new ChatDefinition(cid);
               store.setMaster();
               this.renewExpiry(chatKey, SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_READ_ENABLED, store);
               store.setSlave();
               byte[] lastMsgData = null;
               ChatMessage msgsKey = new ChatMessage(chatKey);
               Set<byte[]> msgs = store.zrangeBinary(msgsKey, -1, -1);
               if (msgs != null && msgs.size() != 0) {
                  ArrayList<byte[]> msgs2 = new ArrayList(msgs);
                  lastMsgData = (byte[])msgs2.get(0);
               }

               if (lastMsgData != null) {
                  this.processLastMessage(chatKey, lastMsgData, results);
               }
            }
         }

         if (log.isDebugEnabled()) {
            log.debug("LatestMessagesDigestPusher: pushing details on " + results.size() + " latest messages to userID=" + this.userID);
         }

         FusionPktLatestMessagesDigest.LatestMessage[] arrResults = (FusionPktLatestMessagesDigest.LatestMessage[])results.toArray(new FusionPktLatestMessagesDigest.LatestMessage[0]);
         FusionPktLatestMessagesDigest digest = new FusionPktLatestMessagesDigest(this.txnID, arrResults);
         this.session.putSerializedPacket(digest.toSerializedBytes());
      }
   }

   private void processLastMessage(ChatDefinition chatKey, byte[] lastMsgData, List<FusionPktLatestMessagesDigest.LatestMessage> results) {
      try {
         ChatMessage unpacked = new ChatMessage(chatKey, lastMsgData);
         String externalChatID = chatKey.internalToExternalChatID(this.parentUsername);
         FusionPktLatestMessagesDigest.LatestMessage latest = new FusionPktLatestMessagesDigest.LatestMessage(externalChatID, unpacked.getGUID(), unpacked.getTimestamp(), chatKey.getChatType(), unpacked.getMessageText());
         results.add(latest);
         if (log.isDebugEnabled()) {
            log.debug("LatestMessagesDigestPusher: found latest message=" + latest.getGUID() + " for chatID=" + chatKey.getChatID());
         }
      } catch (Exception var7) {
         log.error("Exception processing latest message for chatID=" + chatKey.getChatID() + " for user=" + this.parentUsername + " e=" + var7, var7);
      }

   }

   private void retrieve_beforeSE464(ChatSyncStore[] stores) throws FusionException {
      CurrentChatList ccl = new CurrentChatList(this.userID);
      ccl.retrieve(stores);
      ArrayList<FusionPktLatestMessagesDigest.LatestMessage> results = new ArrayList();
      String[] chatIDs = ccl.getChatIDs();
      ChatSyncStore[] arr$ = stores;
      int len$ = stores.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatSyncStore store = arr$[i$];
         String[] arr$ = chatIDs;
         int len$ = chatIDs.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String cid = arr$[i$];
            ChatDefinition chatKey = new ChatDefinition(cid);
            store.setMaster();
            this.renewExpiry(chatKey, SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_READ_ENABLED, store);
            store.setSlave();
            byte[] lastMsgData = null;
            ChatMessage msgsKey = new ChatMessage(chatKey);
            Set<byte[]> msgs = store.zrangeBinary(msgsKey, -1, -1);
            if (msgs != null && msgs.size() != 0) {
               ArrayList<byte[]> msgs2 = new ArrayList(msgs);
               lastMsgData = (byte[])msgs2.get(0);
            }

            if (lastMsgData != null) {
               ChatMessage unpacked = new ChatMessage(chatKey, lastMsgData);
               String externalChatID = chatKey.internalToExternalChatID(this.parentUsername);
               FusionPktLatestMessagesDigest.LatestMessage latest = new FusionPktLatestMessagesDigest.LatestMessage(externalChatID, unpacked.getGUID(), unpacked.getTimestamp(), chatKey.getChatType(), unpacked.getMessageText());
               results.add(latest);
               if (log.isDebugEnabled()) {
                  log.debug("LatestMessagesDigestPusher: found latest message=" + latest.getGUID() + " for chatID=" + cid);
               }
            }
         }
      }

      if (log.isDebugEnabled()) {
         log.debug("LatestMessagesDigestPusher: pushing details on " + results.size() + " latest messages to userID=" + this.userID);
      }

      FusionPktLatestMessagesDigest.LatestMessage[] arrResults = (FusionPktLatestMessagesDigest.LatestMessage[])results.toArray(new FusionPktLatestMessagesDigest.LatestMessage[0]);
      FusionPktLatestMessagesDigest digest = new FusionPktLatestMessagesDigest(this.txnID, arrResults);
      this.session.putSerializedPacket(digest.toSerializedBytes());
   }

   public String getKey() {
      return null;
   }

   public String getValue() {
      return null;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return null;
   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public int processPipelineStoreResults(List<Object> pipelineResults, int startIndex) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public boolean canRetryReads() throws FusionException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRY_LATEST_MESSAGES_DIGEST_ENABLED);
   }

   public boolean canRetryWrites() throws FusionException {
      return false;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(LatestMessagesDigestPusher.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
