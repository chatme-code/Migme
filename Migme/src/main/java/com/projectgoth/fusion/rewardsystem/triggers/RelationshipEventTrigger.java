package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.friending.FriendingRelationshipType;
import com.projectgoth.leto.common.utils.enums.ValueToEnumMap;
import java.util.Map;

public abstract class RelationshipEventTrigger extends RewardProgramTrigger {
   private UserData otherUserData;
   private RelationshipEventTrigger.RelationshipEventTypeEnum relationshipEvent;
   private static final String TMPLT_DATA_KEY_TRIGGER_OTHER_USER_DATA_ID = "trigger.otherUserData.userid";
   private static final String TMPLT_DATA_KEY_TRIGGER_OTHER_USER_DATA_USERNAME = "trigger.otherUserData.username";
   private static final String TMPLT_DATA_KEY_TRIGGER_OTHER_USER_DATA_DISPLAY_NAME = "trigger.otherUserData.displayName";
   private static final String TMPLT_DATA_KEY_TRIGGER_OTHER_USER_DATA_DISPLAY_PICTURE = "trigger.otherUserData.displayPicture";
   private static final String TMPLT_DATA_KEY_TRIGGER_OTHER_USER_DATA_AVATAR = "trigger.otherUserData.avatar";
   private static final String TMPLT_DATA_KEY_TRIGGER_OTHER_USER_DATA_FULLBODY_AVATAR = "trigger.otherUserData.fullbodyAvatar";
   private static final String TMPLT_DATA_KEY_TRIGGER_OTHER_USER_DATA_LANGUAGE = "trigger.otherUserData.language";
   private static final String TMPLT_DATA_KEY_TRIGGER_OTHER_USER_DATA_EMAILADDRESS = "trigger.otherUserData.emailAddress";
   private static final String TMPLT_DATA_KEY_TRIGGER_OTHER_USER_DATA_MOBILEPHONE = "trigger.otherUserData.mobilePhone";

   public RelationshipEventTrigger(RewardProgramData.TypeEnum programType, UserData thisUserData, UserData otherUserData, RelationshipEventTrigger.RelationshipEventTypeEnum relationshipEventType) {
      super(programType, thisUserData);
      this.quantityDelta = 1;
      this.amountDelta = 0.0D;
      this.currency = "USD";
      this.otherUserData = otherUserData;
      this.relationshipEvent = relationshipEventType;
   }

   public UserData getOtherUserData() {
      return this.otherUserData;
   }

   public UserData getThisUserData() {
      return this.userData;
   }

   public RelationshipEventTrigger.RelationshipEventTypeEnum getRelationshipEvent() {
      return this.relationshipEvent;
   }

   protected final void fillTemplateDataMap(Map<String, String> templateContextMap) {
      if (this.otherUserData != null) {
         if (this.otherUserData.userID != null) {
            templateContextMap.put("trigger.otherUserData.userid", this.otherUserData.userID.toString());
         }

         templateContextMap.put("trigger.otherUserData.username", this.otherUserData.username);
         templateContextMap.put("trigger.otherUserData.displayName", this.otherUserData.displayName);
         templateContextMap.put("trigger.otherUserData.displayPicture", this.otherUserData.displayPicture);
         templateContextMap.put("trigger.otherUserData.avatar", this.otherUserData.avatar);
         templateContextMap.put("trigger.otherUserData.fullbodyAvatar", this.otherUserData.fullbodyAvatar);
         templateContextMap.put("trigger.otherUserData.language", this.otherUserData.language);
         String emailAddress = StringUtil.isBlank(this.otherUserData.emailAddress) ? "N/A" : this.otherUserData.emailAddress;
         templateContextMap.put("trigger.otherUserData.emailAddress", emailAddress);
         String mobilePhone = StringUtil.isBlank(this.otherUserData.mobilePhone) ? "N/A" : this.otherUserData.mobilePhone;
         templateContextMap.put("trigger.otherUserData.mobilePhone", mobilePhone);
      }

   }

   public static enum RelationshipEventTypeEnum implements EnumUtils.IEnumValueGetter<Integer> {
      NEW(FriendingRelationshipType.NEW),
      REMOVED(FriendingRelationshipType.REMOVED),
      MUTUALLY_FOLLOWING(FriendingRelationshipType.MUTUALLY_FOLLOWING);

      private final FriendingRelationshipType friendingRelationshipType;

      private RelationshipEventTypeEnum(FriendingRelationshipType friendingRelationshipType) {
         this.friendingRelationshipType = friendingRelationshipType;
      }

      public int getValue() {
         return this.getEnumValue();
      }

      public Integer getEnumValue() {
         return this.friendingRelationshipType.getEnumValue();
      }

      public FriendingRelationshipType toFriendingRelationshipType() {
         return this.friendingRelationshipType;
      }

      public static RelationshipEventTrigger.RelationshipEventTypeEnum fromValue(int value) {
         return (RelationshipEventTrigger.RelationshipEventTypeEnum)RelationshipEventTrigger.RelationshipEventTypeEnum.ValueToEnumMapInstance.INSTANCE.toEnum(value);
      }

      private static final class ValueToEnumMapInstance {
         private static final ValueToEnumMap<Integer, RelationshipEventTrigger.RelationshipEventTypeEnum> INSTANCE = new ValueToEnumMap(RelationshipEventTrigger.RelationshipEventTypeEnum.class);
      }
   }
}
