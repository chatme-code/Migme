package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class GroupActivityTrigger extends RewardProgramTrigger {
   public String groupID = "";

   public GroupActivityTrigger(GroupActivityTrigger.ActivityTypeEnum activityType, UserData userData) {
      super(RewardProgramData.TypeEnum.MANUAL, userData);
      switch(activityType) {
      case TOPIC_CREATED:
         super.programType = RewardProgramData.TypeEnum.GROUP_TOPIC_CREATED;
         break;
      case TOPIC_COMMENTED:
         super.programType = RewardProgramData.TypeEnum.GROUP_TOPIC_COMMENT_CREATED;
         break;
      case WALLPOST_CREATED:
         super.programType = RewardProgramData.TypeEnum.GROUP_WALLPOST_CREATED;
         break;
      case WALLPOST_COMMENTED:
         super.programType = RewardProgramData.TypeEnum.GROUP_WALLPOST_COMMENT_CREATED;
      }

   }

   public static enum ActivityTypeEnum {
      TOPIC_CREATED(1),
      TOPIC_COMMENTED(2),
      WALLPOST_CREATED(3),
      WALLPOST_COMMENTED(4);

      private int type;

      private ActivityTypeEnum(int type) {
         this.type = type;
      }

      public int getType() {
         return this.type;
      }

      public static boolean isValid(int type) {
         return fromType(type) != null;
      }

      public static GroupActivityTrigger.ActivityTypeEnum fromType(int type) {
         GroupActivityTrigger.ActivityTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            GroupActivityTrigger.ActivityTypeEnum e = arr$[i$];
            if (e.type == type) {
               return e;
            }
         }

         return null;
      }
   }
}
