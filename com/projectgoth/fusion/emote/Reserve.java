package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class Reserve extends EmoteCommand {
   public Reserve(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();

      int reserve;
      try {
         reserve = Integer.parseInt(args[1]);
      } catch (Exception var6) {
         throw new FusionException("Invalid argument to /reserve");
      }

      ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
      chatRoomPrx.setNumberOfFakeParticipants(messageData.source, reserve);
      return EmoteCommand.ResultType.HANDLED_AND_STOP;
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
