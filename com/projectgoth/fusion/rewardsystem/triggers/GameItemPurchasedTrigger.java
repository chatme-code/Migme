package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.purchase.GameItemPurchasedEvent;

public class GameItemPurchasedTrigger extends RewardProgramTrigger implements GameItemPurchasedEvent {
   public String reference;

   public GameItemPurchasedTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.GAME_ITEM_PURCHASED, userData);
   }

   public String getReference() {
      return this.reference;
   }
}
