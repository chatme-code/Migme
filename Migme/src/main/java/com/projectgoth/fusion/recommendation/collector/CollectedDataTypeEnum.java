package com.projectgoth.fusion.recommendation.collector;

import com.projectgoth.fusion.common.EnumUtils;
import java.util.HashMap;

public enum CollectedDataTypeEnum implements EnumUtils.IEnumValueGetter<Integer> {
   ADDRESSBOOKCONTACT(1),
   REWARD_PROGRAM_TRIGGER_SUMMARY(2);

   private final int code;

   private CollectedDataTypeEnum(int code) {
      this.code = code;
   }

   public static CollectedDataTypeEnum fromCode(int code) {
      return (CollectedDataTypeEnum)CollectedDataTypeEnum.SingletonHolder.lookupByCode.get(code);
   }

   public int getCode() {
      return this.code;
   }

   public Integer getEnumValue() {
      return this.getCode();
   }

   private static class SingletonHolder {
      public static final HashMap<Integer, CollectedDataTypeEnum> lookupByCode = (HashMap)EnumUtils.buildLookUpMap(new HashMap(), CollectedDataTypeEnum.class);
   }
}
