package com.projectgoth.fusion.userevent.domain;

public enum VirtualGoodType {
   EMOTICON_PACK((byte)1),
   WALLPAPER((byte)2),
   RINGTONE((byte)3),
   GAME((byte)4),
   VIDEO((byte)5),
   PREMIUM_EMOTICON_PACK((byte)6);

   private final byte value;

   private VirtualGoodType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static VirtualGoodType fromValue(byte value) {
      VirtualGoodType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         VirtualGoodType e = arr$[i$];
         if (e.value() == value) {
            return e;
         }
      }

      return null;
   }
}
