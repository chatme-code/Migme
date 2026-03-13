package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class BanIndex extends EmoteCommand {
   public BanIndex(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length != 3) {
         throw new FusionException("Usage: /banindex [index list comma separated] " + Enums.GroupBanReasonEnum.stringifyValues());
      } else {
         boolean var4 = true;

         int reasonCode;
         try {
            reasonCode = Integer.parseInt(args[args.length - 1]);
            if (!Enums.GroupBanReasonEnum.isValid(reasonCode)) {
               throw new FusionException("Please provide a valid reason code. Valid reason codes are: " + Enums.GroupBanReasonEnum.stringifyValues());
            }
         } catch (NumberFormatException var13) {
            throw new FusionException("Please provide a valid reason code. Valid reason codes are: " + Enums.GroupBanReasonEnum.stringifyValues());
         }

         String[] banStrs = args[1].split(",");
         int banIndexMax = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.BAN_INDEX_MAX);
         if (banIndexMax < banStrs.length) {
            throw new FusionException("Number of indexes specified must not be more than " + banIndexMax);
         } else {
            int[] banInts = new int[banStrs.length];

            for(int i = 0; i < banStrs.length; ++i) {
               try {
                  banInts[i] = Integer.parseInt(banStrs[i]);
               } catch (NumberFormatException var12) {
                  throw new FusionException("The index list must be a list of comma separated numbers");
               }
            }

            String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.BAN_INDEX_RATE_LIMIT);
            super.checkRateLimit(BanIndex.class, "s:" + messageData.source, rateLimit);
            ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
            chatRoomPrx.banIndexes(banInts, messageData.source, reasonCode);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
               ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
               ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, args[1], args[0], roomData.id, roomData.groupID, reasonCode, (String)null);
               chatSource.getSessionI().logEmoteData(logData);
            }

            return EmoteCommand.ResultType.HANDLED_AND_STOP;
         }
      }
   }
}
