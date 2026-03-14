package com.projectgoth.fusion.userevent.domain;

public enum UserEventType {
   SHORT_TEXT_STATUS((byte)1),
   PHOTO_UPLOAD_WITH_TITLE((byte)2),
   PHOTO_UPLOAD_WITHOUT_TITLE((byte)3),
   CREATE_PUBLIC_CHATROOM((byte)4),
   ADDING_FRIEND((byte)5),
   UPDATING_PROFILE((byte)6),
   PURCHASED_GOODS((byte)7),
   VIRTUAL_GIFT((byte)8),
   GROUP_DONATION((byte)9),
   GROUP_JOINED((byte)10),
   GROUP_ANNOUNCEMENT((byte)11),
   GROUP_USER_POST((byte)12),
   USER_WALL_POST((byte)13),
   GENERIC_APP_EVENT((byte)14),
   GIFT_SHOWER_EVENT((byte)15);

   private final byte value;

   private UserEventType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static UserEventType fromValue(byte value) {
      UserEventType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         UserEventType e = arr$[i$];
         if (e.value() == value) {
            return e;
         }
      }

      return null;
   }
}
