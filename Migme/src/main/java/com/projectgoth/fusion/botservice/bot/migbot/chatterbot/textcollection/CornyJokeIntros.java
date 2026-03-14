package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class CornyJokeIntros extends TextCollection {
   public CornyJokeIntros() {
      super("cj_intros", "Corny Joke Intros");
      this.loadTexts("resource.BoyFriend_Texts_CornyJokeIntros", new Locale("en"));
   }
}
