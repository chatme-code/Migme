package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class KickIndex extends EmoteCommand {
   public KickIndex(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length != 2) {
         throw new FusionException("Usage: /kickindex [index list comma separated]");
      } else {
         String[] kickStrs = args[1].split(",");
         int kickIndexMax = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.KICK_INDEX_MAX);
         if (kickIndexMax < kickStrs.length) {
            throw new FusionException("Number of indexes specified must not be more than " + kickIndexMax);
         } else {
            int[] kickInts = new int[kickStrs.length];

            for(int i = 0; i < kickStrs.length; ++i) {
               try {
                  kickInts[i] = Integer.parseInt(kickStrs[i]);
               } catch (NumberFormatException var11) {
                  throw new FusionException("The index list must be a list of comma separated numbers");
               }
            }

            String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.KICK_INDEX_RATE_LIMIT);
            super.checkRateLimit(KickIndex.class, "s:" + messageData.source, rateLimit);
            ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
            chatRoomPrx.kickIndexes(kickInts, messageData.source);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
               ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
               ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, args[1], args[0], roomData.id, roomData.groupID, -1, (String)null);
               chatSource.getSessionI().logEmoteData(logData);
            }

            return EmoteCommand.ResultType.HANDLED_AND_STOP;
         }
      }
   }
}
