package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.packet.chatsync.FusionPktChat;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SessionPrx;
import org.apache.log4j.Logger;

public class ClosedChatNotificationPusher extends ChatDefinition {
   private static final LogFilter log;
   private int userID;
   private String username;
   private short txnID;
   private SessionPrx[] sessions;

   public ClosedChatNotificationPusher(int userID, String username, byte chatType, String chatID, short txnID, SessionPrx[] sessions) throws FusionException {
      super(chatID, chatType);
      this.userID = userID;
      this.username = username;
      this.txnID = txnID;
      this.sessions = sessions;
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      try {
         super.retrieve(stores);
      } catch (ChatDefinition.ChatDefinitionNotFoundException var10) {
      }

      ChatSyncStore store = stores[0];
      ChatListVersion versionKey = new ChatListVersion(this.userID);
      versionKey.retrieve(stores);
      int chatListVer = versionKey.getVersion();
      FusionPktChat chat = this.toFusionPktChat(this.txnID, chatListVer, System.currentTimeMillis(), this.username);
      chat.setIsClosedChat((byte)1);
      if (log.isDebugEnabled()) {
         log.debug("ClosedChatNotificationPusher: converted ChatDefinition to pkt=" + chat);
      }

      SessionPrx[] arr$ = this.sessions;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         SessionPrx sessionPrx = arr$[i$];
         if (log.isDebugEnabled()) {
            log.debug("push closed chat notification to session=" + sessionPrx.getSessionID());
         }

         sessionPrx.putSerializedPacket(chat.toSerializedBytes());
      }

   }

   public boolean canRetryReads() throws FusionException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRY_CLOSED_CHAT_NOTIFICATION_ENABLED);
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ClosedChatNotificationPusher.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
