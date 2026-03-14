package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class Lock extends EmoteCommand {
   public Lock(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
      ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
      String rateLimitPerChatroom = SystemProperty.get("ChatroomLockRateLimitExpr", "60/1M");
      super.checkRateLimit(Lock.class, "c:" + roomData.id, rateLimitPerChatroom);
      chatRoomPrx.lock(messageData.source);
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
         ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, "", args[0], roomData.id, roomData.groupID, -1, (String)null);
         chatSource.getSessionI().logEmoteData(logData);
      }

      return EmoteCommand.ResultType.HANDLED_AND_STOP;
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
