package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ContentUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.objectcache.Emote;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageDataIce;
import org.apache.log4j.Logger;

public class Sticker extends FilteringEmoteCommand {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Sticker.class));
   private static final String SPACE = " ";

   public Sticker(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected Logger getLog() {
      return log;
   }

   protected FilteringEmoteCommand.ProcessingResult doExecute(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws EmoteCommandException {
      try {
         if (chatSource.getChatType() != ChatSource.ChatType.CHATROOM_CHAT || SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
            try {
               MessageSwitchboardDispatcher.getInstance().onSendMessageToAllUsersInChat(messageData, messageData.username, chatSource.getSessionPrx(), chatSource.getSessionI());
            } catch (Exception var10) {
               log.warn("While storing sticker in chatsync: ", var10);
            }
         }

         String senderUsername = messageData.source;
         StickerDeliveredMessageData forDelivery = new StickerDeliveredMessageData(cmdArgs, messageData);
         chatSource.sendStickerEmotes(forDelivery);
         int chatRoomID = -1;
         int chatRoomGroupID = -1;
         if (chatSource.getChatType() == ChatSource.ChatType.CHATROOM_CHAT) {
            ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().chatRoomPrx;
            ChatRoomDataIce roomDataIce = chatRoomPrx.getRoomData();
            chatRoomID = roomDataIce.id;
            chatRoomGroupID = roomDataIce.groupID;
         }

         ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(senderUsername, cmdArgs[1], cmdArgs[0], chatRoomID, chatRoomGroupID, -1, (String)null);
         chatSource.getSessionI().logEmoteData(logData);
         return new FilteringEmoteCommand.ProcessingResult(EmoteCommand.ResultType.HANDLED_AND_STOP, true);
      } catch (FusionException var11) {
         throw new EmoteCommandException(var11);
      } catch (Exception var12) {
         throw new EmoteCommandException(var12);
      }
   }

   protected void checkSyntax(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws FusionException {
      if (!ContentUtils.isStickersEnabled()) {
         throw new FusionException("Invalid or disabled command.");
      } else if (cmdArgs.length != 2) {
         String cmd = this.getCommand(cmdArgs);
         throw new FusionException(String.format("Usage: %s [sticker name]", cmd));
      }
   }

   protected String getRateLimitThreshold(String[] cmdArgs, MessageData messageData, ChatSource chatSource) {
      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.STICKER_RATE_LIMIT);
   }

   protected void checkDevice(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws FusionException {
   }

   public static MessageDataIce createStickerEmotesForSender(MessageData messageData, String msgToInstigator, ClientType senderDeviceType, short senderClientVersion) throws FusionException {
      MessageDataIce messageIce = messageData.toIceObject();
      if (!ContentUtils.deviceCanReceiveStickersNatively(senderDeviceType, senderClientVersion) && messageIce.messageDestinations[0].type == MessageDestinationData.TypeEnum.INDIVIDUAL.value()) {
         String tmp = messageIce.source;
         messageIce.source = messageIce.messageDestinations[0].destination;
         messageIce.messageDestinations[0].destination = tmp;
      }

      messageIce.emoteContentType = MessageData.EmoteContentTypeEnum.STICKERS.value();
      messageIce.messageText = msgToInstigator;
      return messageIce;
   }

   public static MessageDataIce createStickerEmotesForRecipients(MessageData messageData, String msgToRecipient) throws FusionException {
      MessageDataIce messageIce = messageData.toIceObject();
      messageIce.messageText = msgToRecipient;
      messageIce.emoteContentType = MessageData.EmoteContentTypeEnum.STICKERS.value();
      return messageIce;
   }

   public static boolean isStickerCommand(MessageData msgData) {
      if (Emote.isEmote(msgData.messageText)) {
         String[] args = msgData.messageText.toLowerCase().split(" ");
         String command = args[0].substring(1);
         IcePrxFinder nullIPF = null;
         EmoteCommand ec = EmoteCommandFactory.getEmoteCommand(command, ChatSource.ChatType.fromDestinationType(((MessageDestinationData)msgData.messageDestinations.get(0)).type), (IcePrxFinder)nullIPF);
         return ec instanceof Sticker;
      } else {
         return false;
      }
   }
}
