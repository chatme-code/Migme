package com.projectgoth.fusion.common;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.leto.common.event.post.IThirdPartySiteType;
import com.projectgoth.leto.common.event.post.ThirdPartySiteType;
import com.projectgoth.leto.common.utils.enums.ValueToEnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Enums {
   public static enum ForgotPasswordEnum {
      VIA_EMAIL(1),
      VIA_SECURITY_QUESTION(2),
      VIA_SMS(3);

      private int value;

      private ForgotPasswordEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static Enums.ForgotPasswordEnum fromValue(int value) {
         Enums.ForgotPasswordEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.ForgotPasswordEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }

      public static boolean isForgotPasswordTypeEnabled(Enums.ForgotPasswordEnum type) {
         switch(type) {
         case VIA_SMS:
            return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED_FORGOT_PASSWORD_VIA_SMS);
         case VIA_EMAIL:
            return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED_FORGOT_PASSWORD_VIA_EMAIL);
         case VIA_SECURITY_QUESTION:
            return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED_FORGOT_PASSWORD_VIA_SECURITY_QUESTION);
         default:
            return false;
         }
      }

      public static String getRatelimitPattern(Enums.ForgotPasswordEnum type) {
         switch(type) {
         case VIA_SMS:
            return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ATTEMPT_RATE_LIMIT_VIA_SMS);
         case VIA_EMAIL:
            return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ATTEMPT_RATE_LIMIT_VIA_EMAIL);
         case VIA_SECURITY_QUESTION:
            return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ATTEMPT_RATE_LIMIT_VIA_SQ);
         default:
            return null;
         }
      }

      public static FusionRestException.RestException getRatelimitRestException(Enums.ForgotPasswordEnum type) {
         switch(type) {
         case VIA_SMS:
            return FusionRestException.RestException.FORGOT_PASSWORD_VIA_SMS_RATE_LIMIT;
         case VIA_EMAIL:
            return FusionRestException.RestException.FORGOT_PASSWORD_VIA_EMAIL_RATE_LIMIT;
         case VIA_SECURITY_QUESTION:
            return FusionRestException.RestException.FORGOT_PASSWORD_VIA_SECURITY_QUESTION_RATE_LIMIT;
         default:
            return FusionRestException.RestException.FORGOT_PASSWORD_OVERALL_RATE_LIMIT;
         }
      }
   }

   public static enum Mig33UserActionMisLogEnum {
      CHANGE_EMAIL_ADDRESS(1, "MIG33_USER", "UPDATE", "Updated email address from %s to %s."),
      CHANGE_PASSWORD(2, "MIG33_USER", "UPDATE", "Updated password."),
      ADD_EMAIL_ADDRESS(3, "MIG33_USER", "ADD", "Added email address %s"),
      DELETE_EMAIL_ADDRESS(4, "MIG33_USER", "DELETE", "Delete email address %s"),
      VERIFY_EMAIL_ADDRESS(5, "MIG33_USER", "VERIFY", "Verify email address %s"),
      SUSPEND_USER(6, "MIG33_USER", "SUSPEND_USER", "Suspended user,duration(hours) %s"),
      FORGOT_PASSWORD(7, "MIG33_USER", "FORGOT_PASSWORD", "Forgot password via %s");

      private int code;
      private String section;
      private String action;
      private String descriptionPattern;

      private Mig33UserActionMisLogEnum(int code, String section, String action, String descriptionPattern) {
         this.code = code;
         this.section = section;
         this.action = action;
         this.descriptionPattern = descriptionPattern;
      }

      public String getSection() {
         return this.section;
      }

      public String getAction() {
         return this.action;
      }

      public String getDescriptionPattern() {
         return this.descriptionPattern;
      }
   }

   public static enum MessageStatusEventTypeEnum {
      COMPOSING(1),
      RECEIVED(2),
      READ(3);

      public static int MAX_VALUE = READ.value();
      private int value;

      private MessageStatusEventTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static Enums.MessageStatusEventTypeEnum fromValue(int value) {
         Enums.MessageStatusEventTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.MessageStatusEventTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum EmailTypeEnum {
      REFERRAL(1),
      DAILY_DIGEST(2),
      DAILY_DIGEST_FOR_USERS_BEING_FOLLOWED(7),
      REFERRAL_ACK(10);

      private static final Map<Integer, Enums.EmailTypeEnum> lookup = new HashMap();
      int value;

      private EmailTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static Enums.EmailTypeEnum fromValue(int v) {
         return (Enums.EmailTypeEnum)lookup.get(v);
      }

      static {
         Enums.EmailTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.EmailTypeEnum e = arr$[i$];
            lookup.put(e.value, e);
         }

      }
   }

   public static enum EventTypeEnum {
      TEST_EVENT(0, "Dummy Test Event"),
      FRIEND_ADDED(1, "Friend Added"),
      VIRTUAL_GIFT_PURCHASED(2, "VG Purchased"),
      USERDATA_UPDATED(3, "User Data Updated"),
      GAME_EVENT(4, "Game Event"),
      THIRD_PARTY_SITE_CREDENTIAL_UPDATED(5, "Third Party Site Credential Updated"),
      STATUS_UPDATE_EVENT(6, "Status Update Event");

      public int value;
      public String description;
      private static Set<Enums.EventTypeEnum> ALL_TYPES;

      private EventTypeEnum(int value, String description) {
         this.value = value;
         this.description = description;
      }

      public static Enums.EventTypeEnum fromValue(int value) {
         Enums.EventTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.EventTypeEnum e = arr$[i$];
            if (e.value == value) {
               return e;
            }
         }

         return null;
      }

      public static Set<Enums.EventTypeEnum> getAllTypes() {
         if (ALL_TYPES == null) {
            ALL_TYPES = new HashSet();
            ALL_TYPES.add(TEST_EVENT);
            ALL_TYPES.add(FRIEND_ADDED);
            ALL_TYPES.add(VIRTUAL_GIFT_PURCHASED);
            ALL_TYPES.add(USERDATA_UPDATED);
            ALL_TYPES.add(GAME_EVENT);
            ALL_TYPES.add(THIRD_PARTY_SITE_CREDENTIAL_UPDATED);
            ALL_TYPES.add(STATUS_UPDATE_EVENT);
         }

         return ALL_TYPES;
      }

      public static boolean isValid(int v) {
         return fromValue(v) != null;
      }

      public static String getDescription(int v) {
         return toString(v);
      }

      public static String toString(int v) {
         return isValid(v) ? fromValue(v).description : "Invalid Event Type [" + v + "]";
      }
   }

   public static enum MerchantTagStatEnum {
      FAILED(0, "failed"),
      CREATED(1, "created"),
      TRANSFERED(2, "transfered"),
      EXPIRED(3, "expired");

      private int code;
      private String description;

      private MerchantTagStatEnum(int code, String description) {
         this.code = code;
         this.description = description;
      }

      public static Enums.MerchantTagStatEnum fromCode(int code) {
         Enums.MerchantTagStatEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.MerchantTagStatEnum value = arr$[i$];
            if (value.code == code) {
               return value;
            }
         }

         return null;
      }

      public static String getDescription(int code) {
         return fromCode(code) != null ? fromCode(code).description : "";
      }

      public static boolean isValid(int code) {
         return fromCode(code) != null;
      }

      public static String toString(int code) {
         return fromCode(code) != null ? fromCode(code).description : "";
      }
   }

   public static enum GroupUnbanReasonEnum {
      chance1(1, "giving user his first chance"),
      chance2(2, "giving user his last chance");

      private int code;
      private String description;

      private GroupUnbanReasonEnum(int code, String description) {
         this.code = code;
         this.description = description;
      }

      public static String stringifyValues() {
         StringBuffer str = new StringBuffer();
         boolean first = true;
         str.append("[ ");
         Enums.GroupUnbanReasonEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.GroupUnbanReasonEnum value = arr$[i$];
            if (!first) {
               str.append(", ");
            } else {
               first = false;
            }

            str.append(value.code);
            str.append(": ");
            str.append(value.description);
         }

         str.append(" ]");
         return str.toString();
      }

      public static Enums.GroupUnbanReasonEnum fromCode(int code) {
         Enums.GroupUnbanReasonEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.GroupUnbanReasonEnum value = arr$[i$];
            if (value.code == code) {
               return value;
            }
         }

         return null;
      }

      public static String getDescription(int code) {
         return fromCode(code) != null ? fromCode(code).description : "";
      }

      public static boolean isValid(int code) {
         return fromCode(code) != null;
      }
   }

   public static enum GroupBanReasonEnum {
      SPAM(1, "spamming in the chatroom"),
      FLOOD(2, "flooding in the chatroom"),
      ABUSE(3, "abusing"),
      HACK(4, "hacking"),
      IMPOSTER(5, "imposter");

      private int code;
      private String description;

      private GroupBanReasonEnum(int code, String description) {
         this.code = code;
         this.description = description;
      }

      public static String stringifyValues() {
         StringBuffer str = new StringBuffer();
         boolean first = true;
         str.append("[ ");
         Enums.GroupBanReasonEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.GroupBanReasonEnum value = arr$[i$];
            if (!first) {
               str.append(", ");
            } else {
               first = false;
            }

            str.append(value.code);
            str.append(": ");
            str.append(value.description);
         }

         str.append(" ]");
         return str.toString();
      }

      public static Enums.GroupBanReasonEnum fromCode(int code) {
         Enums.GroupBanReasonEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.GroupBanReasonEnum value = arr$[i$];
            if (value.code == code) {
               return value;
            }
         }

         return null;
      }

      public static String getDescription(int code) {
         return fromCode(code) != null ? fromCode(code).description : "";
      }

      public static boolean isValid(int code) {
         return fromCode(code) != null;
      }
   }

   public static enum NotificationTypeEnum {
      TOTAL_COUNT(0),
      FRIEND_INVITE(1),
      GROUP_INVITE(2),
      VIRTUALGIFT_ALERT(3),
      GAME_INVITE(4),
      NEW_BADGE_ALERT(5),
      NEW_FOLLOWER_ALERT(6),
      FOLLOWING_REQUEST(7),
      PRIVATE_MESSAGE(8),
      GROUPCHAT_MESSAGE(9),
      MIGLEVEL_INCREASE_ALERT(10),
      REPLY_TO_MIGBO_POST_ALERT(11),
      INCOMING_CREDIT_TRANSFER_ALERT(12),
      MERCHANT_STATUS_CHANGE_ALERT(13),
      REPLY_TO_MIGBO_WATCHED_POST_ALERT(14),
      REPLY_TO_MIGBO_PARTICIPATED_POST_ALERT(15),
      MORE_OFFLINE_MESSAGES_AT_MIGBO(16),
      MUTUAL_FOLLOWING_ALERT(17),
      SYS_ALERT(18),
      GAME_HELP(19),
      MENTIONED_IN_MIGBO_POST_ALERT(21);

      private int type;
      public static final EnumSet<Enums.NotificationTypeEnum> COLLAPSE_SET = EnumSet.of(GAME_INVITE);
      public static final EnumSet<Enums.NotificationTypeEnum> ACCUMULATED_SET = EnumSet.of(NEW_FOLLOWER_ALERT, VIRTUALGIFT_ALERT);
      public static final EnumSet<Enums.NotificationTypeEnum> MIGBO_SET = EnumSet.of(NEW_BADGE_ALERT, MUTUAL_FOLLOWING_ALERT, NEW_FOLLOWER_ALERT, FOLLOWING_REQUEST, FRIEND_INVITE, GROUP_INVITE, VIRTUALGIFT_ALERT, GAME_INVITE, MIGLEVEL_INCREASE_ALERT, REPLY_TO_MIGBO_POST_ALERT, INCOMING_CREDIT_TRANSFER_ALERT, MERCHANT_STATUS_CHANGE_ALERT, REPLY_TO_MIGBO_WATCHED_POST_ALERT, REPLY_TO_MIGBO_PARTICIPATED_POST_ALERT, SYS_ALERT, MENTIONED_IN_MIGBO_POST_ALERT);
      public static final EnumSet<Enums.NotificationTypeEnum> PERSISTENT_SET = EnumSet.of(FOLLOWING_REQUEST, FRIEND_INVITE, GROUP_INVITE, GAME_INVITE);
      public static final EnumSet<Enums.NotificationTypeEnum> MIGCORE_SET = EnumSet.of(FRIEND_INVITE, GROUP_INVITE, VIRTUALGIFT_ALERT, GAME_INVITE);
      public static final EnumSet<Enums.NotificationTypeEnum> MIGBO_NON_PERSISTENT_SET = getMigboNonPersistentSet();

      private NotificationTypeEnum(int type) {
         this.type = type;
      }

      public int getType() {
         return this.type;
      }

      private static EnumSet<Enums.NotificationTypeEnum> getMigboNonPersistentSet() {
         EnumSet<Enums.NotificationTypeEnum> nonPersistentSet = EnumSet.copyOf(MIGBO_SET);
         nonPersistentSet.removeAll(PERSISTENT_SET);
         return nonPersistentSet;
      }

      public static boolean isForCollapse(int type) {
         Enums.NotificationTypeEnum e = fromType(type);
         return COLLAPSE_SET.contains(e);
      }

      public static boolean isForMigBo(int type) {
         Enums.NotificationTypeEnum e = fromType(type);
         return MIGBO_SET.contains(e);
      }

      public static boolean isForPersistent(int type) {
         Enums.NotificationTypeEnum e = fromType(type);
         return PERSISTENT_SET.contains(e);
      }

      public static boolean isForMigCore(int type) {
         Enums.NotificationTypeEnum e = fromType(type);
         return MIGCORE_SET.contains(e);
      }

      public static boolean isValid(int type) {
         return fromType(type) != null;
      }

      public static Enums.NotificationTypeEnum fromType(int type) {
         Enums.NotificationTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.NotificationTypeEnum e = arr$[i$];
            if (e.type == type) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum IMEnum {
      FUSION(ImType.FUSION, MessageType.FUSION, "migme", true),
      MSN(ImType.MSN, MessageType.MSN, "MSN", true),
      AIM(ImType.AIM, MessageType.AIM, "AIM", false),
      YAHOO(ImType.YAHOO, MessageType.YAHOO, "Yahoo!", true),
      GTALK(ImType.GTALK, MessageType.GTALK, "GTalk", false),
      FACEBOOK(ImType.FACEBOOK, MessageType.FACEBOOK, "Facebook", false);

      private ImType imType;
      private MessageType messageType;
      private String name;
      private boolean supportsGroupChat;

      private IMEnum(ImType imType, MessageType messageType, String name, boolean supportsGroupChat) {
         this.imType = imType;
         this.messageType = messageType;
         this.name = name;
         this.supportsGroupChat = supportsGroupChat;
      }

      public ImType getImType() {
         return this.imType;
      }

      public MessageType getMessageType() {
         return this.messageType;
      }

      public String getName() {
         return this.name;
      }

      public boolean supportsGroupChat() {
         return this.supportsGroupChat;
      }
   }

   public static enum PaymentEnum {
      ALL(0),
      CREDIT_CARD(1),
      TELEGRAPHIC_TRANSFER(2),
      BANK_TRANSFER(3),
      WESTERN_UNION(4),
      VOUCHER(5);

      private int value;

      private PaymentEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static Enums.PaymentEnum fromValue(int value) {
         Enums.PaymentEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.PaymentEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum UserProfileKeywordEnum {
      SCHOOLS(1),
      HOBBIES(2),
      LIKES(3),
      DISLIKES(4),
      JOBS(5);

      private int value;

      private UserProfileKeywordEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static Enums.UserProfileKeywordEnum fromValue(int value) {
         Enums.UserProfileKeywordEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.UserProfileKeywordEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum ThirdPartyEnum implements IThirdPartySiteType<Enums.ThirdPartyEnum> {
      FACEBOOK(ThirdPartySiteType.FACEBOOK),
      TWITTER(ThirdPartySiteType.TWITTER);

      private ThirdPartySiteType thirdPartySites;

      private ThirdPartyEnum(ThirdPartySiteType value) {
         this.thirdPartySites = value;
      }

      public int value() {
         return this.getEnumValue();
      }

      public static Enums.ThirdPartyEnum fromValue(int value) {
         return (Enums.ThirdPartyEnum)Enums.ThirdPartyEnum.ValueToEnumMapInstances.INSTANCE.toEnum(value);
      }

      public Integer getEnumValue() {
         return this.thirdPartySites.getEnumValue();
      }

      private static final class ValueToEnumMapInstances {
         private static final ValueToEnumMap<Integer, Enums.ThirdPartyEnum> INSTANCE = new ValueToEnumMap(Enums.ThirdPartyEnum.class);
      }
   }

   /** @deprecated */
   @Deprecated
   public static enum DeviceModeEnum {
      AWAKE(0),
      SLEEP(1);

      private int value;

      private DeviceModeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static Enums.DeviceModeEnum fromValue(int value) {
         Enums.DeviceModeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.DeviceModeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum ConnectionEnum {
      TCP(1),
      HTTP(2);

      private int value;

      private ConnectionEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static Enums.ConnectionEnum fromValue(int value) {
         Enums.ConnectionEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.ConnectionEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
