package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;

public class Unsilence extends EmoteCommand {
   private static final String USAGE_STR = "Usage: /unsilence or /unsilence [username]";

   public Unsilence(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length == 1) {
         ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
         chatRoomPrx.unsilence(messageData.source);
      } else {
         if (args.length != 2) {
            throw new FusionException("Usage: /unsilence or /unsilence [username]");
         }

         String usernameToUnsilence = args[1];
         ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
         chatRoomPrx.unsilenceUser(messageData.source, usernameToUnsilence);
      }

      return EmoteCommand.ResultType.HANDLED_AND_STOP;
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
