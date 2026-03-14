package com.projectgoth.fusion.recommendation.collector.addressbook;

import com.projectgoth.fusion.common.EnumUtils;
import java.util.HashMap;

public enum AddressBookContactTypeEnum implements EnumUtils.IEnumValueGetter<Byte> {
   MOBILENUMBER((byte)1),
   EMAILADDRESS((byte)2);

   private final byte code;

   private AddressBookContactTypeEnum(byte code) {
      this.code = code;
   }

   public static AddressBookContactTypeEnum fromCode(byte code) {
      return (AddressBookContactTypeEnum)AddressBookContactTypeEnum.SingletonHolder.lookupByCode.get(code);
   }

   public byte getCode() {
      return this.code;
   }

   public Byte getEnumValue() {
      return this.getCode();
   }

   private static class SingletonHolder {
      public static final HashMap<Byte, AddressBookContactTypeEnum> lookupByCode = (HashMap)EnumUtils.buildLookUpMap(new HashMap(), AddressBookContactTypeEnum.class);
   }
}
