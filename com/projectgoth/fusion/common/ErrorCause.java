package com.projectgoth.fusion.common;

import org.apache.log4j.Level;

public interface ErrorCause {
   String getCode();

   String getDefaultErrorMessage();

   String toString();

   public static enum Mig33VoucherRedemptionErrorReasonType implements ErrorCause {
      USER_NOT_VERIFIED("You must authenticate your account before redeeming a voucher");

      private String defaultErrorMessage;

      private Mig33VoucherRedemptionErrorReasonType(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }

   public static enum RewardErrorReasonType implements ErrorCause {
      FEATURE_DISABLED("Feature disabled");

      private String defaultErrorMessage;

      private RewardErrorReasonType(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }

   public static enum EmailVerificationErrorReasonType implements ErrorCause {
      TOKEN_DOES_NOT_EXIST("Invalid token provided."),
      TOKEN_REFERS_TO_AN_INVALID_VALUE("Invalid token provided."),
      TOKEN_REFERS_TO_UNPARSEABLE_USERID("Invalid token provided."),
      TOKEN_REFERS_TO_MISMATCHED_USERID("Invalid token provided."),
      INTERNAL_ERROR("Internal Error");

      private String defaultErrorMessage;

      private EmailVerificationErrorReasonType(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }

   public static enum DataCollectorErrorReasonType implements ErrorCause {
      BLANK_UPLOAD_TICKET_REF("Blank upload ticket ref.", Level.WARN),
      WRONG_UPLOAD_TICKET_SIGNATURE("Wrong ticket signature.", Level.WARN),
      INVALID_UPLOAD_TICKET_REF("Invalid upload ticket ref.", Level.WARN),
      TICKET_REF_CREATION_FAILURE("Ticket ref creation failure.", Level.ERROR),
      CORRUPTED_TICKET_REF("Corrupted ticket ref.", Level.WARN),
      ILLEGAL_PARAMETER_VALUE("Illegal parameter value.", Level.WARN),
      INVALID_DATA_TYPE("Invalid data type.", Level.WARN),
      UNSUPPORTED_DATA_TYPE("Unsupported data type.", Level.WARN),
      LOGGING_FAILED("Logging failed.", Level.ERROR),
      INTERNAL_ERROR("Internal Error.", Level.ERROR),
      UNHANDLED_EXCEPTION("Unhandled exception.", Level.ERROR),
      INSUFFICIENT_MIGLEVEL("Requires minimum reputation level %s.", Level.WARN),
      INSUFFICIENT_RIGHTS("Insufficient rights.", Level.WARN),
      UPLOAD_DATA_GLOBALLY_DISABLED("Upload data feature disabled.", Level.WARN),
      UPLOAD_DATA_FOR_SPECIFIC_TYPE_DISABLED("Upload data feature disabled for data type [%s].", Level.WARN),
      INVALID_RATE_LIMIT_CONFIG("Invalid rate limit config key:[%s] val:[%s]", Level.ERROR),
      RATE_LIMIT_BREACHED("Rate limit breached config key:[%s] val:[%s]", Level.WARN);

      private String defaultErrorMessage;
      private Level severityLevel;

      private DataCollectorErrorReasonType(String msg, Level level) {
         this.defaultErrorMessage = msg;
         this.severityLevel = level;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }

      public Level getSeverityLevel() {
         return this.severityLevel;
      }
   }

   public static enum ThirdPartyApplicationErrorReasonType implements ErrorCause {
      THIRD_PARTY_ID_OUT_OF_RANGE("Invalid third party id");

      private String defaultErrorMessage;

      private ThirdPartyApplicationErrorReasonType(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }

   public static enum InvitationErrorReasonType implements ErrorCause {
      INVITATION_STATUS_CHANGE_NOT_ALLOWED("Not allowed to change invitation status for invitation id (%s)"),
      LOG_INVITATION_RESPONSE_NOT_ALLOWED("Not allowed to log response for invitation id (%s)"),
      LOG_INVITATION_RESPONSE_FAILED("Failed to log invitation response type (%s) from username (%s) invitation id (%s)"),
      UNSUPPORTED_ACTIVITY_TYPE("Unsupported activity type %s"),
      UNSUPPORTED_CHANNEL_TYPE("Unsupported channel type %s"),
      INVITATION_ID_OUT_OF_RANGE("Invalid invitation id"),
      INVALID_FACEBOOK_REQUEST_ID_OR_FACEBOOKUSERID("Invalid facebook request id or facebook userid"),
      LACK_OF_INFORMATION_FOR_GAME_INVITATION("Lack of information for game invitation, we need gameId:%s and returnURL:%s"),
      LACK_OF_INFORMATION_FOR_SHARE_PROFILE_INVITATION("Lack of information for sharing user profile, we need sharedUserID:%s."),
      UNKNOWN_USER("Unknown user"),
      UNVERIFIED_USER("Unverified user");

      private String defaultErrorMessage;

      private InvitationErrorReasonType(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }

   public static enum PaymentErrorReasonType implements ErrorCause {
      INTERNAL_SYSTEM_ERROR("An error occurred in your request. Please contact merchant@mig.me."),
      PAYMENT_DOES_NOT_EXIST("Payment does not exist."),
      PAYMENT_INTERFACE_NOT_REGISTERED("Payment interface not registered for payment vendor type %s"),
      UNKNOWN_PAYMENT_VENDOR("Unknown payment vendor type %s"),
      UNKNOWN_USER("Unknown user"),
      INCORRECT_CURRENCY("Incorrect currency"),
      INVALID_AMOUNT_PROVIDED("Please provide a valid amount."),
      PURCHASE_AMOUNT_BELOW_MINIMUM("Minimum purchase amount must be at least %s %s"),
      PURCHASE_AMOUNT_ABOVE_MAXIMUM("Purchase amount must not exceed %s %s"),
      TOO_MANY_PENDING_PAYMENTS("You have exceeded the number of maximum number attempts for credit recharge through %s. Please try again later."),
      NO_ACCESS_TO_PAYMENT_VENDOR("User %s is not allowed to recharge credit through %s"),
      MISSING_FIELD("%s not supplied"),
      UNKNOWN_CREDIT_RECHARGE_TYPE("Unknown credit recharge type %s"),
      INVALID_RELOAD_KEY("Invalid reload key '%s'"),
      GENERATED_PAYMENT_ID_TOO_LONG("Generated payment ID is too long"),
      DATABASE_ERROR("%s"),
      RECORD_NOT_FOUND("Record not found for ID/key:%s"),
      ILLEGAL_TRANSACTION("%s"),
      INVALID_VENDOR_DATA("%s"),
      VENDOR_SYSTEM_ERROR("%s"),
      FAILED_TO_RETRIEVE_RESPONSE_FROM_VENDOR("%s"),
      RELOAD_CARD_USED_BY_ANOTHER_USER("Reload card is used"),
      DB_UPDATE_PAYMENT_FAILED("An error occurred in processing the payment. Please try again later."),
      UNSUPPORTED_PAYMENT_METHOD("Payment method is unsupported."),
      ERROR("%s"),
      RATE_LIMIT_EXCEEDED("You have exceeded the maximum %s allowed. Please try again at a later time."),
      RATE_LIMIT_EXCEEDED_WO_MESSAGE("%s"),
      PAYMENT_STILL_PENDING_IN_VENDOR("Your payment in %s has not yet been completed. Please verify first that the payment has been succesful."),
      MISMATCH_USER_INFORMATION("Mismatch user information");

      private String defaultErrorMessage;

      private PaymentErrorReasonType(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }

   public static enum TransferCreditErrorReasonType implements ErrorCause {
      SELF_TRANSFER_CREDIT_DISALLOWED("You can't transfer credit to yourself"),
      TRANSER_AMOUNT_TOO_LOW("Transfer amount is too low"),
      USERNAME_VALIDATION_ERROR("%s"),
      INSUFFICIENT_CREDIT("Insufficient credit"),
      BELOW_MIN_BALANCE("%s"),
      INSUFFICIENT_MIG_LEVEL("%s"),
      BANNED_SENDER("%s");

      private String defaultErrorMessage;

      private TransferCreditErrorReasonType(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }

   public static enum InvalidUserName implements ErrorCause {
      VALUE_NOT_SPECIFIED("The username name is not specified"),
      VALUE_IS_BLANK("The username is blank"),
      CONTAINS_ILLEGAL_PATTERNS("The username must start with a letter, and contain at least %s letters, numbers, periods (.), hyphens (-), or underscores (_)"),
      UNSUPPORTED_STRING_LENGTH("The user name is too long"),
      STRING_LENGTH_BEYOND_CONFIGURED_LIMIT("The username must not contain more than %s characters"),
      DETECTED_BANNED_WORDS("You cannot use the word '%s' as a username"),
      UNKNOWN_USER("Unknown user"),
      INVALID_FORMAT("Username format is invalid:[%s]");

      private String defaultErrorMessage;

      private InvalidUserName(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }

   public static enum EmoteCommandError implements ErrorCause {
      INVALID_USER("Invalid username '%s'"),
      INVALID_CHATROOM("Invalid chatroom name '%s'"),
      INVALID_GROUP("Invalid groupid '%s'"),
      INTERNAL_ERROR("%s"),
      INSTIGATOR_NOT_IN_CHATROOM("%s"),
      TARGET_NOT_IN_CHATROOM("%s"),
      MESSAGE_TOO_LONG("%s"),
      INVALID_CHATROOM_TYPE("%s"),
      INVALID_USER_TYPE("%s"),
      UNSUPPORTED_CLIENT_DEVICE("Unsupported client device"),
      INSUFFICIENT_REPUTATION_LEVEL("Insufficient mig level. Minimum mig level is %s"),
      INSUFFICIENT_RIGHTS("Insufficient rights"),
      INVALID_STICKER_NAME("'%s' is an invalid sticker or you have not purchased it.");

      private String defaultErrorMessage;

      private EmoteCommandError(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }

   public static enum GroupErrorType implements ErrorCause {
      INVALID_USER("Unknown %s"),
      INTERNAL_ERROR("%s"),
      FAIL_LOADING_REPUTATION_DATA("Unable to load reputation data for %"),
      INVALID_GROUP("Unknown %s"),
      INCORRECT_GROUP_CREATOR("%s must be an owner of the group to promote %s to be a moderator"),
      INCORRECT_TARGET_USER_LEVEL("'%s's migLevel must be at least %s to become a group moderator)"),
      MAX_MODERATOR_COUNT_REACHED("%s cannot have more than %s moderators in group %s"),
      MANAGE_RIGHTS_ERROR("%s"),
      SELF_DEMOTE("You can't demote yourself");

      private String defaultErrorMessage;

      private GroupErrorType(String msg) {
         this.defaultErrorMessage = msg;
      }

      public String getDefaultErrorMessage() {
         return this.defaultErrorMessage;
      }

      public String toString() {
         return this.name() + "(" + this.defaultErrorMessage + ")";
      }

      public String getCode() {
         return this.name();
      }
   }
}
