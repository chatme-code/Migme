package com.projectgoth.fusion.chatsync;

import com.google.common.collect.Multimap;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.emote.EmoteCommand;
import com.projectgoth.fusion.emote.Sticker;
import com.projectgoth.fusion.emote.StickerDeliveredMessageData;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.gateway.packet.FusionPktMessage;
import com.projectgoth.fusion.gateway.packet.chatsync.FusionPktEndMessages;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.Logger;

public class LastNChatMessagesPusher extends ChatMessages {
   private static final LogFilter log;
   private static final String SPACE = " ";
   private final byte chatType;
   private final Integer maxMessages;
   private final SessionPrx session;
   private final boolean receivedPrivateMessagesOnly;
   private final ClientType deviceType;
   private final short clientVersion;
   private final boolean sessionIsJ2ME;
   private final String sessionParentUsername;
   private Short fusionPktTransactionId;
   private long queueingStartTime;
   private int messagesSent;

   public LastNChatMessagesPusher(ChatDefinition chatKey, byte chatType, Long oldestTimestamp, Long newestTimestamp, Integer maxMessages, SessionPrx session, String sessionParentUsername, ClientType deviceType, short clientVersion, Short fusionPktTransactionId) throws FusionException {
      this(chatKey, chatType, oldestTimestamp, newestTimestamp, maxMessages, session, sessionParentUsername, deviceType, clientVersion, fusionPktTransactionId, false);
   }

   public LastNChatMessagesPusher(ChatDefinition chatKey, byte chatType, Long oldestTimestamp, Long newestTimestamp, Integer maxMessages, SessionPrx session, String sessionParentUsername, ClientType deviceType, short clientVersion, Short fusionPktTransactionId, boolean receivedPrivateMessagesOnly) throws FusionException {
      super(chatKey, oldestTimestamp, newestTimestamp, maxMessages, sessionParentUsername);
      this.messagesSent = 0;
      this.chatType = chatType;
      this.maxMessages = maxMessages;
      this.session = session;
      this.receivedPrivateMessagesOnly = receivedPrivateMessagesOnly;
      this.deviceType = deviceType;
      this.clientVersion = clientVersion;
      this.sessionIsJ2ME = ClientType.MIDP2.equals(deviceType);
      this.sessionParentUsername = sessionParentUsername;
      this.fusionPktTransactionId = fusionPktTransactionId;
      this.queueingStartTime = System.currentTimeMillis();
   }

   public int getMessagesSent() {
      return this.messagesSent;
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.WALLCLOCK_TIME_STATS_ENABLED)) {
         ChatSyncStats.getInstance().addGetMessagesAsyncQueueingWallclockTime(System.currentTimeMillis() - this.queueingStartTime);
      }

      long startWallclock = System.currentTimeMillis();

      try {
         this.retrieveInner(stores);
      } finally {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.WALLCLOCK_TIME_STATS_ENABLED)) {
            ChatSyncStats.getInstance().addGetMessagesWallclockTime(System.currentTimeMillis() - startWallclock);
         }

      }

   }

   private void retrieveInner(ChatSyncStore[] stores) throws FusionException {
      super.retrieve(stores);
      if (log.isDebugEnabled() && this.retrievedMessages != null) {
         log.debug("Retrieved " + this.retrievedMessages.size() + "messages for user=" + this.sessionParentUsername);
      }

      int limit = this.maxMessages != null ? this.maxMessages : Integer.MAX_VALUE;
      if (log.isDebugEnabled()) {
         log.debug("Limiting messages to push to client to count=" + limit + " for user=" + this.sessionParentUsername);
      }

      int startIndex;
      if (this.retrievedMessages.size() > limit) {
         startIndex = this.retrievedMessages.size() - limit;
      } else {
         startIndex = 0;
      }

      if (log.isDebugEnabled()) {
         if (this.retrievedMessages != null) {
            log.debug("Retrieved message count=" + this.retrievedMessages.size() + " for user=" + this.sessionParentUsername);
         }

         log.debug("startIndex=" + startIndex + " and precedingMessage=" + this.precedingMessage + " for user=" + this.sessionParentUsername);
      }

      String lastGUID = null;
      ChatMessage unpacked;
      if (startIndex == 0) {
         if (this.precedingMessage != null) {
            unpacked = new ChatMessage(this.chatID, this.precedingMessage);
            lastGUID = unpacked.getGUID();
         }
      } else {
         unpacked = new ChatMessage(this.chatID, (byte[])this.retrievedMessages.get(startIndex - 1));
         lastGUID = unpacked.getGUID();
      }

      String firstMessageGUID = null;
      String finalMessageGUID = null;
      Multimap<Long, MessageStatusEvent> events = this.statuses != null ? this.statuses.getEventsByTimestamp() : null;

      for(int i = startIndex; i < this.retrievedMessages.size(); ++i) {
         byte[] result = (byte[])this.retrievedMessages.get(i);
         ChatMessage unpacked = new ChatMessage(this.chatID, result);
         MessageData msgData = unpacked.cloneMessageData();
         String sender = msgData.source;
         MessageDestinationData dest = (MessageDestinationData)msgData.messageDestinations.get(0);
         MessageDestinationData.TypeEnum destType = dest.type;
         boolean sendIt = !this.receivedPrivateMessagesOnly || destType == MessageDestinationData.TypeEnum.INDIVIDUAL && !sender.equals(this.sessionParentUsername);
         if (sendIt) {
            if (log.isDebugEnabled()) {
               log.debug("pushing message to destination=" + dest + " for chatID=" + this.chatID + " chatType=" + this.chatType + " with msgGUID=" + unpacked.getGUID() + " and session parent username=" + this.sessionParentUsername + " and messageText=" + msgData.messageText);
            }

            if (EmoteCommand.hasMessageVariables(msgData.messageText)) {
               String newText = EmoteCommand.processMessageVariables(msgData.messageText, this.sessionParentUsername, msgData.source);
               if (!newText.equals(msgData.messageText)) {
                  if (log.isDebugEnabled()) {
                     log.debug("Substituted emote template:" + msgData.messageText + " with:" + newText + "for sender=" + msgData.source + " recipient=" + this.sessionParentUsername);
                  }

                  unpacked.setMessageText(newText);
               }
            }

            this.stickerProcessing(unpacked, msgData);
            if (firstMessageGUID == null) {
               firstMessageGUID = unpacked.getGUID();
            }

            finalMessageGUID = unpacked.getGUID();
            FusionPktMessage pkt = unpacked.toFusionPktMessage(lastGUID, this.sessionIsJ2ME, this.sessionParentUsername, this.fusionPktTransactionId);
            if (events != null) {
               Collection<MessageStatusEvent> tsEvents = events.get(unpacked.getTimestamp());
               Iterator i$ = tsEvents.iterator();

               while(i$.hasNext()) {
                  MessageStatusEvent event = (MessageStatusEvent)i$.next();
                  if (unpacked.getGUID().equals(event.getMessageGUID())) {
                     pkt.setStatus(event.getMessageStatus());
                  }
               }
            }

            this.session.putSerializedPacket(pkt.toSerializedBytes());
            ++this.messagesSent;
         }

         lastGUID = unpacked.getGUID();
      }

      String returnChatID = this.chatID.internalToExternalChatID(this.sessionParentUsername);
      FusionPktEndMessages endMsgs = new FusionPktEndMessages(returnChatID, firstMessageGUID, finalMessageGUID, this.messagesSent, this.fusionPktTransactionId);
      this.session.putSerializedPacket(endMsgs.toSerializedBytes());
   }

   private void stickerProcessing(ChatMessage unpacked, MessageData msgData) {
      try {
         if (Sticker.isStickerCommand(msgData)) {
            String[] args = msgData.messageText.toLowerCase().split(" ");
            StickerDeliveredMessageData sdmd = new StickerDeliveredMessageData(args, msgData);
            MessageDataIce mdi;
            if (msgData.source.equals(this.sessionParentUsername)) {
               mdi = Sticker.createStickerEmotesForSender(sdmd.getMessageData(), sdmd.getMessageToInstigator(), this.deviceType, this.clientVersion);
            } else {
               mdi = Sticker.createStickerEmotesForRecipients(sdmd.getMessageData(), sdmd.getMessageToRecipients());
            }

            unpacked.setMessageData(new MessageData(mdi));
         }
      } catch (Exception var6) {
         log.warn("Sticker processing failed for message=" + msgData.messageText);
      }

   }

   public boolean canRetryReads() throws FusionException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRY_GET_MESSAGES_ENABLED);
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(LastNChatMessagesPusher.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
