package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.RewardedUnlockedStoreItemData;
import com.projectgoth.leto.common.event.userreward.UserRewardedWithUnlockedStoreItemEvent;
import com.projectgoth.leto.common.storeitem.StoreItem;
import java.util.Date;

public class UserRewardedWithUnlockedStoreItemTrigger extends UserRewardedBaseTrigger implements UserRewardedWithUnlockedStoreItemEvent {
   private final RewardedUnlockedStoreItemData unlockedStoreItemData;

   public UserRewardedWithUnlockedStoreItemTrigger(UserData userData, RewardProgramData fulfilledUserRewardProgram, RewardedUnlockedStoreItemData unlockedStoreItemData, Date rewardedTime) {
      super(RewardProgramData.TypeEnum.USER_REWARDED_WITH_UNLOCKED_STOREITEM, userData, fulfilledUserRewardProgram, rewardedTime);
      this.unlockedStoreItemData = unlockedStoreItemData;
      this.quantityDelta = unlockedStoreItemData.getQuantity();
   }

   public StoreItem getStoreItem() {
      return this.unlockedStoreItemData;
   }
}
