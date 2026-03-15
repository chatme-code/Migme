package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class InspirationalQuoteIntros extends TextCollection {
   public InspirationalQuoteIntros() {
      super("ii_intro", "Inspirational Quote Intros);");
      this.loadTexts("resource.GirlFriend_Texts_InspirationalQuoteIntros", new Locale("en"));
   }
}
