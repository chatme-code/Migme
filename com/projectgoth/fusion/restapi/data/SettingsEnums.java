package com.projectgoth.fusion.restapi.data;

import javax.xml.bind.annotation.XmlElement;

public class SettingsEnums {
   public static enum DisplayPictureChoice {
      AVATAR(1),
      PROFILE_PICTURE(2);

      private int value;

      private DisplayPictureChoice(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SettingsEnums.DisplayPictureChoice fromValue(int value) {
         SettingsEnums.DisplayPictureChoice[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SettingsEnums.DisplayPictureChoice e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum Birthday {
      HIDE(0),
      SHOW_FULL(1),
      SHOW_WITHOUT_YEAR(2);

      private int value;

      private Birthday(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SettingsEnums.Birthday fromValue(int value) {
         SettingsEnums.Birthday[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SettingsEnums.Birthday e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum EveryoneFriendHide {
      HIDE(0),
      EVERYONE(1),
      FRIEND_ONLY(2);

      private int value;

      private EveryoneFriendHide(int value) {
         this.value = value;
      }

      @XmlElement
      public int value() {
         return this.value;
      }

      public static SettingsEnums.EveryoneFriendHide fromValue(int value) {
         SettingsEnums.EveryoneFriendHide[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SettingsEnums.EveryoneFriendHide e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum EveryoneOrFollowerAndFriend {
      EVERYONE(1),
      FRIEND_OR_FOLLOWER(2);

      private int value;

      private EveryoneOrFollowerAndFriend(int value) {
         this.value = value;
      }

      @XmlElement
      public int value() {
         return this.value;
      }

      public static SettingsEnums.EveryoneOrFollowerAndFriend fromValue(int value) {
         SettingsEnums.EveryoneOrFollowerAndFriend[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SettingsEnums.EveryoneOrFollowerAndFriend e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum EveryoneFollowerFriend {
      EVERYONE(1),
      FRIEND_ONLY(2),
      FOLLOWER_ONLY(3);

      private int value;

      private EveryoneFollowerFriend(int value) {
         this.value = value;
      }

      @XmlElement
      public int value() {
         return this.value;
      }

      public static SettingsEnums.EveryoneFollowerFriend fromValue(int value) {
         SettingsEnums.EveryoneFollowerFriend[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SettingsEnums.EveryoneFollowerFriend e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum EveryoneFollowerFriendHide {
      HIDE(0),
      EVERYONE(1),
      FRIEND_ONLY(2),
      FOLLOWER_ONLY(3);

      private int value;

      private EveryoneFollowerFriendHide(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SettingsEnums.EveryoneFollowerFriendHide fromValue(int value) {
         SettingsEnums.EveryoneFollowerFriendHide[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SettingsEnums.EveryoneFollowerFriendHide e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum OnOff {
      ON(1),
      OFF(0);

      private int value;

      private OnOff(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static SettingsEnums.OnOff fromValue(int value) {
         SettingsEnums.OnOff[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SettingsEnums.OnOff e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum ShowHide {
      SHOW(1),
      HIDE(0);

      private int value;

      private ShowHide(int value) {
         this.value = value;
      }

      @XmlElement
      public int value() {
         return this.value;
      }

      public static SettingsEnums.ShowHide fromValue(int value) {
         SettingsEnums.ShowHide[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SettingsEnums.ShowHide e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
