package com.projectgoth.fusion.restapi.enums;

public enum MigboAccessMemberTypeEnum {
   WHITELIST(1),
   BLACKLIST(2),
   LEVEL_GATE_THRESHOLD(3),
   MIN_VERSION(4);

   private int value;

   private MigboAccessMemberTypeEnum(int value) {
      this.value = value;
   }

   public int value() {
      return this.value;
   }
}
