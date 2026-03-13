package com.projectgoth.fusion.fdl.enums;

import java.util.HashMap;

public enum ChatroomCategoryRefreshType {
   REPLACE((byte)1),
   APPEND((byte)2);

   private byte value;
   private static final HashMap<Byte, ChatroomCategoryRefreshType> LOOKUP = new HashMap();

   private ChatroomCategoryRefreshType(byte value) {
      this.value = value;
   }

   public byte value() {
      return this.value;
   }

   public static ChatroomCategoryRefreshType fromValue(int value) {
      return (ChatroomCategoryRefreshType)LOOKUP.get((byte)value);
   }

   public static ChatroomCategoryRefreshType fromValue(Byte value) {
      return (ChatroomCategoryRefreshType)LOOKUP.get(value);
   }

   static {
      ChatroomCategoryRefreshType[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatroomCategoryRefreshType chatroomCategoryRefreshType = arr$[i$];
         LOOKUP.put(chatroomCategoryRefreshType.value, chatroomCategoryRefreshType);
      }

   }
}
