package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.emote.EmoteCommand;
import com.projectgoth.fusion.emote.Sticker;
import com.projectgoth.fusion.emote.StickerDeliveredMessageData;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.List;
import org.apache.log4j.Logger;

public class MessageSendLiveSyncer implements ChatSyncEntity {
   private static final LogFilter log;
   private static final String SPACE = " ";
   private SessionPrx currentSession;
   private MessageDataIce msg;
   private UserDataIce senderUserData;
   private UserPrx sender;
   private GroupChatPrx groupChat;

   public MessageSendLiveSyncer(SessionPrx currentSession, MessageDataIce msg, UserDataIce senderUserData, UserPrx sender, GroupChatPrx groupChat) throws FusionException {
      this.currentSession = currentSession;
      this.msg = msg;
      this.senderUserData = senderUserData;
      this.sender = sender;
      this.groupChat = groupChat;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("MessageSendLiveSyncer.store() for sender=" + this.senderUserData.username);
      }

      this.forwardToSendersOtherSessions(this.currentSession, this.msg, this.senderUserData, this.sender);
   }

   private void forwardToSendersOtherSessions(SessionPrx currentSession, MessageDataIce msg, UserDataIce senderUserData, UserPrx sender) throws FusionException {
      SessionPrx[] senderSessions = sender.getSessions();
      if (log.isDebugEnabled()) {
         log.debug("forwardToSendersOtherSessions: sender=" + senderUserData.username + " session count=" + senderSessions.length);
      }

      MessageDataIce copy = (new MessageData(msg)).toIceObject();
      StickerDeliveredMessageData stickerMessageData = null;
      String currentSessionID;
      if (EmoteCommand.hasMessageVariables(copy.messageText)) {
         currentSessionID = EmoteCommand.processMessageVariables(copy.messageText, senderUserData.username, senderUserData.username);
         if (!currentSessionID.equals(copy.messageText)) {
            if (log.isDebugEnabled()) {
               log.debug("forwardToSendersOtherSessions: Substituted emote template:" + copy.messageText + " with:" + currentSessionID + "for live synched message");
            }

            copy.messageText = currentSessionID;
         }
      } else if (Sticker.isStickerCommand(new MessageData(msg))) {
         try {
            String[] args = msg.messageText.toLowerCase().split(" ");
            stickerMessageData = new StickerDeliveredMessageData(args, new MessageData(msg));
         } catch (Exception var13) {
            log.warn("Sticker processing failed while live synching message=" + msg.messageText + " cause=" + var13, var13);
         }
      }

      currentSessionID = currentSession.getSessionID();
      SessionPrx[] arr$ = senderSessions;
      int len$ = senderSessions.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         SessionPrx session = arr$[i$];
         if (!session.getSessionID().equals(currentSessionID)) {
            if (stickerMessageData != null) {
               copy = Sticker.createStickerEmotesForSender(stickerMessageData.getMessageData(), stickerMessageData.getMessageToInstigator(), ClientType.fromValue(session.getDeviceTypeAsInt()), session.getClientVersionIce());
            }

            this.forwardToSenderSession(session, copy, senderUserData);
         } else if (log.isDebugEnabled()) {
            log.debug("forwardToSendersOtherSessions: sender=" + senderUserData.username + " not ccing message to session=" + session + " as is current");
         }
      }

   }

   private void forwardToSenderSession(SessionPrx session, MessageDataIce copy, UserDataIce senderUserData) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("forwardToSenderSession: sender=" + senderUserData.username + " ccing message to session=" + session);
         }

         session.putMessage(copy);
      } catch (Exception var5) {
         log.warn("Exception live-syncing message to other sessions of sender=" + senderUserData.username + " : " + var5, var5);
      }

   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.MESSAGE;
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
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRY_LIVESYNC_MESSAGE_ENABLED);
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(MessageSendLiveSyncer.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
