package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.referreduserrewarded.ReferredUserRewardedEvent;
import com.projectgoth.leto.common.user.UserDetails;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public abstract class ReferredUserRewardedBaseTrigger extends RewardProgramTrigger implements ReferredUserRewardedEvent {
   public static final String TMPLT_DATA_KEY_TRIGGER_REFERRED_USER_REWARDED_USER_ID = "trigger.referredUserRewarded.userid";
   public static final String TMPLT_DATA_KEY_TRIGGER_REFERRED_USER_REWARDED_USER_NAME = "trigger.referredUserRewarded.username";
   public static final String TMPLT_DATA_KEY_TRIGGER_REFERRED_USER_REWARDED_DISPLAY_NAME = "trigger.referredUserRewarded.displayName";
   public static final String TMPLT_DATA_KEY_TRIGGER_REFERRED_USER_REWARDED_DISPLAY_PICTURE = "trigger.referredUserRewarded.displayPicture";
   public static final String TMPLT_DATA_KEY_TRIGGER_REFERRED_USER_REWARDED_AVATAR = "trigger.referredUserRewarded.avatar";
   public static final String TMPLT_DATA_KEY_TRIGGER_REFERRED_USER_REWARDED_FULLBODY_AVATAR = "trigger.referredUserRewarded.fullbodyAvatar";
   public static final String TMPLT_DATA_KEY_TRIGGER_REFERRED_USER_REWARDED_LANGUAGE = "trigger.referredUserRewarded.language";
   public static final String TMPLT_DATA_KEY_TRIGGER_REFERRED_USER_REWARDED_MOBILEPHONE = "trigger.referredUserRewarded.mobilePhone";
   public static final String TMPLT_DATA_KEY_TRIGGER_REFERRED_USER_REWARDED_EMAILADDRESS = "trigger.referredUserRewarded.emailAddress";
   private final UserData referredUserData;
   private final RewardProgramData referredUserRewardProgram;
   private final Date rewardedTime;

   protected ReferredUserRewardedBaseTrigger(RewardProgramData.TypeEnum programType, UserData referrerUserData, UserData referredUserData, RewardProgramData referredUserRewardProgram) {
      super(programType, referrerUserData);
      this.referredUserData = referredUserData;
      this.referredUserRewardProgram = referredUserRewardProgram;
      this.rewardedTime = new Timestamp(System.currentTimeMillis());
   }

   public UserData getReferrerUserData() {
      return this.userData;
   }

   public UserData getReferredUserData() {
      return this.referredUserData;
   }

   public RewardProgramData getReferredUserRewardProgram() {
      return this.referredUserRewardProgram;
   }

   protected final void fillTemplateDataMap(Map<String, String> templateContextMap) {
      if (this.referredUserData != null) {
         if (this.referredUserData.userID != null) {
            templateContextMap.put("trigger.referredUserRewarded.userid", this.referredUserData.userID.toString());
         }

         templateContextMap.put("trigger.referredUserRewarded.username", this.referredUserData.username);
         templateContextMap.put("trigger.referredUserRewarded.displayName", this.referredUserData.displayName);
         templateContextMap.put("trigger.referredUserRewarded.displayPicture", this.referredUserData.displayPicture);
         templateContextMap.put("trigger.referredUserRewarded.avatar", this.referredUserData.avatar);
         templateContextMap.put("trigger.referredUserRewarded.fullbodyAvatar", this.referredUserData.fullbodyAvatar);
         templateContextMap.put("trigger.referredUserRewarded.language", this.referredUserData.language);
         String emailAddress = StringUtil.isBlank(this.referredUserData.emailAddress) ? "N/A" : this.referredUserData.emailAddress;
         templateContextMap.put("trigger.referredUserRewarded.emailAddress", emailAddress);
         String mobilePhone = StringUtil.isBlank(this.referredUserData.mobilePhone) ? "N/A" : this.referredUserData.mobilePhone;
         templateContextMap.put("trigger.referredUserRewarded.mobilePhone", mobilePhone);
      }

      this.fillTemplateDataMapForReferredUserRewardedTrigger(templateContextMap);
   }

   protected final void fillTemplateDataMapForReferredUserRewardedTrigger(Map<String, String> templateContextMap) {
   }

   public UserDetails getReferredUser() {
      return this.getReferredUserData();
   }

   public Date getRewardedTime() {
      return this.rewardedTime;
   }

   public long getRewardedProgramID() {
      return this.referredUserRewardProgram != null ? (long)this.referredUserRewardProgram.id : -1L;
   }

   public int getRewardedProgramTriggerType() {
      return this.referredUserRewardProgram != null ? this.referredUserRewardProgram.type.getId() : -1;
   }
}
