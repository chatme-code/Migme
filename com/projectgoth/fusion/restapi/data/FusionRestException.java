package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class FusionRestException extends Exception {
   FusionRestException.EntityObject entity = new FusionRestException.EntityObject();
   public static final short INTERNAL_SERVER_ERROR = -1;
   public static final short EXCEPTION = 101;
   public static final short SESSION_ALREADY_EXISTS = 102;

   public FusionRestException(int errno, String message) {
      super(message);
      this.entity.error = new FusionRestException.FusionRestExceptionError(errno, message);
   }

   public FusionRestException(FusionRestException.RestException restException, String message) {
      super(message);
      this.entity.error = new FusionRestException.FusionRestExceptionError(restException.code, message);
   }

   public FusionRestException(FusionRestException.RestException restException) {
      this.entity.error = new FusionRestException.FusionRestExceptionError(restException.code, restException.message);
   }

   public FusionRestException.EntityObject getEntity() {
      return this.entity;
   }

   @XmlAccessorType(XmlAccessType.NONE)
   @XmlRootElement(
      name = "holder"
   )
   public class FusionRestExceptionError {
      @XmlElement(
         required = true
      )
      public int errno;
      @XmlElement(
         nillable = true,
         required = false
      )
      public String message;

      public FusionRestExceptionError(int errno, String message) {
         this.errno = errno;
         this.message = message;
      }
   }

   @XmlAccessorType(XmlAccessType.NONE)
   @XmlRootElement(
      name = "holder"
   )
   public class EntityObject {
      @XmlElement(
         required = true
      )
      public FusionRestException.FusionRestExceptionError error;
   }

   public static enum RestException {
      INTERNAL_SERVER_ERROR(-1, "Internal error. Please try again."),
      ERROR(101, "An unexpected error occurred. Please try again later."),
      MISSING_SESSION_ID(102, "Session id does not exists."),
      SESSION_ALREADY_EXISTS(103, "Session already exists."),
      USER_CREDENTIALS_REQUIRED(104, "Please specify username and password."),
      UNABLE_TO_CREATE_SESSION(105, "Unable to create session. Please try again later."),
      DISABLED_SSO(106, "Login is disabled."),
      CREDENTIAL_ALREADY_EXISTS(107, "Account is already linked to another user"),
      EMAIL_REGISTERED_USER_NOT_VERIFIED(108, "Please activate your account first or go to m.mig.me/resend to resend token"),
      MAX_FAILED_AUTHENTICATION_REACHED(109, "You've attempted too many incorrect logins"),
      INCORRECT_AUTHENTICATION_DETAIL(110, "Incorrect authentication detail"),
      UNKNOWN_SYSTEM_PROPERTY(111, "Unknown system property"),
      MANDATORY_FIELD_MISSING(112, "Mandatory field missing"),
      UNKNOWN_USER_NAME(113, "Unknown user name"),
      INVALID_PAYLOAD_DATA(114, "Invalid payload data"),
      UNKNOWN_USER_ID(115, "Unknown user id"),
      UNAUTHENTICATED_MOBILE_USER(116, "Unauthenticated mobile user"),
      DISABLED_FEATURE(117, "Sorry, we are unable to process your request."),
      INVALID_TOKEN(201, "Invalid token"),
      UNSUPPORTED_PAYMENT_VIEW_TYPE(301, "Unsupported payment view type."),
      INVALID_PAYMENT_VENDOR(302, "Invalid payment vendor."),
      INVALID_PAYMENT_AMOUNT(303, "Invalid payment amount."),
      UNKNOWN_PAYMENT_OPTION(304, "Unknown payment option."),
      THIRD_PARTY_PAYMENT_DISABLED(305, "Payment through third party vendors is disabled."),
      INSUFFICIENT_CREDIT(306, "Insufficient credit."),
      INVALID_REFERRAL_CHANNEL(401, "Invalid channel type for sending invitation"),
      INVALID_REFERRAL_DESTINATION(402, "Invalid destination for sending invitation"),
      INVALID_REFERRAL_TOKEN(403, "Invalid referral token"),
      INVITATION_DISABLED(404, "Inviting feature is currently disabled"),
      INVITATION_DENIED(405, "Sending invitation not allowed"),
      REACH_REFERRAL_RATE_LIMIT(406, "You have reached the number of referrals you can send for today. Please try again tomorrow"),
      INVALID_REFERRAL_ACTIVITY_TYPE(407, "Invalid activity type for sending invitation"),
      INVALID_INVITATION_RESPONSE(408, "Invalid invitation response"),
      INVALID_ACCOUNTENTRY_TYPE(409, "Invalid accountentry type"),
      FORGOT_USERNAME_VIA_EMAIL_RATE_LIMIT(501, "You have already exceeded the maximum number of requests for today. If you have not requested for your username, please email contact@mig.me."),
      FORGOT_PASSWORD_VIA_EMAIL_RATE_LIMIT(502, "You have already exceeded the maximum number of reset passwords via email today. If you have not requested for your password to be reset, please email contact@mig.me."),
      FORGOT_PASSWORD_VIA_SECURITY_QUESTION_RATE_LIMIT(503, "You have already exceeded the maximum number of reset passwords via security question today. If you have not requested for your password to be reset, please email contact@mig.me."),
      FORGOT_PASSWORD_VIA_SMS_RATE_LIMIT(505, "You have already exceeded the maximum number of reset passwords via SMS today. If you have not requested for your password to be reset, please email contact@mig.me."),
      FORGOT_PASSWORD_OVERALL_RATE_LIMIT(506, "You have already exceeded the maximum number of reset passwords today. If you have not requested for your password to be reset, please email contact@mig.me."),
      AUTO_LOGIN_RATE_LIMIT(504, String.format("You may have already activated your account. Please visit %s and login to enjoy the awesome migme experience.", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.LOGIN_URL))),
      RATE_LIMIT(510, "You have already exceeded the rate limit"),
      UNKNOWN_REWARD_PROGRAM_ID(601, "Unknown reward program ID"),
      SERVICE_DISABLED(999, "Service Disabled"),
      DUPLICATE_MOBILE(2000, "Duplicate Mobile");

      private int code;
      private String message;

      private RestException(int code, String message) {
         this.code = code;
         this.message = message;
      }

      public int getCode() {
         return this.code;
      }

      public String getMessage() {
         return this.message;
      }
   }
}
