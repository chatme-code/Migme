package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.RewardedUnlockedStoreItemData;
import com.projectgoth.leto.common.event.userreward.UserRewardedWithStoreItemEvent;
import com.projectgoth.leto.common.storeitem.StoreItem;

public class ReferredUserRewardedWithUnlockedStoredItemTrigger extends ReferredUserRewardedBaseTrigger implements UserRewardedWithStoreItemEvent {
   private final RewardedUnlockedStoreItemData unlockedStoreItemData;

   public ReferredUserRewardedWithUnlockedStoredItemTrigger(UserData referrerUserData, UserData referredUserData, RewardProgramData referredUserRewardProgram, RewardedUnlockedStoreItemData unlockedStoreItemData) {
      super(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_STOREITEM, referrerUserData, referredUserData, referredUserRewardProgram);
      this.unlockedStoreItemData = unlockedStoreItemData;
      this.quantityDelta = unlockedStoreItemData.getQuantity();
      this.amountDelta = (double)unlockedStoreItemData.getQuantity() * unlockedStoreItemData.getPrice();
      this.currency = unlockedStoreItemData.getCurrency();
   }

   public RewardedUnlockedStoreItemData getRewardedStoreItemData() {
      return this.unlockedStoreItemData;
   }

   public StoreItem getStoreItem() {
      return this.getRewardedStoreItemData();
   }
}
