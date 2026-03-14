package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class PersonalProphecies extends TextCollection {
   public PersonalProphecies() {
      super("personalprophecies", "Personal Prophecies");
      this.loadTexts("resource.GirlFriend_Texts_PersonalProphecies", new Locale("en"));
   }
}
