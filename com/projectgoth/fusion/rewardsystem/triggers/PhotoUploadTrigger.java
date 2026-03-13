package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class PhotoUploadTrigger extends RewardProgramTrigger {
   public PhotoUploadTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.PHOTOS_UPLOADED, userData);
   }
}
