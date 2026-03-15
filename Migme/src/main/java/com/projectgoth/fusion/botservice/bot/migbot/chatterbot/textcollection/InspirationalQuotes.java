package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class InspirationalQuotes extends TextCollection {
   public InspirationalQuotes() {
      super("inspirationalquotes", "Inspirational Quotes");
      this.loadTexts("resource.GirlFriend_Texts_InspirationalQuotes", new Locale("en"));
   }
}
