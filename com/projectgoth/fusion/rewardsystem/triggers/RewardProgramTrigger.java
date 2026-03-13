package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.Money;
import com.projectgoth.leto.common.event.UserEvent;
import com.projectgoth.leto.common.user.UserDetails;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public class RewardProgramTrigger implements Serializable, UserEvent {
   public static final String CURRENT_VERSION = "1.0";
   public static final String TMPLT_DATA_KEY_TRIGGER_USER_DATA_MOBILEPHONE = "trigger.userData.mobilePhone";
   public static final String TMPLT_DATA_KEY_TRIGGER_USER_DATA_EMAILADDRESS = "trigger.userData.emailAddress";
   public static final String TMPLT_DATA_KEY_TRIGGER_USER_DATA_LANGUAGE = "trigger.userData.language";
   public static final String TMPLT_DATA_KEY_TRIGGER_USER_DATA_FULLBODY_AVATAR = "trigger.userData.fullbodyAvatar";
   public static final String TMPLT_DATA_KEY_TRIGGER_USER_DATA_AVATAR = "trigger.userData.avatar";
   public static final String TMPLT_DATA_KEY_TRIGGER_USER_DATA_DISPLAY_PICTURE = "trigger.userData.displayPicture";
   public static final String TMPLT_DATA_KEY_TRIGGER_USER_DATA_DISPLAY_NAME = "trigger.userData.displayName";
   public static final String TMPLT_DATA_KEY_TRIGGER_USER_DATA_USERNAME = "trigger.userData.username";
   public static final String TMPLT_DATA_KEY_TRIGGER_USER_DATA_ID = "trigger.userData.id";
   public static final String TMPLT_DATA_KEY_TRIGGER_CURRENCY = "trigger.currency";
   public static final String TMPLT_DATA_KEY_TRIGGER_AMOUNT_DELTA = "trigger.amountDelta";
   public static final String TMPLT_DATA_KEY_TRIGGER_QUANTITY_DELTA = "trigger.quantityDelta";
   public static final String DEFAULT_TMPLT_DATA_TRIGGER_USER_DATA_MOBILEPHONE = "N/A";
   public static final String DEFAULT_TMPLT_DATA_TRIGGER_USER_DATA_EMAILADDRESS = "N/A";
   public static final String PROFILE_URL = "profile_url";
   public static final String BASE_URL = "base_url";
   public static final String IMG_BASE_URL = "img_base_url";
   public RewardProgramData.TypeEnum programType;
   public UserData userData;
   public int quantityDelta;
   public double amountDelta;
   public String currency;

   public RewardProgramTrigger(RewardProgramData.TypeEnum programType, UserData userData) {
      this.programType = programType;
      this.userData = userData;
      this.quantityDelta = 0;
      this.amountDelta = 0.0D;
      this.currency = "USD";
   }

   public String toString() {
      return "" + this.programType + ':' + (this.userData == null ? -1 : this.userData.userID) + ':' + (this.userData == null ? -1 : this.userData.countryID) + ':' + this.quantityDelta + ':' + this.amountDelta + ':' + this.currency;
   }

   public final void populateTemplateDataMap(Map<String, String> templateDataMap) {
      templateDataMap.put("trigger.quantityDelta", String.valueOf(this.quantityDelta));
      templateDataMap.put("trigger.amountDelta", String.valueOf(this.amountDelta));
      templateDataMap.put("base_url", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIG33_WEB_BASE_URL));
      templateDataMap.put("img_base_url", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.IMG_WEB_BASE_URL));
      if (this.currency != null) {
         templateDataMap.put("trigger.currency", this.currency);
      } else {
         templateDataMap.put("trigger.currency", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.DEFAULT_CURRENCY));
      }

      if (this.userData != null) {
         if (this.userData.userID != null) {
            templateDataMap.put("trigger.userData.id", this.userData.userID.toString());
         }

         templateDataMap.put("trigger.userData.username", this.userData.username);
         templateDataMap.put("trigger.userData.displayName", this.userData.displayName);
         templateDataMap.put("trigger.userData.displayPicture", this.userData.displayPicture);
         templateDataMap.put("trigger.userData.avatar", this.userData.avatar);
         templateDataMap.put("trigger.userData.fullbodyAvatar", this.userData.fullbodyAvatar);
         templateDataMap.put("trigger.userData.language", this.userData.language);
         String emailAddress = StringUtil.isBlank(this.userData.emailAddress) ? "N/A" : this.userData.emailAddress;
         templateDataMap.put("trigger.userData.emailAddress", emailAddress);
         String mobilePhone = StringUtil.isBlank(this.userData.mobilePhone) ? "N/A" : this.userData.mobilePhone;
         templateDataMap.put("trigger.userData.mobilePhone", mobilePhone);
         templateDataMap.put("profile_url", String.format("%s/u/%s", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIG33_WEB_BASE_URL), this.userData.username));
      }

      this.fillTemplateDataMap(templateDataMap);
   }

   protected void fillTemplateDataMap(Map<String, String> templateContextMap) {
   }

   public String getMetadataVersion() {
      return "1.0";
   }

   public String getClassType() {
      return this.getClass().getSimpleName();
   }

   public UserDetails getSubjectUser() {
      return this.userData;
   }

   public int getEventType() {
      return this.programType.value();
   }

   public Money getValue() {
      return this.amountDelta == 0.0D ? Money.ZERO : new Money(new BigDecimal(this.amountDelta), this.currency);
   }

   public long getQuantity() {
      return (long)this.quantityDelta;
   }
}
