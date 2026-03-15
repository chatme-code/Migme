package com.projectgoth.fusion.payment.creditcard;

import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.payment.PaymentData;
import java.util.HashMap;

public class GlobalCollectCreditCardData {
   private static String version = "2.0";

   public static String getAPIVersion() {
      return version;
   }

   public static enum VendorStatus implements EnumUtils.IEnumValueGetter<Integer> {
      PENDING_AT_MERCHANT(20, PaymentData.StatusEnum.ONHOLD),
      PENDING_AT_GLOBAL_COLLECT(25, PaymentData.StatusEnum.ONHOLD),
      PENDING_AT_GLOBAL_COLLECT_CC_INFO_CAPTURED(30, PaymentData.StatusEnum.ONHOLD),
      PENDING_AT_BANK(50, PaymentData.StatusEnum.ONHOLD),
      PENDING_AT_BANK_SCENARIO_2(650, PaymentData.StatusEnum.ONHOLD),
      PENDING_APPROVAL(600, PaymentData.StatusEnum.PENDING),
      SUCCESSFUL(800, PaymentData.StatusEnum.APPROVED),
      CANCELLED_BY_MIG33(99999, PaymentData.StatusEnum.REJECTED);

      private int value;
      private PaymentData.StatusEnum paymentStatus;
      private static HashMap<Integer, GlobalCollectCreditCardData.VendorStatus> lookupByValue = new HashMap();

      private VendorStatus(int value, PaymentData.StatusEnum paymentStatus) {
         this.value = value;
         this.paymentStatus = paymentStatus;
      }

      public static GlobalCollectCreditCardData.VendorStatus fromValue(int value) {
         return (GlobalCollectCreditCardData.VendorStatus)lookupByValue.get(value);
      }

      public int getValue() {
         return this.value;
      }

      public PaymentData.StatusEnum getStatus() {
         return this.paymentStatus;
      }

      public Integer getEnumValue() {
         return this.value;
      }

      static {
         EnumUtils.populateLookUpMap(lookupByValue, GlobalCollectCreditCardData.VendorStatus.class);
      }
   }

   public static enum VendorErrorCode implements EnumUtils.IEnumValueGetter<Integer> {
      ONLINE_AUTHORISATION_FAILED(430110, PaymentData.StatusEnum.REJECTED),
      ALLOWABLE_PIN_TRIES_EXCEEDED_NA(430165, PaymentData.StatusEnum.REJECTED),
      AMOUNT_TOO_LARGE(430175, PaymentData.StatusEnum.REJECTED),
      AUTHENTICATION_FAILURE_NA(430185, PaymentData.StatusEnum.REJECTED),
      FAILED(430190, PaymentData.StatusEnum.REJECTED),
      BAD_TRACK_2(430195, PaymentData.StatusEnum.REJECTED),
      BANK_NOT_SUPPORTED_ISSUER_SIGNOFF(430200, PaymentData.StatusEnum.REJECTED),
      COMMS_FAIL_1113(430205, PaymentData.StatusEnum.REJECTED),
      COMMS_FAIL_961(430210, PaymentData.StatusEnum.REJECTED),
      CALL_ACQUIRER(430250, PaymentData.StatusEnum.REJECTED),
      CALL_ISSUER(430255, PaymentData.StatusEnum.REJECTED),
      CARD_ACCEPTOR_CALL_ACQUIRERS_SECURITY_DEP(430255, PaymentData.StatusEnum.REJECTED),
      COMPLETED_PARTIALLY_NA(430270, PaymentData.StatusEnum.REJECTED),
      CUSTOMER_DISPUTE(430281, PaymentData.StatusEnum.REJECTED),
      DO_NOT_HONOR(430285, PaymentData.StatusEnum.REJECTED),
      ERROR(430303, PaymentData.StatusEnum.REJECTED),
      EXPIRED_CARD(430306, PaymentData.StatusEnum.REJECTED),
      INVALID_CARD_NUMBER(430330, PaymentData.StatusEnum.REJECTED),
      INVALID_CARD_RANGE(430333, PaymentData.StatusEnum.REJECTED),
      INVALID_ISSUE_NUMBER(430336, PaymentData.StatusEnum.REJECTED),
      INVALID_START_DATE(430342, PaymentData.StatusEnum.REJECTED),
      INVALID_TRANSACTION(430345, PaymentData.StatusEnum.REJECTED),
      ISSUER_OFF_LINE(430351, PaymentData.StatusEnum.REJECTED),
      ISSUER_UNKNOWN(430354, PaymentData.StatusEnum.REJECTED),
      LOST_OR_STOLEN_CARD(430357, PaymentData.StatusEnum.REJECTED),
      LOW_FUNDS(430360, PaymentData.StatusEnum.REJECTED),
      NETWORK_ERROR(430381, PaymentData.StatusEnum.REJECTED),
      NO_ACCOUNT(430384, PaymentData.StatusEnum.REJECTED),
      REQUEST_FUNCTION_NOT_SUPPORTED(430385, PaymentData.StatusEnum.REJECTED),
      NO_UNIVERSAL_ACCOUNT(430386, PaymentData.StatusEnum.REJECTED),
      NO_ACTION_TAKEN(430387, PaymentData.StatusEnum.REJECTED),
      NO_INVESTMENT_ACCOUNT(430388, PaymentData.StatusEnum.REJECTED),
      NO_CHEQUING_ACCOUNT(430389, PaymentData.StatusEnum.REJECTED),
      NO_SAVING_ACCOUNT(430391, PaymentData.StatusEnum.REJECTED),
      NOT_AUTHORISED(430393, PaymentData.StatusEnum.REJECTED),
      NOT_PERMITTED_TO_CARDHOLDER(430396, PaymentData.StatusEnum.REJECTED),
      REFERRED(430409, PaymentData.StatusEnum.REJECTED),
      RESTRICTED(430412, PaymentData.StatusEnum.REJECTED),
      SECURITY_VIOLATION(430415, PaymentData.StatusEnum.REJECTED),
      STOLEN_CARD(430418, PaymentData.StatusEnum.REJECTED),
      SUSPECTED_FRAUD(430421, PaymentData.StatusEnum.REJECTED),
      TRANSACTION_FAILED(430436, PaymentData.StatusEnum.REJECTED),
      TRANSACTION_TYPE_NOT_ALLOWED_FOR_MERCHANT(430439, PaymentData.StatusEnum.REJECTED),
      PARTIAL_APPROVAL(430443, PaymentData.StatusEnum.REJECTED),
      DENIED(430450, PaymentData.StatusEnum.REJECTED),
      DENIED_BLACKLIST(430451, PaymentData.StatusEnum.REJECTED),
      DENIED_VELOCITY_CHECK(430452, PaymentData.StatusEnum.REJECTED),
      DENIED_VELOCITY_CHECK_ASYNC(430453, PaymentData.StatusEnum.REJECTED),
      DENIED_REPEATED_REVERSALS(430454, PaymentData.StatusEnum.REJECTED),
      ORIGINAL_AMOUNT_INCORRECT(430470, PaymentData.StatusEnum.REJECTED),
      OTHERS(430475, PaymentData.StatusEnum.REJECTED),
      PRE_VALID_CARD(430476, PaymentData.StatusEnum.REJECTED),
      OVER_FLOOR_LIMIT(430480, PaymentData.StatusEnum.REJECTED),
      PIN_VALIDATION_NOT_POSSIBLE_NA(430485, PaymentData.StatusEnum.REJECTED),
      PICK_UP_CARD(430490, PaymentData.StatusEnum.REJECTED),
      PICK_UP_CARD_SPECIAL_CONDITION_OTHER_STOLENLOST(430495, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE_AVS_ISSUER_SYSTEM_UNAVAILABLE(430600, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE_AVS_NO_MATCH(430603, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE_AVS_NOT_A_CNP_ORDER_NO_AVS(430606, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE_AVS_NOT_SUPP_OR_REQUESTED(430609, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE_AVS_STREET_AND_ZIP_MATCH(430612, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE_AVS_STREET_AND_ZIP4_MATCH(430615, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE_AVS_STREET_MATCH_ONLY(430618, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE_AVS_ZIP_MATCH_ONLY(430621, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE_AVS_ZIP4_MATCH_ONLY(430624, PaymentData.StatusEnum.REJECTED),
      CVV_HARD_FAILURE(430654, PaymentData.StatusEnum.REJECTED),
      CVV_NOT_PERFORMED_AVS_NO_MATCH(430660, PaymentData.StatusEnum.REJECTED),
      CVV_PERFORMED_AND_INVALID_HARD_FAILURE(430684, PaymentData.StatusEnum.REJECTED),
      CVV_SOFT_FAILURE(430687, PaymentData.StatusEnum.REJECTED),
      INVALID_TERMINAL_ID(430688, PaymentData.StatusEnum.REJECTED),
      INVALID_VOID_DATA(430691, PaymentData.StatusEnum.REJECTED),
      CVV2_DECLINED(430692, PaymentData.StatusEnum.REJECTED),
      NO_FUNDS_TRANSFER(430693, PaymentData.StatusEnum.REJECTED),
      INVALID_CARD_VERIFICATION_VALUE(430694, PaymentData.StatusEnum.REJECTED),
      AUTHORIZATION_CODE_RESPONSE_DATE_INVALID(430695, PaymentData.StatusEnum.REJECTED),
      NEW_CARD_ISSUED(430696, PaymentData.StatusEnum.REJECTED),
      SUSPECTED_FRAUD_CC(430697, PaymentData.StatusEnum.REJECTED),
      INVALID_INSTITUTION(430698, PaymentData.StatusEnum.REJECTED),
      INVALID_EXPIRATION_DATE(430699, PaymentData.StatusEnum.REJECTED),
      INVALID_MOP_UNAUTHORIZED_USER(430701, PaymentData.StatusEnum.REJECTED),
      INVALID_EXPIRATION(430702, PaymentData.StatusEnum.REJECTED),
      ALL_AUTHORISATION_ATTEMPTS_FAILED(430790, PaymentData.StatusEnum.REJECTED),
      REFERRED_CC(430850, PaymentData.StatusEnum.REJECTED),
      DENIAL(430205, PaymentData.StatusEnum.REJECTED),
      INVALID_ISSUE_NUMBER_CC(430336, PaymentData.StatusEnum.REJECTED),
      INVALID_ISSUE_NUMBER_CHARACTER_COUNT(430342, PaymentData.StatusEnum.REJECTED);

      private int value;
      private PaymentData.StatusEnum paymentStatus;
      private static HashMap<Integer, GlobalCollectCreditCardData.VendorErrorCode> lookupByValue = new HashMap();

      private VendorErrorCode(int value, PaymentData.StatusEnum paymentStatus) {
         this.value = value;
         this.paymentStatus = paymentStatus;
      }

      public static GlobalCollectCreditCardData.VendorErrorCode fromValue(int value) {
         return (GlobalCollectCreditCardData.VendorErrorCode)lookupByValue.get(value);
      }

      public int getValue() {
         return this.value;
      }

      public PaymentData.StatusEnum getStatus() {
         return this.paymentStatus;
      }

      public Integer getEnumValue() {
         return this.value;
      }

      static {
         EnumUtils.populateLookUpMap(lookupByValue, GlobalCollectCreditCardData.VendorErrorCode.class);
      }
   }
}
