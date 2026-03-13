package com.projectgoth.fusion.invitation;

import com.projectgoth.fusion.common.EnumUtils;
import java.util.HashMap;

public enum InvitationStatusEnum implements EnumUtils.IEnumValueGetter<Integer> {
   DISABLED(0),
   OPEN(1),
   CLOSED(2),
   EXPIRED(3),
   INVALID(4),
   UNKNOWN(5);

   private Integer typeCode;
   private static HashMap<Integer, InvitationStatusEnum> lookupByCode = new HashMap();

   private InvitationStatusEnum(int typeCode) {
      this.typeCode = typeCode;
   }

   public Integer getEnumValue() {
      return this.getTypeCode();
   }

   public int getTypeCode() {
      return this.typeCode;
   }

   public static InvitationStatusEnum fromTypeCode(int typeCode) {
      return (InvitationStatusEnum)lookupByCode.get(typeCode);
   }

   static {
      EnumUtils.populateLookUpMap(lookupByCode, InvitationStatusEnum.class);
   }
}
