package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class Broadcast extends EmoteCommand {
   public Broadcast(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length < 2) {
         throw new FusionException("Usage: /broadcast [message]");
      } else {
         String message = messageData.messageText.substring("/broadcast ".length(), messageData.messageText.length());
         ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
         chatRoomPrx.broadcastMessage(messageData.source, message);
         if (message.length() > 128) {
            message = message.substring(0, 128);
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
            ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
            ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, "", args[0], roomData.id, roomData.groupID, -1, message);
            chatSource.getSessionI().logEmoteData(logData);
         }

         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
