package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class DefaultBoyFriendBotResponses extends TextCollection {
   public DefaultBoyFriendBotResponses() {
      super("defaultbf", "Default BoyFriendBot Responses");
      this.loadTexts("resource.BoyFriend_Texts_DefaultBoyFriendBotResponses", new Locale("en"));
   }
}
