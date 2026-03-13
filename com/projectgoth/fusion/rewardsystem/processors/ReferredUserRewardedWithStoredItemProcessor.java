package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.StoreItemData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedBaseTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithStoredItemTrigger;

public class ReferredUserRewardedWithStoredItemProcessor extends ReferredUserRewardedBaseProcessor {
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDED_STORE_ITEM_IDS_PARAM_KEY = "refdUsrRwdSItmIDs";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDED_STORE_ITEM_IDS_IS_WHITELIST_PARAM_KEY = "refdUsrRwdSItmIDsIsWhiteLst";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDED_STORE_ITEM_TYPES_PARAM_KEY = "refdUsrRwdSItmTypes";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDED_STORE_ITEM_TYPES_IS_WHITELIST_PARAM_KEY = "refdUsrRwdSItmTypesIsWhiteLst";

   protected boolean processInternal(RewardProgramData programData, ReferredUserRewardedBaseTrigger trigger) throws Exception {
      if (!(trigger instanceof ReferredUserRewardedWithStoredItemTrigger)) {
         return false;
      } else {
         ReferredUserRewardedWithStoredItemTrigger rfdUserRewardedWithStoreItemTrigger = (ReferredUserRewardedWithStoredItemTrigger)trigger;
         return matchesStoreItemIDConstraint(programData, rfdUserRewardedWithStoreItemTrigger) && matchesStoreItemTypesConstraint(programData, rfdUserRewardedWithStoreItemTrigger);
      }
   }

   private static boolean matchesStoreItemIDConstraint(RewardProgramData programData, ReferredUserRewardedWithStoredItemTrigger rfdUserRewardedWithStoreItemTrigger) {
      return programData.matchesSetOfStringsConstraint("refdUsrRwdSItmIDs", "refdUsrRwdSItmIDsIsWhiteLst", String.valueOf(rfdUserRewardedWithStoreItemTrigger.getRewardedStoreItemData().getId()));
   }

   private static boolean matchesStoreItemTypesConstraint(RewardProgramData programData, ReferredUserRewardedWithStoredItemTrigger rfdUserRewardedWithStoreItemTrigger) {
      StoreItemData.TypeEnum storeItemType = rfdUserRewardedWithStoreItemTrigger.getRewardedStoreItemData().getType();
      return programData.matchesSetOfStringsConstraint("refdUsrRwdSItmTypes", "refdUsrRwdSItmTypesIsWhiteLst", storeItemType == null ? "null" : String.valueOf(storeItemType.value()));
   }
}
