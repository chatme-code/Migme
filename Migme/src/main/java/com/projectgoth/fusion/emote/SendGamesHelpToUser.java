package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.slice.FusionException;

public class SendGamesHelpToUser implements ChatSourceVisitor {
   private String username;

   public SendGamesHelpToUser(String username) {
      this.username = username;
   }

   public void visit(ChatSource.LocalChatRoomChatSource chatSource) throws FusionException {
      chatSource.chatRoomPrx.sendGamesHelpToUser(this.username);
   }

   public void visit(ChatSource.RemoteChatRoomChatSource chatSource) throws FusionException {
      chatSource.chatRoom.sendGamesHelpToUser(this.username);
   }

   public void visit(ChatSource.LocalGroupChatChatSource chatSource) throws FusionException {
      chatSource.groupChatPrx.sendGamesHelpToUser(this.username);
   }

   public void visit(ChatSource.RemoteGroupChatChatSource chatSource) throws FusionException {
      chatSource.chatGroup.sendGamesHelpToUser(this.username);
   }

   public void visit(ChatSource.LocalPrivateChatChatSource chatSource) throws FusionException {
      throw new FusionException("/games is not a valid command");
   }

   public void visit(ChatSource.RemotePrivateChatChatSource chatSource) throws FusionException {
      throw new FusionException("/games is not a valid command");
   }
}
