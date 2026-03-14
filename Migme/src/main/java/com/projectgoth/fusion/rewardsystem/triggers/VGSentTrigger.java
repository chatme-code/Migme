package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.gifting.VGSentUserEvent;
import com.projectgoth.leto.common.user.UserDetails;

public class VGSentTrigger extends RewardProgramTrigger implements VGSentUserEvent {
   public int virtualGiftID;
   private UserData receiverUserData;
   public boolean fromSenderInventory;

   public VGSentTrigger(UserData userData, UserData receiverUserData) {
      super(RewardProgramData.TypeEnum.VIRTUAL_GIFT_SENT, userData);
      this.receiverUserData = receiverUserData;
   }

   public UserData getReceiverUserData() {
      return this.receiverUserData;
   }

   public int getVirtualGiftID() {
      return this.virtualGiftID;
   }

   public UserDetails getReceiverUser() {
      return this.receiverUserData;
   }
}
