package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;

public class Bot extends EmoteCommand {
   public Bot(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length == 1) {
         throw new FusionException("/bot is not a valid command");
      } else {
         if ("stop".equals(args[1])) {
            chatSource.accept(new StopBot(messageData.source, (String)null));
         } else {
            chatSource.accept(new StartBot(messageData.source, args[1]));
         }

         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
