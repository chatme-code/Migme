package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.ChatListIce;
import com.projectgoth.fusion.slice.FusionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public abstract class ChatList implements ChatSyncEntity {
   private static final LogFilter log;
   protected int userID;
   protected int chatListVersion;
   protected List<String> chatIDs;

   public ChatList(ChatListIce iceList) {
      this.userID = iceList.userID;
      this.chatListVersion = iceList.chatListVersion;
      this.chatIDs = Arrays.asList(iceList.chatIDs);
   }

   public ChatList(int userID) {
      this.userID = userID;
      this.chatListVersion = 0;
      this.chatIDs = new ArrayList();
   }

   public ChatList(int userID, int chatListVersion, String[] chatIDs) {
      this.userID = userID;
      this.chatListVersion = chatListVersion;
      this.chatIDs = new ArrayList(Arrays.asList(chatIDs));
   }

   public int getUserID() {
      return this.userID;
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.USER;
   }

   public String getKey() {
      return Integer.toString(this.userID);
   }

   public int getVersion() {
      return this.chatListVersion;
   }

   public String[] getChatIDs() {
      String[] result = new String[this.chatIDs.size()];
      return (String[])this.chatIDs.toArray(result);
   }

   public void setVersion(int version) {
      this.chatListVersion = version;
   }

   public ChatListIce toIceObject() {
      ChatListIce ice = new ChatListIce();
      ice.userID = this.userID;
      ice.chatListVersion = this.chatListVersion;
      String[] arr = new String[this.chatIDs.size()];
      ice.chatIDs = (String[])this.chatIDs.toArray(arr);
      return ice;
   }

   public ChatListIce groupChatSubsetToIceObject() throws FusionException {
      ChatListIce ice = new ChatListIce();
      ice.userID = this.userID;
      ice.chatListVersion = this.chatListVersion;
      List<String> subset = new ArrayList();
      Iterator i$ = this.chatIDs.iterator();

      while(i$.hasNext()) {
         String id = (String)i$.next();
         ChatDefinition key = new ChatDefinition(id);
         if (key.isGroupChat()) {
            subset.add(id);
         }
      }

      ice.chatIDs = (String[])subset.toArray(new String[subset.size()]);
      return ice;
   }

   public boolean containsChat(ChatDefinition cd) {
      return this.containsChat(cd.getKey());
   }

   public boolean containsChat(String key) {
      Iterator i$ = this.chatIDs.iterator();

      String id;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         id = (String)i$.next();
      } while(!id.equals(key));

      return true;
   }

   public void addChat(ChatDefinition chat) {
      if (chat != null) {
         String key = chat.getKey();
         if (!this.chatIDs.contains(key)) {
            this.chatIDs.add(key);
         }
      }

   }

   public void removeChat(ChatDefinition chat) {
      if (chat != null) {
         String key = chat.getKey();
         this.chatIDs.remove(key);
      }

   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
      throw new FusionException("retrievePipeline not implemented for " + this);
   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      throw new FusionException("loadPipeline not implemented for " + this);
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
      throw new FusionException("storePipeline not implemented for " + this);
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      throw new FusionException("processPipelineStoreResults not implemented for " + this);
   }

   public boolean canRetryReads() throws FusionException {
      return false;
   }

   public boolean canRetryWrites() throws FusionException {
      return false;
   }

   public ChatList clone() {
      return new CurrentChatList(this.toIceObject());
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ChatList.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
