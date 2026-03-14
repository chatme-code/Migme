package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserSettingData implements Serializable {
   private static final long serialVersionUID = 1L;
   public String username;
   public UserSettingData.TypeEnum type;
   public Integer value;

   public UserSettingData() {
   }

   public UserSettingData(ResultSet rs) throws SQLException {
      this.username = rs.getString("username");
      this.value = (Integer)rs.getObject("value");
      Integer intVal = (Integer)rs.getObject("type");
      if (intVal != null) {
         this.type = UserSettingData.TypeEnum.fromValue(intVal);
      }

   }

   public static boolean isValidEmailTypeEnum(UserSettingData.TypeEnum type) {
      switch(type) {
      case EMAIL_ALL:
      case EMAIL_MENTION:
      case EMAIL_REPLY_TO_POST:
      case EMAIL_RECEIVE_GIFT:
      case EMAIL_NEW_FOLLOWER:
         return true;
      default:
         return false;
      }
   }

   public static enum MessageEnum {
      DISABLED(0),
      EVERYONE(1),
      FRIENDS_ONLY(2);

      private int value;

      private MessageEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserSettingData.MessageEnum defaultValue() {
         return FRIENDS_ONLY;
      }

      public static UserSettingData.MessageEnum fromValue(int value) {
         UserSettingData.MessageEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserSettingData.MessageEnum e = arr$[i$];
            if (value == e.value()) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum AnonymousCallEnum {
      DISABLED(0),
      ENABLED(1);

      private int value;

      private AnonymousCallEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserSettingData.AnonymousCallEnum defaultValue() {
         return ENABLED;
      }

      public static UserSettingData.AnonymousCallEnum fromValue(int value) {
         UserSettingData.AnonymousCallEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserSettingData.AnonymousCallEnum e = arr$[i$];
            if (value == e.value()) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum EmailSettingEnum {
      DISABLED(0),
      ENABLED(1),
      PEOPLE_IM_FAN_OF(2),
      MY_FRIENDS(3);

      private int value;

      private EmailSettingEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserSettingData.EmailSettingEnum defaultValue() {
         return ENABLED;
      }

      public static UserSettingData.EmailSettingEnum fromValue(int value) {
         UserSettingData.EmailSettingEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserSettingData.EmailSettingEnum e = arr$[i$];
            if (value == e.value()) {
               return e;
            }
         }

         return defaultValue();
      }

      public static UserSettingData.EmailSettingEnum fromName(String name) {
         UserSettingData.EmailSettingEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserSettingData.EmailSettingEnum e = arr$[i$];
            if (e.name().equalsIgnoreCase(name)) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      ANONYMOUS_CALL(1),
      MESSAGE(2),
      SECURITY_QUESTION(3),
      EMAIL_MENTION(4),
      EMAIL_REPLY_TO_POST(5),
      EMAIL_RECEIVE_GIFT(6),
      EMAIL_NEW_FOLLOWER(7),
      EMAIL_ALL(8);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserSettingData.TypeEnum fromValue(int value) {
         UserSettingData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserSettingData.TypeEnum e = arr$[i$];
            if (value == e.value()) {
               return e;
            }
         }

         return null;
      }

      public static UserSettingData.TypeEnum fromName(String name) {
         UserSettingData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserSettingData.TypeEnum e = arr$[i$];
            if (e.name().equalsIgnoreCase(name)) {
               return e;
            }
         }

         return null;
      }
   }
}
