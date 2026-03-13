package com.projectgoth.fusion.chatsync;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.ChatListIce;
import com.projectgoth.fusion.slice.FusionException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

public class OldChatList extends ChatList implements ChatSyncEntity {
   private static final LogFilter log;

   public OldChatList(ChatListIce iceList) {
      super(iceList);
   }

   public OldChatList(int userID) {
      super(userID);
   }

   public OldChatList(int userID, int chatListVersion, String[] chatIDs) {
      super(userID, chatListVersion, chatIDs);
   }

   public OldChatList(int userID, byte[] chatListData) throws FusionException {
      super(userID);
      String gsonifiedChatList = ChatSyncCompression.adaptiveDecompressToString(chatListData);
      if (log.isDebugEnabled()) {
         log.debug("Unpacking gson chat list=" + gsonifiedChatList);
      }

      try {
         Gson gson = (new GsonBuilder()).setFieldNamingStrategy(OldChatList.OldChatListStrategy.getInstance()).create();
         OldChatList.ChatListValue wrapper = (OldChatList.ChatListValue)gson.fromJson(gsonifiedChatList, OldChatList.ChatListValue.class);
         this.chatListVersion = wrapper.chatListVersion;
         this.chatIDs = new ArrayList(Arrays.asList(wrapper.chatIDs));
      } catch (UnsupportedOperationException var6) {
         log.warn("An error ocurred unpacking the chatlist. Using empty chat list");
         this.chatListVersion = 0;
         this.chatIDs = new ArrayList();
      }

   }

   public String getValue() {
      OldChatList.ChatListValue wrapper = new OldChatList.ChatListValue();
      wrapper.chatListVersion = this.chatListVersion;
      String[] ids = new String[this.chatIDs.size()];
      wrapper.chatIDs = (String[])this.chatIDs.toArray(ids);
      Gson gson = (new GsonBuilder()).setFieldNamingStrategy(OldChatList.OldChatListStrategy.getInstance()).create();
      return gson.toJson(wrapper);
   }

   public void renewExpiry(SystemPropertyEntities.ChatSyncSettings toggle, ChatSyncStore store) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)toggle)) {
         int hrs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.OLD_CHAT_LISTS_EXPIRY_HOURS);
         store.expire(this, hrs * 3600);
      }

   }

   public void store(ChatSyncStore[] stores) throws FusionException {
      throw new FusionException("store() not implemented for " + this);
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
      this.userID = new Integer(storedKey);
      Gson gson = (new GsonBuilder()).setFieldNamingStrategy(OldChatList.OldChatListStrategy.getInstance()).create();
      OldChatList.ChatListValue wrapper = (OldChatList.ChatListValue)gson.fromJson(storedValue, OldChatList.ChatListValue.class);
      this.chatListVersion = wrapper.chatListVersion;
      this.chatIDs = new ArrayList(Arrays.asList(wrapper.chatIDs));
   }

   public String[] findMissingChats(String[] chatSupersetIDs) {
      ArrayList<String> missingIDs = new ArrayList();
      String[] result = chatSupersetIDs;
      int len$ = chatSupersetIDs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String chatID = result[i$];
         if (!this.chatIDs.contains(chatID)) {
            missingIDs.add(chatID);
         }
      }

      result = new String[missingIDs.size()];
      return (String[])missingIDs.toArray(result);
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      throw new FusionException("retrieve() not implemented for " + this);
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
      String algo = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.OLD_CHAT_LISTS_COMPRESSION_ALGORITHM);
      String gson = this.getValue();
      if (log.isDebugEnabled()) {
         log.debug("Storing OldChatList gson=" + gson + "  Size before compression=" + Redis.safeEncoderEncode(gson).length + "  Compression algo=" + algo);
      }

      if (StringUtil.isBlank(algo)) {
         pipelineStore.rpush(this, (String)gson);
      } else {
         byte[] compressed = ChatSyncCompression.compressFromString(gson, algo);
         if (log.isDebugEnabled()) {
            log.debug("Size after compression=" + compressed.length);
         }

         pipelineStore.rpush(this, (byte[])compressed);
      }

      this.renewExpiry(SystemPropertyEntities.ChatSyncSettings.RENEW_EXPIRY_ON_WRITE_ENABLED, pipelineStore);
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return startIndex + 2;
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(OldChatList.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }

   private static class OldChatListStrategy implements FieldNamingStrategy {
      private static final String CLV_ALIAS = "v";
      private static final String IDS_ALIAS = "i";

      private OldChatListStrategy() {
      }

      public static OldChatList.OldChatListStrategy getInstance() {
         return OldChatList.OldChatListStrategy.SingletonHolder.INSTANCE;
      }

      public String translateName(Field field) {
         if (field.getName().equals("chatListVersion")) {
            return "v";
         } else if (field.getName().equals("chatIDs")) {
            return "i";
         } else {
            throw new RuntimeException("Unexpected field name in OldChatListStrategy: " + field.getName());
         }
      }

      // $FF: synthetic method
      OldChatListStrategy(Object x0) {
         this();
      }

      private static class SingletonHolder {
         public static final OldChatList.OldChatListStrategy INSTANCE = new OldChatList.OldChatListStrategy();
      }
   }

   public static class ChatListValue {
      public int chatListVersion;
      public String[] chatIDs;
   }
}
