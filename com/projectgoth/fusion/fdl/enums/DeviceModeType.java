package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum DeviceModeType {
   AWAKE(0),
   SLEEP(1);

   private int value;
   private static final HashMap<Integer, DeviceModeType> LOOKUP = new HashMap();

   private DeviceModeType(int value) {
      this.value = value;
   }

   public int value() {
      return this.value;
   }

   public static DeviceModeType fromValue(Integer value) {
      return (DeviceModeType)LOOKUP.get(value);
   }

   static {
      DeviceModeType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         DeviceModeType deviceModeType = arr$[i$];
         LOOKUP.put(deviceModeType.value, deviceModeType);
      }

   }
}
