package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.slice.FusionException;

public interface ChatSourceVisitor {
   void visit(ChatSource.LocalChatRoomChatSource var1) throws FusionException;

   void visit(ChatSource.RemoteChatRoomChatSource var1) throws FusionException;

   void visit(ChatSource.LocalGroupChatChatSource var1) throws FusionException;

   void visit(ChatSource.RemoteGroupChatChatSource var1) throws FusionException;

   void visit(ChatSource.LocalPrivateChatChatSource var1) throws FusionException;

   void visit(ChatSource.RemotePrivateChatChatSource var1) throws FusionException;
}
