package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class ThirdPartyAppInternalInvitationTrigger extends RewardProgramTrigger {
   public String applicationName = "";

   public ThirdPartyAppInternalInvitationTrigger(ThirdPartyAppInternalInvitationTrigger.StateEnum state, UserData userData) {
      super(RewardProgramData.TypeEnum.MANUAL, userData);
      switch(state) {
      case SENT:
         super.programType = RewardProgramData.TypeEnum.THIRDPARTY_APP_INTERNAL_INVITATION_SENT;
         break;
      case ACCEPTED:
         super.programType = RewardProgramData.TypeEnum.THIRDPARTY_APP_INTERNAL_INVITATION_ACCEPTED;
      }

   }

   public static enum StateEnum {
      SENT(1),
      ACCEPTED(2);

      private int type;

      private StateEnum(int type) {
         this.type = type;
      }

      public int getType() {
         return this.type;
      }

      public static boolean isValid(int type) {
         return fromType(type) != null;
      }

      public static ThirdPartyAppInternalInvitationTrigger.StateEnum fromType(int type) {
         ThirdPartyAppInternalInvitationTrigger.StateEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ThirdPartyAppInternalInvitationTrigger.StateEnum e = arr$[i$];
            if (e.type == type) {
               return e;
            }
         }

         return null;
      }
   }
}
