package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class Description extends EmoteCommand {
   public Description(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length < 2) {
         throw new FusionException("Usage: /description [text]");
      } else {
         ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
         String rateLimitPerChatroom = SystemProperty.get("ChatroomDescriptionUpdateRateLimitExpr", "2/1M");
         ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
         super.checkRateLimit(Description.class, "c:" + roomData.id, rateLimitPerChatroom);
         String description = messageData.messageText.substring("/description ".length(), messageData.messageText.length());
         chatRoomPrx.updateDescription(messageData.source, description);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
            ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, "", args[0], roomData.id, roomData.groupID, -1, description);
            chatSource.getSessionI().logEmoteData(logData);
         }

         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
