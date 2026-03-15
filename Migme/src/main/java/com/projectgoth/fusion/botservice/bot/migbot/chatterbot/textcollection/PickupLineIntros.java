package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class PickupLineIntros extends TextCollection {
   public PickupLineIntros() {
      super("pl_intros", "Pickup Line Intros");
      this.loadTexts("resource.BoyFriend_Texts_PickupLineIntros", new Locale("en"));
   }
}
