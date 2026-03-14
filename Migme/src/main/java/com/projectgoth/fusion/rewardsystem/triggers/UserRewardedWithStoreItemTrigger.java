package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.RewardedStoreItemData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.userreward.UserRewardedWithStoreItemEvent;
import com.projectgoth.leto.common.storeitem.StoreItem;
import java.util.Date;

public class UserRewardedWithStoreItemTrigger extends UserRewardedBaseTrigger implements UserRewardedWithStoreItemEvent {
   private final RewardedStoreItemData storeItemData;

   public UserRewardedWithStoreItemTrigger(UserData userData, RewardProgramData fulfilledUserRewardProgram, RewardedStoreItemData storeItemData, Date rewardedTime) {
      super(RewardProgramData.TypeEnum.USER_REWARDED_WITH_STOREITEM, userData, fulfilledUserRewardProgram, rewardedTime);
      this.storeItemData = storeItemData;
   }

   public RewardedStoreItemData getStoreItemData() {
      return this.storeItemData;
   }

   public StoreItem getStoreItem() {
      return this.getStoreItemData();
   }
}
