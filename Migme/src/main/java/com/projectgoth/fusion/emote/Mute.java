package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class Mute extends EmoteCommand {
   public Mute(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length < 2) {
         throw new FusionException("Usage: /mute [username]");
      } else {
         ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
         String rateLimitPerUser = SystemProperty.get("MutePerInstigatorRateLimitExpr", "30/1M");
         super.checkRateLimit(Mute.class, "s:" + messageData.source, rateLimitPerUser);
         chatRoomPrx.mute(messageData.source, args[1]);
         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
