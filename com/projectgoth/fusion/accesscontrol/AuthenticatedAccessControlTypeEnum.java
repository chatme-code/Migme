package com.projectgoth.fusion.accesscontrol;

public enum AuthenticatedAccessControlTypeEnum {
   ADD_FRIEND(true, true),
   BE_ADDED_AS_FRIEND(true, true),
   JOIN_GROUP(true, true),
   CREATE_GROUP_CHAT(true, true),
   TRANSFER_CREDIT_OUT(true, true),
   RECEIVE_CREDIT_TRANSFER(true, true),
   BUY_AVATAR(true, true),
   BUY_EMOTICONPACK(true, true),
   BUY_VIRTUALGIFT(true, true),
   BUY_PAIDEMOTE(true, true),
   ENTER_CHATROOM(true, true),
   SEND_GROUP_INVITE(true, true),
   RECEIVE_GROUP_INVITE(true, true),
   LOGIN_AFTER_90DAYS(true, true),
   PARTICIPATE_IN_MARKETING_MECHANICS(true, true),
   REGISTER_AS_MERCHANT(true, true),
   EDIT_PROFILE(true, true),
   RECEIVE_USER_LIKE(true, true),
   UPLOAD_PHOTO(true, true),
   RECEIVE_AVATAR_VOTE(true, true),
   RETURN_VERIFIED_UPON_LOGIN(true, false),
   OTHER_IM_LOGIN(true, false),
   UPLOAD_FILE(true, false),
   SEND_MIG33_EMAIL(true, false),
   MAKE_CREDIT_CARD_PAYMENT(true, false),
   MAKE_BANK_TRANSFER(true, false),
   CREATE_USER_POST_IN_GROUPS(true, false),
   ADD_CONTACT_GROUP(true, false),
   INVITE_FRIEND(true, false),
   CHANGE_ROOM_OWNER_EMAIL(true, false),
   SEND_BINARY_DATA(true, false),
   SEND_PRIVATE_MESSAGE_TO_NON_CONTACT(true, false);

   public boolean defaultMobileVerifiedAllowed;
   public boolean defaultEmailVerifiedAllowed;
   public String defaultMobileVerifiedRateLimit;
   public String defaultEmailVerifiedRateLimit;

   private AuthenticatedAccessControlTypeEnum(boolean defaultMobileVerifiedAllowed, boolean defaultEmailVerifiedAllowed, String defaultMobileVerifiedRateLimit, String defaultEmailVerifiedRateLimit) {
      this.defaultMobileVerifiedAllowed = defaultMobileVerifiedAllowed;
      this.defaultEmailVerifiedAllowed = defaultEmailVerifiedAllowed;
      this.defaultMobileVerifiedRateLimit = defaultMobileVerifiedRateLimit;
      this.defaultEmailVerifiedRateLimit = defaultEmailVerifiedRateLimit;
   }

   private AuthenticatedAccessControlTypeEnum(boolean defaultMobileVerifiedAllowed, boolean defaultEmailVerifiedAllowed) {
      this(defaultMobileVerifiedAllowed, defaultEmailVerifiedAllowed, (String)null, (String)null);
   }

   public AuthenticatedAccessControlData toData() {
      return new AuthenticatedAccessControlData(this.name(), this.defaultMobileVerifiedAllowed, this.defaultEmailVerifiedAllowed, this.defaultMobileVerifiedRateLimit, this.defaultEmailVerifiedRateLimit);
   }
}
