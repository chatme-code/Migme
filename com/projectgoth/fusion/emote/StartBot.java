package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.slice.FusionException;

public class StartBot implements ChatSourceVisitor {
   private String username;
   private String botCommandName;

   public StartBot(String username, String botCommandName) {
      this.username = username;
      this.botCommandName = botCommandName;
   }

   public void visit(ChatSource.LocalChatRoomChatSource chatSource) throws FusionException {
      chatSource.chatRoomPrx.startBot(this.username, this.botCommandName);
   }

   public void visit(ChatSource.RemoteChatRoomChatSource chatSource) throws FusionException {
      chatSource.chatRoom.startBot(this.username, this.botCommandName);
   }

   public void visit(ChatSource.LocalGroupChatChatSource chatSource) throws FusionException {
      chatSource.groupChatPrx.startBot(this.username, this.botCommandName);
   }

   public void visit(ChatSource.RemoteGroupChatChatSource chatSource) throws FusionException {
      chatSource.chatGroup.startBot(this.username, this.botCommandName);
   }

   public void visit(ChatSource.LocalPrivateChatChatSource chatSource) throws FusionException {
      throw new FusionException("/bot is not a valid command");
   }

   public void visit(ChatSource.RemotePrivateChatChatSource chatSource) throws FusionException {
      throw new FusionException("/bot is not a valid command");
   }
}
