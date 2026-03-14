package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.slice.FusionException;

public class StopAllBots implements ChatSourceVisitor {
   private String username;
   private int timeout;

   public StopAllBots(String username, int timeout) {
      this.username = username;
      this.timeout = timeout;
   }

   public void visit(ChatSource.LocalChatRoomChatSource chatSource) throws FusionException {
      chatSource.chatRoomPrx.stopAllBots(this.username, this.timeout);
   }

   public void visit(ChatSource.RemoteChatRoomChatSource chatSource) throws FusionException {
      chatSource.chatRoom.stopAllBots(this.username, this.timeout);
   }

   public void visit(ChatSource.LocalGroupChatChatSource chatSource) throws FusionException {
      chatSource.groupChatPrx.stopAllBots(this.username, this.timeout);
   }

   public void visit(ChatSource.RemoteGroupChatChatSource chatSource) throws FusionException {
      chatSource.chatGroup.stopAllBots(this.username, this.timeout);
   }

   public void visit(ChatSource.LocalPrivateChatChatSource chatSource) throws FusionException {
      throw new FusionException("/botstop is not a valid command");
   }

   public void visit(ChatSource.RemotePrivateChatChatSource chatSource) throws FusionException {
      throw new FusionException("/botstop is not a valid command");
   }
}
