package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class DefaultGirlFriendBotResponses extends TextCollection {
   public DefaultGirlFriendBotResponses() {
      super("defaultgf", "Default GirlFriendBot Responses");
      this.loadTexts("resource.GirlFriend_Texts_DefaultGirlFriendBotResponses", new Locale("en"));
   }
}
