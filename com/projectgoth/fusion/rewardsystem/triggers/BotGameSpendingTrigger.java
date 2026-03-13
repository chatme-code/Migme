package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.botgame.BotGameUserSpendingEvent;

public class BotGameSpendingTrigger extends RewardProgramTrigger implements BotGameUserSpendingEvent {
   public int botID;

   public BotGameSpendingTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.BOT_GAME_SPENDING, userData);
   }

   public int getBotID() {
      return this.botID;
   }
}
