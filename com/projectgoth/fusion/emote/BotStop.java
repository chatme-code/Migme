package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;

public class BotStop extends EmoteCommand {
   public BotStop(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length == 1) {
         throw new FusionException("/botstop is not a valid command");
      } else if (!args[1].equals("!")) {
         throw new FusionException("/botstop " + args[1] + " is not a valid command");
      } else {
         int timeout = 0;
         if (args.length > 2) {
            try {
               timeout = Integer.parseInt(args[2]);
               if (timeout < 120 || timeout > 3600) {
                  throw new FusionException("Timeout must be between 120 and 3600 seconds");
               }
            } catch (NumberFormatException var6) {
               throw new FusionException("Invalid timeout value");
            }
         }

         chatSource.accept(new StopAllBots(messageData.source, timeout));
         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
