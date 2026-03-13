package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.AsymmetricCryptUtils;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.CreditCardUtils;
import com.projectgoth.fusion.common.MessageBundle;
import java.io.Serializable;
import java.security.PublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.keyczar.Crypter;

public class CreditCardPaymentData implements Serializable {
   public Integer id;
   public String username;
   public Date dateCreated;
   public CreditCardPaymentData.SourceEnum source;
   public String ipAddress;
   public CreditCardPaymentData.CardTypeEnum cardType;
   public String cardNumber;
   public String encryptedCardNumber;
   public String checkNumber;
   public String cardExpiryDate;
   public String cardHolder;
   public String cardVerificationNumber;
   public Double amount;
   public String currency;
   public Double exchangeRate;
   public String providerTransactionId;
   public String responseCode;
   public Date chargeBackDate;
   public String chargeBackReasonCode;
   public CreditCardPaymentData.StatusEnum status;
   public CreditCardPaymentData.ApproveTypeEnum autoApprove;
   public String firstName;
   public String lastName;
   public Double percentageDiscount;
   public Double discountAmount;
   public boolean allowAutoApprove;
   public CreditCardPaymentData.ErrorEnum error;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CreditCardPaymentData.class));

   public CreditCardPaymentData() {
      this.allowAutoApprove = true;
      this.autoApprove = CreditCardPaymentData.ApproveTypeEnum.MANUAL;
   }

   public CreditCardPaymentData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.username = rs.getString("username");
      this.dateCreated = rs.getTimestamp("dateCreated");
      this.ipAddress = rs.getString("ipAddress");
      this.cardNumber = rs.getString("cardNumber");
      this.cardExpiryDate = rs.getString("cardExpiryDate");
      this.cardHolder = rs.getString("cardHolder");
      this.cardVerificationNumber = rs.getString("cardVerificationNumber");
      this.amount = (Double)rs.getObject("amount");
      this.currency = rs.getString("currency");
      this.exchangeRate = (Double)rs.getObject("exchangeRate");
      this.providerTransactionId = rs.getString("providerTransactionId");
      this.responseCode = rs.getString("responseCode");
      this.chargeBackDate = rs.getTimestamp("chargeBackDate");
      this.chargeBackReasonCode = rs.getString("chargeBackReasonCode");
      Integer intValue = (Integer)rs.getObject("autoApprove");
      if (intValue != null) {
         this.autoApprove = CreditCardPaymentData.ApproveTypeEnum.fromValue(intValue);
      }

      if (this.autoApprove != CreditCardPaymentData.ApproveTypeEnum.AUTO) {
         this.allowAutoApprove = false;
      }

      intValue = (Integer)rs.getObject("source");
      if (intValue != null) {
         this.source = CreditCardPaymentData.SourceEnum.fromValue(intValue);
      }

      intValue = (Integer)rs.getObject("cardType");
      if (intValue != null) {
         this.cardType = CreditCardPaymentData.CardTypeEnum.fromValue(intValue);
      }

      intValue = (Integer)rs.getObject("status");
      if (intValue != null) {
         this.status = CreditCardPaymentData.StatusEnum.fromValue(intValue);
      }

      String details = rs.getString("details");

      try {
         this.deserializeDetails(details);
      } catch (JSONException var5) {
         log.error("unable to deserialize creditcard payment details for id " + this.id + " [" + details + "]");
      }

   }

   public Integer getGlobalCollectPaymentProductId() {
      return getGlobalCollectPaymentProductId(this.cardType);
   }

   public static Integer getGlobalCollectPaymentProductId(CreditCardPaymentData.CardTypeEnum cardType) {
      switch(cardType) {
      case VISA:
         return 1;
      case AMEX:
         return 2;
      case MASTERCARD:
         return 3;
      case DINERS_CLUB:
         return 7;
      case JCB:
         return 125;
      case DISCOVER:
         return 128;
      default:
         return null;
      }
   }

   public String getGlobalCollectMerchantId() {
      return this.providerTransactionId != null && this.providerTransactionId.length() >= 10 ? this.providerTransactionId.substring(6, 10) : null;
   }

   public static CreditCardPaymentData protectedPayment(CreditCardPaymentData other, PublicKey publicKey, Crypter crypter) throws Exception {
      CreditCardPaymentData payment = new CreditCardPaymentData();
      payment.id = other.id;
      payment.username = other.username;
      payment.dateCreated = other.dateCreated;
      payment.ipAddress = other.ipAddress;
      payment.encryptedCardNumber = AsymmetricCryptUtils.encryptAndBase64EncodeText(other.cardNumber, publicKey);
      payment.checkNumber = CreditCardUtils.creditCardHash(other.cardNumber.toCharArray());
      char[] ccnArray = other.cardNumber.toCharArray();
      CreditCardUtils.maskCreditCardNumber(ccnArray);
      payment.cardNumber = new String(ccnArray);
      payment.cardHolder = crypter.encrypt(other.cardHolder);
      payment.cardExpiryDate = crypter.encrypt(other.cardExpiryDate);
      payment.amount = other.amount;
      payment.currency = other.currency;
      payment.exchangeRate = other.exchangeRate;
      payment.providerTransactionId = other.providerTransactionId;
      payment.responseCode = other.responseCode;
      payment.chargeBackDate = other.chargeBackDate;
      payment.chargeBackReasonCode = other.chargeBackReasonCode;
      payment.autoApprove = other.autoApprove;
      payment.allowAutoApprove = other.allowAutoApprove;
      payment.source = other.source;
      payment.cardType = other.cardType;
      payment.status = other.status;
      payment.firstName = other.firstName;
      payment.lastName = other.lastName;
      return payment;
   }

   public void scrubSensitiveInfo() {
      this.cardNumber = "";
      this.cardHolder = "";
      this.cardExpiryDate = "";
   }

   public void decline(String reason) {
      this.status = CreditCardPaymentData.StatusEnum.DECLINED;
      this.responseCode = reason;
      this.allowAutoApprove = false;
   }

   public void decline(CreditCardPaymentData.ErrorEnum errorResponse) {
      this.status = CreditCardPaymentData.StatusEnum.DECLINED;
      this.responseCode = errorResponse.responseCode;
      this.error = errorResponse;
      this.allowAutoApprove = false;
   }

   public void decline(CreditCardPaymentData.ErrorEnum errorResponse, String args) {
      this.status = CreditCardPaymentData.StatusEnum.DECLINED;
      this.responseCode = String.format(errorResponse.responseCode, args);
      this.error = errorResponse;
      this.allowAutoApprove = false;
   }

   public void deserializeDetails(String details) throws JSONException {
      JSONObject jsonDetails = new JSONObject(details);
      String val = jsonDetails.getString("lastname");
      if (val != null) {
         this.lastName = val;
      }

      val = jsonDetails.getString("firstname");
      if (val != null) {
         this.firstName = val;
      }

   }

   public JSONObject serializeExtraFieldsToJSON() throws JSONException {
      JSONObject jsonObj = new JSONObject();
      jsonObj.put("lastname", this.lastName);
      jsonObj.put("firstname", this.firstName);
      return jsonObj;
   }

   public static enum ApproveTypeEnum {
      MANUAL(0),
      AUTO(1);

      private int value;

      private ApproveTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CreditCardPaymentData.ApproveTypeEnum fromValue(int value) {
         CreditCardPaymentData.ApproveTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CreditCardPaymentData.ApproveTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum StatusEnum {
      AWAITING_APPROVAL(0),
      APPROVED(1),
      DECLINED(2),
      CHARGE_BACK(3);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CreditCardPaymentData.StatusEnum fromValue(int value) {
         CreditCardPaymentData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CreditCardPaymentData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum CardTypeEnum {
      VISA(1),
      MASTERCARD(2),
      BANKCARD(3),
      AMEX(4),
      DISCOVER(5),
      DINERS_CLUB(6),
      JCB(7);

      private int value;

      private CardTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CreditCardPaymentData.CardTypeEnum fromValue(int value) {
         CreditCardPaymentData.CardTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CreditCardPaymentData.CardTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum ErrorEnum {
      UNKNOWN_SOURCE(1, "Invalid credit card payment source."),
      INVALID_CARD_TYPE(2, "Invalid credit card type."),
      INVALID_AMOUNT(3, "Amount must be greater than zero."),
      INVALID_CARD_EXPIRY(4, "Invalid card expiry date %s."),
      INVALID_CREDIT_CARD_NUMBER(5, "Invalid credit card number."),
      INVALID_CARD_HOLDER(6, "Invalid card holder."),
      INVALID_AMEX_CVV(7, "Invalid Verification Number. Your card's Verification Number (CVN) is the 4 digits printed on the front of your card just above and to the right of your main credit card number."),
      INVALID_CVV(8, "Invalid Verification Number. Your card's Verification Number (CVN) is the last 3 digits printed on the back of your card."),
      UNKNOWN_USER(9, "Unable to find user data."),
      UNAUTHENTICATED_USER(10, "You must authenticate your account before making a credit card payment."),
      INVALID_CURRENCY(11, "Invalid currency %s."),
      CC_PAYMENT_UNAVAILABLE(12, "Credit card payment unavailable."),
      AMEX_UNSUPPORTED(13, "American Express is not supported. Please choose another type of credit card."),
      DIFFERENT_CC_USED(14, "Please enter a credit card number previously used with migme, or choose a lower amount."),
      USER_DAILY_APPROVED_TRANSACTIONS_LIMIT_EXCEEDED(15, "You have reached the maximum number of credit card payments for today. Please try again at a later time."),
      USER_WEEKLY_APPROVED_TRANSACTIONS_LIMIT_EXCEEDED(16, "You have reached the maximum number of credit card payments for the past seven days. Please try again at a later time."),
      USER_MONTHLY_APPROVED_TRANSACTIONS_LIMIT_EXCEEDED(17, "You have reached the maximum number of credit card payments for the past 30 days. Please try again at a later time."),
      USER_DAILY_RECHARGE_AMOUNT_EXCEEDED(18, "User exceeded 24 hour recharge limit of $%s"),
      USER_DAILY_DECLINED_TRANSACTIONS_LIMIT_EXCEEDED(24, "We regret we are unable to complete your payment at this time. Your satisfaction is very important to us. Please email merchant@mig.me and we will assist you in completing your payment. Alternatively, you may like to try another payment option available to you."),
      CARD_DAILY_APPROVED_USAGE_EXCEEDED(19, "Card reached 24 hour transaction limit."),
      CARD_48_HRS_APPROVED_USAGE_EXCEEDED(20, "Card already used by %s other user(s) in the last 48 hours"),
      CARD_WEEKLY_APPROVED_USAGE_THRESHOLD_EXCEEDED(21, "Card reached 7 day transaction limit"),
      CARD_MONTHLY_APPROVED_USAGE_THRESHOLD_EXCEEDED(21, "Card reached 30 day transaction limit"),
      WEEKLY_NUMBER_OF_CARD_USER_THRESHOLD_EXCEEDED(22, "Card already used by %s other user(s) in the last 7 days"),
      MONTHLY_NUMBER_OF_CARD_USER_THRESHOLD_EXCEEDED(23, "Card already used by %s other user(s) in the last 30 days."),
      CARD_DETAILS_DAILY_DECLINED_TRANSACTIONS_LIMIT_EXCEEDED(24, "Card details already declined %s time(s) in the last 24 hours"),
      CARD_48_HRS_DECLINED_TRANSACTIONS_LIMIT_EXCEEDED(25, "Card details already declined %s time(s) in the last 48 hours"),
      HAS_PENDING_CC_PAYMENT(26, "There is another credit card payment currently waiting for approval"),
      DECLINED(27, "Sorry. The requested credit card transaction was declined. Please contact your bank or try again later. You may also email merchant@mig.me for help."),
      UNABLE_TO_GET_CC_DETAIL(28, "Unable to get credit card details."),
      DEFAULT(100, "An error occurred in your request. Please email merchant@mig.me for help.");

      private static final String MESSAGES_BUNDLE_NAME = "resource.Credit_Card_Payment_Error_Messages";
      private static final String DEFAULT_ERROR_MESSAGE = "An error occurred in your request. Please email merchant@mig.me for help.";
      private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CreditCardPaymentData.class));
      private int value;
      private String responseCode;

      private ErrorEnum(int value, String responseCode) {
         this.value = value;
         this.responseCode = responseCode;
      }

      public int value() {
         return this.value;
      }

      public String message(Locale locale) throws Exception {
         try {
            return MessageBundle.getMessage("resource.Credit_Card_Payment_Error_Messages", locale, this.name());
         } catch (Exception var3) {
            log.error("message error: " + var3.getMessage());
            return "An error occurred in your request. Please email merchant@mig.me for help.";
         }
      }

      public String message(Locale locale, Object... parameters) throws Exception {
         try {
            return MessageBundle.getMessage("resource.Credit_Card_Payment_Error_Messages", locale, this.name(), parameters);
         } catch (Exception var4) {
            log.error("message error: " + var4.getMessage());
            return "An error occurred in your request. Please email merchant@mig.me for help.";
         }
      }

      public String responseCode() {
         return this.responseCode;
      }

      public static CreditCardPaymentData.ErrorEnum fromValue(int value) {
         CreditCardPaymentData.ErrorEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CreditCardPaymentData.ErrorEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum SourceEnum {
      MIDLET(1),
      WEB(2),
      WAP(3),
      TOUCH(4);

      private int value;

      private SourceEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static CreditCardPaymentData.SourceEnum fromValue(int value) {
         CreditCardPaymentData.SourceEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CreditCardPaymentData.SourceEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
