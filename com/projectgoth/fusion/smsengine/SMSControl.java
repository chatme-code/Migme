package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.SystemSMSData;
import org.apache.log4j.Logger;

public class SMSControl {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SMSControl.class));

   public static boolean isSendEnabledForSubtype(int subtype) {
      SystemSMSData.SubTypeEnum cSubtype = SystemSMSData.SubTypeEnum.fromValue(subtype);
      return isSendEnabledForSubtype(cSubtype, (String)null);
   }

   public static boolean isSendEnabledForSubtype(int subtype, String recipient) {
      SystemSMSData.SubTypeEnum cSubtype = SystemSMSData.SubTypeEnum.fromValue(subtype);
      return isSendEnabledForSubtype(cSubtype, recipient);
   }

   public static boolean isSendEnabledForSubtype(SystemSMSData.SubTypeEnum subtype) {
      return isSendEnabledForSubtype(subtype, (String)null);
   }

   public static boolean isSendEnabledForSubtype(SystemSMSData.SubTypeEnum subtype, String recipient) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SmsSettings.SMS_ENGINE_ENABLED)) {
         logNotSendingMsg(subtype, recipient, SystemPropertyEntities.SmsSettings.SMS_ENGINE_ENABLED);
         return false;
      } else {
         SystemPropertyEntities.SmsSettings setting = smsSubtypeToSetting(subtype);
         if (setting == null) {
            log.error("Unknown SystemSMSData.SubTypeEnum=" + subtype);
            return false;
         } else if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)setting)) {
            return true;
         } else {
            logNotSendingMsg(subtype, recipient, setting);
            return false;
         }
      }
   }

   private static void logNotSendingMsg(SystemSMSData.SubTypeEnum subtype, String recipient, SystemPropertyEntities.SmsSettings killswitch) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SmsSettings.LOG_REFUSED_TO_SEND) && log.isInfoEnabled()) {
         log.info("Not sending " + subtype.name() + " sms " + (recipient != null ? " to " + recipient : "") + " : sending disabled by kill switch " + killswitch.getName());
      }

   }

   public static SystemPropertyEntities.SmsSettings smsSubtypeToSetting(SystemSMSData.SubTypeEnum subtype) {
      switch(subtype) {
      case ACTIVATION_CODE:
         return SystemPropertyEntities.SmsSettings.SEND_ACTIVATION_CODE_ENABLED;
      case FORGOT_PASSWORD:
         return SystemPropertyEntities.SmsSettings.SEND_FORGOT_PASSWORD_ENABLED;
      case USER_REFERRAL:
         return SystemPropertyEntities.SmsSettings.SEND_USER_REFERRAL_ENABLED;
      case USER_REFERRAL_ACTIVATION:
         return SystemPropertyEntities.SmsSettings.SEND_USER_REFERRAL_ACTIVATION_ENABLED;
      case MIG33_WAP_PUSH:
         return SystemPropertyEntities.SmsSettings.SEND_MIG33_WAP_PUSH_ENABLED;
      case MIG33_PREMIUM_SMS:
         return SystemPropertyEntities.SmsSettings.SEND_MIG33_PREMIUM_SMS_ENABLED;
      case TT_NOTIFICATION:
         return SystemPropertyEntities.SmsSettings.SEND_TT_NOTIFICATION_ENABLED;
      case SMS_CALLBACK_HELP:
         return SystemPropertyEntities.SmsSettings.SEND_SMS_CALLBACK_HELP_ENABLED;
      case SMS_CALLBACK_BALANCE:
         return SystemPropertyEntities.SmsSettings.SEND_SMS_CALLBACK_BALANCE_ENABLED;
      case EMAIL_ALERT:
         return SystemPropertyEntities.SmsSettings.SEND_EMAIL_ALERT_ENABLED;
      case BANK_TRANSFER_CONFIRMATION:
         return SystemPropertyEntities.SmsSettings.SEND_BANK_TRANSFER_CONFIRMATION_ENABLED;
      case LOW_BALANCE_ALERT:
         return SystemPropertyEntities.SmsSettings.SEND_LOW_BALANCE_ALERT_ENABLED;
      case MERCHANT_USER_ACTIVATION:
         return SystemPropertyEntities.SmsSettings.SEND_MERCHANT_USER_ACTIVATION_ENABLED;
      case BUZZ:
         return SystemPropertyEntities.SmsSettings.SEND_BUZZ_ENABLED;
      case LOOKOUT:
         return SystemPropertyEntities.SmsSettings.SEND_LOOKOUT_ENABLED;
      case WESTERN_UNION_CONFIRMATION:
         return SystemPropertyEntities.SmsSettings.SEND_WESTERN_UNION_CONFIRMATION_ENABLED;
      case MOBILE_CONTENT_DOWNLOAD:
         return SystemPropertyEntities.SmsSettings.SEND_MOBILE_CONTENT_DOWNLOAD_ENABLED;
      case SMS_VOUCHER_RECHARGE:
         return SystemPropertyEntities.SmsSettings.SEND_SMS_VOUCHER_RECHARGE_ENABLED;
      case VIRTUAL_GIFT_NOTIFICATION:
         return SystemPropertyEntities.SmsSettings.SEND_VIRTUAL_GIFT_NOTIFICATION_ENABLED;
      case GROUP_ANNOUNCEMENT_NOTIFICATION:
         return SystemPropertyEntities.SmsSettings.SEND_GROUP_ANNOUNCEMENT_NOTIFICATION_ENABLED;
      case BANGLALINK_URL_DOWNLOAD:
         return SystemPropertyEntities.SmsSettings.SEND_BANGLALINK_URL_DOWNLOAD_ENABLED;
      case BANGLALINK_VOUCHER:
         return SystemPropertyEntities.SmsSettings.SEND_BANGLALINK_VOUCHER_ENABLED;
      case GROUP_EVENT_NOTIFICATION:
         return SystemPropertyEntities.SmsSettings.SEND_GROUP_EVENT_NOTIFICATION_ENABLED;
      case INDOSAT_URL_DOWNLOAD:
         return SystemPropertyEntities.SmsSettings.SEND_INDOSAT_URL_DOWNLOAD_ENABLED;
      case SUBSCRIPTION_EXPIRY_NOTIFICATION:
         return SystemPropertyEntities.SmsSettings.SEND_SUBSCRIPTION_EXPIRY_NOTIFICATION_ENABLED;
      case MARKETING_REWARD_NOTIFICATION:
         return SystemPropertyEntities.SmsSettings.SEND_MARKETING_REWARD_NOTIFICATION_ENABLED;
      case USER_REFERRAL_VIA_GAMES:
         return SystemPropertyEntities.SmsSettings.SEND_USER_REFERRAL_VIA_GAMES_ENABLED;
      default:
         return null;
      }
   }
}
