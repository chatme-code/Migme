package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class OldChatLists implements ChatSyncEntity {
   private static final LogFilter log;
   private int userID;
   private ArrayList<OldChatList> oldChatLists = new ArrayList();

   public OldChatLists(int userID) {
      this.userID = userID;
   }

   public String getKey() {
      return Integer.toString(this.userID);
   }

   public String getValue() {
      return null;
   }

   public int getUserID() {
      return this.userID;
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.USER;
   }

   public void renewExpiry(SystemPropertyEntities.ChatSyncSettings toggle, ChatSyncStore store) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)toggle)) {
         int hrs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.OLD_CHAT_LISTS_EXPIRY_HOURS);
         store.expire(this, hrs * 3600);
      }

   }

   public void store(ChatSyncStore[] stores) throws FusionException {
   }

   public static void truncateIfNeeded(int userID, ChatSyncStore store) throws FusionException {
      int maxLength = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.OLD_CHAT_LISTS_MAX_LENGTH);
      if (maxLength == 0) {
         log.debug("Old chat lists max length truncation disabled");
      } else {
         OldChatLists oclKey = new OldChatLists(userID);
         store.setSlave();
         Integer size = store.llen(oclKey);
         if (size != null) {
            store.setMaster();
            if (size > maxLength) {
               if (log.isDebugEnabled()) {
                  log.debug("OldChatList.truncateIfNeeded: for userID=" + userID + " has length=" + size + " max length=" + maxLength + "...truncating");
               }

               for(int i = 0; i < size - maxLength; ++i) {
                  store.lpop(oclKey);
               }
            }

         }
      }
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
      pipelineStore.lrangeBinary(this, 0, -1);
   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      ArrayList<byte[]> ocls = (ArrayList)pipelineResults.get(startIndex);
      this.oldChatLists = new ArrayList();
      Iterator i$ = ocls.iterator();

      while(i$.hasNext()) {
         byte[] oclData = (byte[])i$.next();
         OldChatList addMe = new OldChatList(this.userID, oclData);
         this.oldChatLists.add(addMe);
      }

      return startIndex + 1;
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return 0;
   }

   public int size() {
      return this.oldChatLists.size();
   }

   public OldChatList get(int i) {
      return (OldChatList)this.oldChatLists.get(i);
   }

   public boolean canRetryReads() throws FusionException {
      return false;
   }

   public boolean canRetryWrites() throws FusionException {
      return false;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(OldChatLists.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
