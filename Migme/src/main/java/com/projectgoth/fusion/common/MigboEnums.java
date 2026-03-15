package com.projectgoth.fusion.common;

import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.leto.common.event.post.PostContentType;
import com.projectgoth.leto.common.event.post.PostOriginality;
import com.projectgoth.leto.common.event.post.PostingApplicationType;
import com.projectgoth.leto.common.utils.enums.IEnumValueGetter;
import com.projectgoth.leto.common.utils.enums.ValueToEnumMap;

public class MigboEnums {
   public static enum MigboFollowingEventTypeEnum {
      NEW_FOLLOWING(1),
      REMOVE_FOLLOWING(2),
      MUTUAL_FOLLOWING(3);

      private int type;

      private MigboFollowingEventTypeEnum(int type) {
         this.type = type;
      }

      public int value() {
         return this.type;
      }

      public static boolean isValid(int type) {
         return fromType(type) != null;
      }

      public static MigboEnums.MigboFollowingEventTypeEnum fromType(int type) {
         MigboEnums.MigboFollowingEventTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MigboEnums.MigboFollowingEventTypeEnum e = arr$[i$];
            if (e.type == type) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum MigboPostTypeEnum implements IEnumValueGetter<Integer> {
      TEXT(PostContentType.TEXT),
      LINK(PostContentType.LINK),
      PHOTO(PostContentType.PHOTO),
      VIDEO(PostContentType.VIDEO),
      RSSFEED(PostContentType.RSSFEED),
      ACTIVITY(PostContentType.ACTIVITY),
      GAME_EVENT(PostContentType.GAME_EVENT);

      private final PostContentType postContentType;

      private MigboPostTypeEnum(PostContentType postContentType) {
         this.postContentType = postContentType;
      }

      public int getType() {
         return this.postContentType.getEnumValue();
      }

      public static boolean isValid(int type) {
         return fromValue(type) != null;
      }

      public static MigboEnums.MigboPostTypeEnum fromValue(int type) {
         return (MigboEnums.MigboPostTypeEnum)MigboEnums.MigboPostTypeEnum.ValueToEnumMapInstance.INSTANCE.toEnum(type);
      }

      public PostContentType toPostContentType() {
         return this.postContentType;
      }

      public Integer getEnumValue() {
         return this.postContentType.getEnumValue();
      }

      private static final class ValueToEnumMapInstance {
         public static final ValueToEnumMap<Integer, MigboEnums.MigboPostTypeEnum> INSTANCE = new ValueToEnumMap(MigboEnums.MigboPostTypeEnum.class);
      }
   }

   public static enum MigboPostOriginalityEnum implements IEnumValueGetter<Integer> {
      ORIGINAL(PostOriginality.ORIGINAL),
      REPLY(PostOriginality.REPLY),
      RESHARE(PostOriginality.RESHARE);

      private final PostOriginality postOriginality;

      private MigboPostOriginalityEnum(PostOriginality postOriginality) {
         this.postOriginality = postOriginality;
      }

      public int getType() {
         return this.postOriginality.getEnumValue();
      }

      public static boolean isValid(int type) {
         return fromType(type) != null;
      }

      public static MigboEnums.MigboPostOriginalityEnum fromType(int type) {
         return (MigboEnums.MigboPostOriginalityEnum)MigboEnums.MigboPostOriginalityEnum.ValueToEnumMapInstance.INSTANCE.toEnum(type);
      }

      public PostOriginality toPostOriginality() {
         return this.postOriginality;
      }

      public Integer getEnumValue() {
         return this.postOriginality.getEnumValue();
      }

      private static final class ValueToEnumMapInstance {
         public static final ValueToEnumMap<Integer, MigboEnums.MigboPostOriginalityEnum> INSTANCE = new ValueToEnumMap(MigboEnums.MigboPostOriginalityEnum.class);
      }
   }

   public static enum PostApplicationEnum implements IEnumValueGetter<Integer> {
      WEB(PostingApplicationType.WEB),
      WAP(PostingApplicationType.WAP),
      J2ME(PostingApplicationType.J2ME),
      ANDROID(PostingApplicationType.ANDROID),
      SYSTEM(PostingApplicationType.SYSTEM),
      BLACKBERRY(PostingApplicationType.BLACKBERRY),
      BLAAST(PostingApplicationType.BLAAST),
      MRE(PostingApplicationType.MRE),
      IOS(PostingApplicationType.IOS);

      private final PostingApplicationType postingApplicationType;

      private PostApplicationEnum(PostingApplicationType postingApplicationType) {
         this.postingApplicationType = postingApplicationType;
      }

      public int value() {
         return this.postingApplicationType.getEnumValue();
      }

      public static MigboEnums.PostApplicationEnum fromValue(int v) {
         return (MigboEnums.PostApplicationEnum)MigboEnums.PostApplicationEnum.ValueToEnumMapInstance.INSTANCE.toEnum(v);
      }

      public static MigboEnums.PostApplicationEnum fromDeviceType(ClientType deviceType) {
         if (deviceType == null) {
            return null;
         } else {
            switch(deviceType) {
            case AJAX1:
            case AJAX2:
            case MIGBO:
            case MERCHANT_CENTER:
            case VAS:
               return WEB;
            case ANDROID:
               return ANDROID;
            case WAP:
               return WAP;
            case BLACKBERRY:
               return BLACKBERRY;
            case BLAAST:
               return BLAAST;
            case MRE:
               return MRE;
            case IOS:
               return IOS;
            case MIDP2:
            case MIDP1:
            case WINDOWS_MOBILE:
            case SYMBIAN:
            case DESKTOP:
            case TOOLBAR:
            default:
               return J2ME;
            }
         }
      }

      public static MigboEnums.PostApplicationEnum fromSSOView(SSOEnums.View ssoView) {
         if (ssoView == null) {
            return null;
         } else {
            switch(ssoView) {
            case MIG33_AJAX:
            case MIG33_AJAXV2:
            case MIGBO_WEB:
               return WEB;
            case MIG33_WAP:
            case MIGBO_WAP:
               return WAP;
            case MIG33_TOUCH:
            case MIGBO_TOUCH:
               return ANDROID;
            case MIG33_MIDLET:
            case MIGBO_MIDLET:
            case MIG33_WINDOWS_MOBILE:
            case MIGBO_WINDOWS_MOBILE:
               return J2ME;
            case MIG33_BLACKBERRY:
            case MIGBO_BLACKBERRY:
               return BLACKBERRY;
            case MIG33_BLAAST:
            case MIGBO_BLAAST:
               return BLAAST;
            case MIG33_MRE:
            case MIGBO_MRE:
               return MRE;
            case MIG33_IOS:
            case MIGBO_IOS:
               return IOS;
            case UNKNOWN:
            default:
               return null;
            }
         }
      }

      public PostingApplicationType toPostingApplicationType() {
         return this.postingApplicationType;
      }

      public Integer getEnumValue() {
         return this.postingApplicationType.getEnumValue();
      }

      private static final class ValueToEnumMapInstance {
         public static final ValueToEnumMap<Integer, MigboEnums.PostApplicationEnum> INSTANCE = new ValueToEnumMap(MigboEnums.PostApplicationEnum.class);
      }
   }
}
