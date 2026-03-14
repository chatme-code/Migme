package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class PersonalProphecyIntros extends TextCollection {
   public PersonalProphecyIntros() {
      super("pp_intros", "Personal Prophecy Intros");
      this.loadTexts("resource.GirlFriend_Texts_PersonalProphecyIntros", new Locale("en"));
   }
}
