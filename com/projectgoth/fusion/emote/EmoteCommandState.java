package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;

public abstract class EmoteCommandState {
   public EmoteCommand.ResultType execute(EmoteCommandData emoteCommandData, MessageData messageData, ChatSource chatSource) throws FusionException {
      return EmoteCommand.ResultType.NOTHANDLED;
   }

   public abstract void cleanUp();
}
