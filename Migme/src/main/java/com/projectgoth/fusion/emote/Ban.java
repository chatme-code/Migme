package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import java.util.Arrays;

public class Ban extends EmoteCommand {
   public Ban(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length < 3) {
         throw new FusionException("Usage: /ban [username] " + Enums.GroupBanReasonEnum.stringifyValues());
      } else {
         super.checkRateLimit(Ban.class, "s:" + messageData.source, "");
         boolean var4 = true;

         int reasonCode;
         try {
            reasonCode = Integer.parseInt(args[args.length - 1]);
            if (!Enums.GroupBanReasonEnum.isValid(reasonCode)) {
               throw new FusionException("Please provide a valid reason code. Valid reason codes are: " + Enums.GroupBanReasonEnum.stringifyValues());
            }
         } catch (NumberFormatException var10) {
            throw new FusionException("Please provide a valid reason code. Valid reason codes are: " + Enums.GroupBanReasonEnum.stringifyValues());
         }

         ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
         ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
         super.checkRateLimit(Ban.class, "t:" + args[1] + ":" + roomData.id + ":" + roomData.groupID, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.BAN_RATE_LIMIT));
         java.util.List<String> list = Arrays.asList(args).subList(1, args.length - 1);
         String[] bannedList = new String[list.size()];
         list.toArray(bannedList);
         chatRoomPrx.banGroupMembers(bannedList, messageData.source, reasonCode);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
            ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, args[1], args[0], roomData.id, roomData.groupID, reasonCode, (String)null);
            chatSource.getSessionI().logEmoteData(logData);
         }

         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
