package com.projectgoth.fusion.restapi.enums;

import com.projectgoth.fusion.common.EnumUtils;
import java.util.HashMap;
import java.util.Map;

public enum GuardCapabilityEnum implements EnumUtils.IEnumValueGetter<Integer> {
   MIGBO_ACCESS(10),
   MIGBO_CREATE_POST(11),
   MIGBO_TAG_ACCESS(14),
   OFFLINE_MESSAGING(15),
   MOL_PAYMENT_ALLOWED(16),
   MIMO_PAYMENT_ALLOWED(17),
   PAYPAL_PAYMENT_ALLOWED(18),
   PAYPAL_BLACKLIST(19),
   INVITATION_EMAIL_REFERRAL(20),
   VALHALLA_TEST(21),
   EMOTES_MIN_VERSION(22),
   OFFLINE_MESSAGING_MIN_VERSION(23),
   FB_INVITATION_REFERRAL(24),
   FUSION_PKT_GET_EMOTICON_HI_RES_SUPPORT(25),
   CHATSYNC_USERS(26),
   INTERNAL_INVITATION_REFERRAL(27),
   CHATSYNC_MIN_VERSION(28),
   CHARGEABLE_WITH_CREDIT_TRANSFER_FEE(29),
   SEND_STICKERS_ALLOWED(30),
   RECEIVE_STICKERS_NATIVE_SUPPORT(31),
   PUSH_MODE_ALLOWED(32),
   UPLOAD_ADDRESSBOOK_DATA(33),
   PERSISTENT_GROUP_CHAT_MIN_VERSION(34),
   CREDIT_CARD_HML_WHITELIST(35),
   FUSION_PKT_CONTACT_REQUEST_ACCEPTED_SUPPORT(50);

   private int value;

   private GuardCapabilityEnum(int value) {
      this.value = value;
   }

   public int value() {
      return this.value;
   }

   public static GuardCapabilityEnum fromValue(int value) {
      return (GuardCapabilityEnum)GuardCapabilityEnum.SingletonHolder.LOOKUP_BY_CODE.get(value);
   }

   public Integer getEnumValue() {
      return this.value;
   }

   private static class SingletonHolder {
      public static final Map<Integer, GuardCapabilityEnum> LOOKUP_BY_CODE = EnumUtils.buildLookUpMap(new HashMap(), GuardCapabilityEnum.class);
   }
}
