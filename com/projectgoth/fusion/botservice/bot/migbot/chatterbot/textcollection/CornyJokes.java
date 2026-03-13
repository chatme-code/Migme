package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class CornyJokes extends TextCollection {
   public CornyJokes() {
      super("cornyjokes", "Corny Jokes");
      this.loadTexts("resource.BoyFriend_Texts_CornyJokes", new Locale("en"));
   }
}
