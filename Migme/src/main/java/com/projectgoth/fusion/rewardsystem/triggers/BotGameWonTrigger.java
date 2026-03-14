package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.botgame.BotGameUserWonEvent;

public class BotGameWonTrigger extends RewardProgramTrigger implements BotGameUserWonEvent {
   public int botID;

   public BotGameWonTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.BOT_GAME_WON, userData);
   }

   public int getBotID() {
      return this.botID;
   }
}
