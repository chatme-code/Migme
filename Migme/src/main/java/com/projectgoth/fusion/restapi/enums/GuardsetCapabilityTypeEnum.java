package com.projectgoth.fusion.restapi.enums;

public enum GuardsetCapabilityTypeEnum {
   GUARD_BY_USER_ID(1),
   GUARD_BY_MIN_CLIENT_VERSION(2);

   private int value;

   private GuardsetCapabilityTypeEnum(int value) {
      this.value = value;
   }

   public int value() {
      return this.value;
   }
}
