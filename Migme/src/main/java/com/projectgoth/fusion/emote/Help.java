package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;

public class Help extends EmoteCommand {
   public Help(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String urlSuffix = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.HELP_COMMAND_URL_SUFFIX);
      return EmoteCommandUtils.createNewTab("help", urlSuffix, messageData, chatSource, this.emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
