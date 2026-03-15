package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.slice.FusionException;

public abstract class ChatSyncStorePipeline {
   public abstract void execute(ChatSyncEntity var1, ChatSyncStore var2) throws FusionException;
}
