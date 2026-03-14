package com.projectgoth.fusion.botservice.bot.migbot.chatterbot.textcollection;

import com.projectgoth.fusion.botservice.bot.migbot.chatterbot.TextCollection;
import java.util.Locale;

public class PickupLines extends TextCollection {
   public PickupLines() {
      super("pickuplines", "Pickup Lines");
      this.loadTexts("resource.BoyFriend_Texts_PickupLines", new Locale("en"));
   }
}
