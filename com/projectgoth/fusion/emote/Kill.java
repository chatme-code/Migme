package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class Kill extends EmoteCommand {
   public Kill(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.KILL_RATE_LIMIT);
      super.checkRateLimit(Kill.class, "s:" + messageData.source, rateLimit);
      ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
      chatRoomPrx.banMultiIds(messageData.source);
      return EmoteCommand.ResultType.HANDLED_AND_STOP;
   }
}
