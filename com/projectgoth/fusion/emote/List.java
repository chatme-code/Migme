package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class List extends EmoteCommand {
   private final String USAGE_STR = "Usage: /list [size] [start index (optional)]";

   public List(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      int size = false;
      int startIndex = 0;
      if (args.length >= 2) {
         int size;
         try {
            size = Integer.parseInt(args[1]);
         } catch (NumberFormatException var9) {
            throw new FusionException("Usage: /list [size] [start index (optional)]");
         }

         if (size < 1) {
            throw new FusionException("You must specify a number larger than 1 for your first parameter");
         } else {
            if (args.length >= 3) {
               try {
                  startIndex = Integer.parseInt(args[2]);
               } catch (NumberFormatException var8) {
                  throw new FusionException("Usage: /list [size] [start index (optional)]");
               }

               if (startIndex < 1) {
                  throw new FusionException("You must specify a number larger than 1 for your second parameter");
               }

               --startIndex;
            }

            String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LIST_RATE_LIMIT);
            super.checkRateLimit(List.class, "s:" + messageData.source, rateLimit);
            ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
            chatRoomPrx.listParticipants(messageData.source, size, startIndex);
            return EmoteCommand.ResultType.HANDLED_AND_STOP;
         }
      } else {
         throw new FusionException("Usage: /list [size] [start index (optional)]");
      }
   }
}
