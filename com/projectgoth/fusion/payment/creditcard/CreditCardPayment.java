package com.projectgoth.fusion.payment.creditcard;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlParameter;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.SimpleXMLParser;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CreditCardPaymentData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentException;
import com.projectgoth.fusion.payment.PaymentIResponse;
import com.projectgoth.fusion.payment.PaymentInterface;
import com.projectgoth.fusion.payment.PaymentMetaDetails;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class CreditCardPayment implements PaymentInterface {
   private static final String AUTHORIZE_TOKEN_DELIMITER = "||";
   private static final CreditCardPayment INSTANCE = new CreditCardPayment();
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CreditCardPayment.class));

   public static CreditCardPayment getInstance() {
      return INSTANCE;
   }

   public boolean isAccessAllowed(UserData userData) throws PaymentException, Exception {
      return false;
   }

   public Map<String, Object> clientInitiatePayment(JSONObject paymentDetails) throws PaymentException, Exception {
      throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNSUPPORTED_PAYMENT_METHOD, new Object[0]);
   }

   public Map<String, Object> updatePaymentStatus(JSONObject paymentDetails) throws Exception {
      throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNSUPPORTED_PAYMENT_METHOD, new Object[0]);
   }

   public String getCurrencyForUser(UserData user) {
      return null;
   }

   public <T extends PaymentData> PaymentIResponse clientInitiatePayment(T paymentDetails) throws PaymentException, Exception {
      AccountLocal accountEJB = null;
      CreditCardPaymentUserAndCountryInfo userAndCountryInfo = null;

      try {
         accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         userAndCountryInfo = accountEJB.getUserAndCountryInfoForCreditCardPayment((Connection)null, paymentDetails.userId);
         userAndCountryInfo.ipAddress = paymentDetails.accountEntrySource.ipAddress;
      } catch (CreateException var8) {
         throw new PaymentException(var8, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      } catch (FusionEJBException var9) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{var9.getMessage()});
      }

      CreditCardData paymentData = (CreditCardData)paymentDetails;
      paymentData.username = userAndCountryInfo.username;
      paymentData.creditCardPaymentStatus = CreditCardPaymentData.StatusEnum.APPROVED;
      paymentData.allowAutoApprove = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.AUTO_APPROVE_CREDIT_CARD_PAYMENT_ENABLED);
      paymentData.assignCreatedTime(new Timestamp(System.currentTimeMillis()));
      paymentData.assignUpdatedTime((Date)null);
      paymentData.status = paymentData.getInitialStatus();
      paymentData = this.validateCreatePaymentRequest(paymentData, userAndCountryInfo, accountEJB);
      if (paymentData.allowAutoApprove && !this.isPaymentEligibleForAutoApproval(paymentData, userAndCountryInfo, accountEJB)) {
         paymentData.allowAutoApprove = false;
      }

      paymentData = (CreditCardData)accountEJB.createPayment(paymentData);
      paymentData.merchantID = userAndCountryInfo.userType != UserData.TypeEnum.MIG33_MERCHANT && userAndCountryInfo.userType != UserData.TypeEnum.MIG33_TOP_MERCHANT ? SystemProperty.get("CreditCardMerchantIDForUser") : SystemProperty.get("CreditCardMerchantIDForMerchant");
      JSONObject result = this.sendOrderWithPaymentToGlobalCollect(paymentData, userAndCountryInfo);
      paymentData.assignUpdatedTime(new Timestamp(System.currentTimeMillis()));
      List<PaymentMetaDetails> details = new ArrayList();
      details.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.AUTO_APPROVE, String.valueOf(paymentData.allowAutoApprove)));
      details.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.GC_CC_CARD_TYPE, String.valueOf(paymentData.cardType.value())));
      details.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.GC_CC_MERCHANTID, paymentData.merchantID));
      if (result.getBoolean("error")) {
         paymentData.status = PaymentData.StatusEnum.fromValue(result.getInt("responseCode"));
      } else {
         paymentData.status = GlobalCollectCreditCardData.VendorStatus.fromValue(result.getInt("responseCode")).getStatus();
         paymentData.vendorTransactionId = result.getString("reference");
         String reference = generateReferenceString(result.getString("returnMac"), result.getString("reference"));
         details.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.GC_CC_REFERENCE, reference));
         details.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.GC_CC_STATUS, result.getString("responseCode")));
      }

      paymentData.setDetails(details);
      paymentData = (CreditCardData)accountEJB.updatePayment(paymentData, paymentDetails.accountEntrySource);
      this.postValidationForCreatePaymentRequest();
      if (result.getBoolean("error")) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{result.get("message")});
      } else {
         CreditCardIResponse requestResponse = new CreditCardIResponse();
         requestResponse.returnUrl = result.getString("returnUrl");
         requestResponse.returnMac = result.getString("returnMac");
         requestResponse.vendorTransactionId = result.getString("reference");
         return requestResponse;
      }
   }

   public <T extends PaymentData> PaymentIResponse updatePaymentStatus(T paymentDetails) throws PaymentException, Exception {
      return null;
   }

   public PaymentIResponse onPaymentAuthorized(String token, JSONObject params) throws PaymentException, Exception {
      log.info(String.format("onPaymentAuthorized: JSON: %s", params.toString()));
      AccountLocal accountEJB = null;
      CreditCardData paymentData = null;
      boolean paymentAlreadySuccessful = false;

      try {
         accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         String reference = generateReferenceString(params.getString("RETURNMAC"), token);
         PaymentMetaDetails[] paymentMetaDetails = new PaymentMetaDetails[]{new PaymentMetaDetails(PaymentMetaDetails.MetaType.GC_CC_REFERENCE, reference)};
         List<CreditCardData> creditCardPayments = accountEJB.getPaymentsByMetaData(PaymentData.TypeEnum.CREDIT_CARD, paymentMetaDetails);
         if (creditCardPayments.isEmpty()) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Unable to retrieve your payment details. Please contact merchant@mig.me"});
         }

         paymentData = (CreditCardData)creditCardPayments.get(0);
         paymentData.loadMetaDetails();
         log.info(String.format("Request to authorize credit card payment [%s] username:%s, amount: %s %s, reference: %s, status: %s", paymentData.id, paymentData.username, paymentData.currency, paymentData.amount, paymentData.vendorTransactionId, paymentData.status));
         if (paymentData.status == PaymentData.StatusEnum.REJECTED) {
            log.info(String.format("Request to authorize credit card payment that's already REJECTED [%s] :: username:%s, amount: %s %s, reference: %s", paymentData.id, paymentData.username, paymentData.currency, paymentData.amount, paymentData.vendorTransactionId));
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Your payment has been rejected."});
         }

         if (paymentData.status == PaymentData.StatusEnum.APPROVED) {
            log.info(String.format("[APPROVED PAYMENT] Request to authorize credit card payment that's alrady APPROVED [%s] :: username:%s, amount: %s %s, reference: %s", paymentData.id, paymentData.username, paymentData.currency, paymentData.amount, paymentData.vendorTransactionId));
            paymentAlreadySuccessful = true;
         }
      } catch (CreateException var10) {
         throw new PaymentException(var10, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      }

      JSONObject paymentDataJson = new JSONObject();
      if (!paymentAlreadySuccessful) {
         paymentDataJson = this.sendGetOrderStatusQueryToGlobalCollect(paymentData);
         log.info(String.format("onPaymentAuthorized: paymentDataJson: %s", paymentDataJson.toString()));
         paymentData.status = PaymentData.StatusEnum.fromValue(paymentDataJson.getInt("responseCode"));
         if (paymentData.status == PaymentData.StatusEnum.PENDING && paymentData.allowAutoApprove) {
            this.sendPaymentApprovalToGlobalCollect(paymentData);
            paymentDataJson = this.sendGetOrderStatusQueryToGlobalCollect(paymentData);
            paymentData.status = PaymentData.StatusEnum.fromValue(paymentDataJson.getInt("responseCode"));
            log.info(String.format("onPaymentAuthorized: autoApprove: paymentDataJson: %s", paymentDataJson.toString()));
         }

         if (params.has("accountEntrySource")) {
            try {
               paymentData.accountEntrySource = new AccountEntrySourceData(params.getJSONObject("accountEntrySource"));
            } catch (Exception var9) {
               paymentData.accountEntrySource = new AccountEntrySourceData(CreditCardPayment.class);
            }
         }

         paymentData.assignUpdatedTime(new Timestamp(System.currentTimeMillis()));
         accountEJB.updatePayment(paymentData, paymentData.accountEntrySource);
      }

      CreditCardIResponse requestResponse = new CreditCardIResponse();
      requestResponse.result = paymentData.status.name();
      requestResponse.amount = String.valueOf(paymentData.amount);
      requestResponse.currency = paymentData.currency;
      if (paymentDataJson.has("message")) {
         requestResponse.message = paymentDataJson.getString("message");
      }

      return requestResponse;
   }

   private CreditCardData validateCreatePaymentRequest(CreditCardData paymentData, CreditCardPaymentUserAndCountryInfo userAndCountryInfo, AccountLocal accountEJB) throws PaymentException {
      if (paymentData.amount <= 0.0D) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_AMOUNT_PROVIDED, new Object[0]);
      } else if (userAndCountryInfo.allowCreditCardPayment != CountryData.AllowCreditCardEnum.ALLOW && userAndCountryInfo.allowCreditCardPayment != CountryData.AllowCreditCardEnum.ALLOW_IF_BIN_CHECK) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNSUPPORTED_PAYMENT_METHOD, new Object[0]);
      } else if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.MAKE_CREDIT_CARD_PAYMENT, new AuthenticatedAccessControlParameter(paymentData.username, userAndCountryInfo.mobileVerified, userAndCountryInfo.emailVerified))) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"You may have improperly accessed this page. Please contact merchant@mig.me"});
      } else if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_CREDITCARD.HML_ENABLED)) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNSUPPORTED_PAYMENT_METHOD, new Object[0]);
      } else {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_CREDITCARD.HML_GUARDSET_ENABLED)) {
            try {
               if (!accountEJB.isPaymentAllowedToUser(userAndCountryInfo.userID, GuardCapabilityEnum.CREDIT_CARD_HML_WHITELIST.value())) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNSUPPORTED_PAYMENT_METHOD, new Object[0]);
               }
            } catch (FusionEJBException var16) {
               throw new PaymentException(var16, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
            }
         }

         if (!paymentData.currency.toLowerCase().equals(userAndCountryInfo.creditCardCurrency.toLowerCase())) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.INCORRECT_CURRENCY, new Object[0]);
         } else if (null == CreditCardPaymentData.getGlobalCollectPaymentProductId(paymentData.cardType)) {
            log.warn("Unsupported credit card payment requested [" + userAndCountryInfo.username + "] [" + paymentData.currency + " " + paymentData.amount + "] type: " + paymentData.cardType);
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ILLEGAL_TRANSACTION, new Object[]{"We do not support the payment you requested. Please choose another."});
         } else {
            double maxRechargeAmountInDefaultCcPaymentCurrency;
            String defaultCcPaymentCurrency;
            try {
               boolean isMerchant = userAndCountryInfo.userType == UserData.TypeEnum.MIG33_MERCHANT || userAndCountryInfo.userType == UserData.TypeEnum.MIG33_TOP_MERCHANT;
               boolean isOnCreditCardWhiteList = accountEJB.isOnCreditCardWhiteList((Connection)null, paymentData.username, (String)null);
               log.info("User " + paymentData.username + " is in credit card whitelist: " + isOnCreditCardWhiteList);
               if (userAndCountryInfo.allowCreditCardPayment == CountryData.AllowCreditCardEnum.ALLOW_IF_BIN_CHECK) {
                  maxRechargeAmountInDefaultCcPaymentCurrency = SystemProperty.getDouble("MaxCreditCardPaymentAmount");
               } else {
                  double[] amounts;
                  if (isMerchant && isOnCreditCardWhiteList) {
                     amounts = SystemProperty.getDoubleArray("CreditCardTrustedPaymentAmounts");
                     maxRechargeAmountInDefaultCcPaymentCurrency = amounts[amounts.length - 1];
                  } else {
                     amounts = SystemProperty.getDoubleArray("CreditCardPaymentAmounts");
                     maxRechargeAmountInDefaultCcPaymentCurrency = amounts[amounts.length - 1];
                  }
               }

               defaultCcPaymentCurrency = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_CREDITCARD.DEFAULT_CURRENCY);
               double paymentAmountInDefaultCcPaymentCurrency = accountEJB.convertCurrency(paymentData.amount, paymentData.currency, defaultCcPaymentCurrency);
               if (paymentAmountInDefaultCcPaymentCurrency > maxRechargeAmountInDefaultCcPaymentCurrency) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Please choose a smaller amount to recharge."});
               }
            } catch (SQLException var17) {
               throw new PaymentException(var17, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
            } catch (NoSuchFieldException var18) {
               throw new PaymentException(var18, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
            }

            CountryData countryData = new CountryData();
            countryData.allowBankTransfer = userAndCountryInfo.allowBankTransfer;
            countryData.allowChequePayment = userAndCountryInfo.allowChequePayment;
            countryData.allowWesternUnion = userAndCountryInfo.allowWesternUnion;
            double amountInBaseCurrency = accountEJB.convertCurrency(paymentData.amount, paymentData.currency, CurrencyData.baseCurrency);
            double maxAmountInBaseCurrency = maxRechargeAmountInDefaultCcPaymentCurrency;
            if (!defaultCcPaymentCurrency.toLowerCase().equals(CurrencyData.baseCurrency.toLowerCase())) {
               maxAmountInBaseCurrency = accountEJB.convertCurrency(maxRechargeAmountInDefaultCcPaymentCurrency, defaultCcPaymentCurrency, CurrencyData.baseCurrency);
            }

            try {
               CreditCardPaymentData creditCardPaymentData = new CreditCardPaymentData();
               creditCardPaymentData.username = paymentData.username;
               creditCardPaymentData.checkNumber = "";
               creditCardPaymentData.status = paymentData.creditCardPaymentStatus;
               creditCardPaymentData.allowAutoApprove = paymentData.allowAutoApprove;
               creditCardPaymentData = accountEJB.validatePastCreditCardHMLTransactions(creditCardPaymentData, countryData, amountInBaseCurrency, maxAmountInBaseCurrency, maxRechargeAmountInDefaultCcPaymentCurrency, userAndCountryInfo.isNewUser);
               if (creditCardPaymentData.status == CreditCardPaymentData.StatusEnum.DECLINED) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED_WO_MESSAGE, new Object[]{creditCardPaymentData.responseCode});
               } else {
                  return paymentData;
               }
            } catch (PaymentException var13) {
               throw var13;
            } catch (EJBException var14) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[]{var14.getMessage()});
            } catch (Exception var15) {
               throw new PaymentException(var15, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
            }
         }
      }
   }

   private boolean isPaymentEligibleForAutoApproval(PaymentData paymentData, CreditCardPaymentUserAndCountryInfo userAndCountryInfo, AccountLocal accountEJB) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.AUTO_APPROVE_CREDIT_CARD_PAYMENT_ENABLED)) {
         return false;
      } else {
         try {
            if (accountEJB.isOnCreditCardWhiteList((Connection)null, paymentData.username, (String)null)) {
               log.info("User " + paymentData.username + " is in credit card whitelist. Transaction is automatically approved if GC clears the transaction.");
               return true;
            }
         } catch (SQLException var8) {
            log.warn("Unable to determine if " + paymentData.username + " is in credit card whitelist.");
         }

         double maxAmountAllowedforAutoApproveUSD = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_MAXIMUM_AUTO_APPROVE_AMOUNT_USD);
         if (maxAmountAllowedforAutoApproveUSD > 0.0D) {
            double amountInUSD = paymentData.amount;
            if (!paymentData.currency.toUpperCase().equals("USD")) {
               amountInUSD = accountEJB.convertCurrency(paymentData.amount, paymentData.currency, "USD");
            }

            if (amountInUSD > maxAmountAllowedforAutoApproveUSD) {
               log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] (USD " + amountInUSD + ") amount more than auto-approve limit which is " + maxAmountAllowedforAutoApproveUSD);
               return false;
            }
         }

         if (SystemProperty.isValueInIntegerArray(userAndCountryInfo.countryId, SystemPropertyEntities.AccountTransaction.AUTO_APPROVE_CREDIT_CARD_RISKY_COUNTRIES)) {
            log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] user country(" + userAndCountryInfo.isoCountryCode + ") is listed under risky countries");
            return false;
         } else {
            return true;
         }
      }
   }

   private void postValidationForCreatePaymentRequest() throws PaymentException {
   }

   private JSONObject sendOrderWithPaymentToGlobalCollect(CreditCardData paymentData, CreditCardPaymentUserAndCountryInfo userAndCountryInfo) throws PaymentException {
      log.info("sendOrderWithPaymentToGlobalCollect");

      try {
         String customerID = paymentData.username;
         if (paymentData.username.length() > 15) {
            customerID = paymentData.username.substring(0, 15);
         }

         String callbackUrl = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_CREDITCARD.HML_CALLBACK_URL);
         StringBuilder request = new StringBuilder();
         request.append("<XML>");
         request.append("<REQUEST>");
         request.append("<ACTION>INSERT_ORDERWITHPAYMENT</ACTION>");
         request.append("<META>");
         request.append("<VERSION>").append(GlobalCollectCreditCardData.getAPIVersion()).append("</VERSION>");
         request.append("<MERCHANTID>").append(paymentData.merchantID).append("</MERCHANTID>");
         request.append("</META>");
         request.append("<PARAMS>");
         request.append("<ORDER>");
         request.append("<ORDERID>").append(paymentData.id).append("</ORDERID>");
         request.append("<CUSTOMERID>").append(customerID).append("</CUSTOMERID>");
         request.append("<IPADDRESSCUSTOMER>").append(userAndCountryInfo.ipAddress).append("</IPADDRESSCUSTOMER>");
         request.append("<AMOUNT>").append((int)(paymentData.amount * 100.0D)).append("</AMOUNT>");
         request.append("<CURRENCYCODE>").append(paymentData.currency).append("</CURRENCYCODE>");
         request.append("<LANGUAGECODE>").append(userAndCountryInfo.isoLanguageCode).append("</LANGUAGECODE>");
         request.append("<COUNTRYCODE>").append(userAndCountryInfo.isoCountryCode).append("</COUNTRYCODE>");
         request.append("</ORDER>");
         request.append("<PAYMENT>");
         request.append("<PAYMENTPRODUCTID>").append(CreditCardPaymentData.getGlobalCollectPaymentProductId(paymentData.cardType)).append("</PAYMENTPRODUCTID>");
         request.append("<AMOUNT>").append((int)(paymentData.amount * 100.0D)).append("</AMOUNT>");
         request.append("<CURRENCYCODE>").append(paymentData.currency).append("</CURRENCYCODE>");
         request.append("<LANGUAGECODE>").append(userAndCountryInfo.isoLanguageCode).append("</LANGUAGECODE>");
         request.append("<COUNTRYCODE>").append(userAndCountryInfo.isoCountryCode).append("</COUNTRYCODE>");
         request.append("<HOSTEDINDICATOR>").append(1).append("</HOSTEDINDICATOR>");
         request.append("<RETURNURL>").append(callbackUrl).append("</RETURNURL>");
         request.append("<CUSTOMERIPADDRESS>").append(userAndCountryInfo.ipAddress).append("</CUSTOMERIPADDRESS>");
         request.append("<CVVINDICATOR>").append(1).append("</CVVINDICATOR>");
         request.append("</PAYMENT>");
         request.append("</PARAMS>");
         request.append("</REQUEST>");
         request.append("</XML>");
         log.info("request to GC " + request);
         JSONObject result = new JSONObject();
         result.put("error", false);

         try {
            SimpleXMLParser reply = new SimpleXMLParser(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.GLOBAL_COLLECT_URL), request.toString(), SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_PAYMENT_STRICT_SSL_CHECK));
            log.info("reply from GC: " + reply);
            JSONObject errorReply = this.parseGlobalCollectErrorMessage(reply);
            if (errorReply.has("message")) {
               log.warn("Unable to complete payment for id [" + paymentData.id + "] :: " + errorReply.getString("globalCollectErrorCode") + " - " + errorReply.getString("globalCollectErrorMessage"));
               result.put("responseCode", errorReply.get("responseCode"));
               result.put("error", true);
               result.put("message", errorReply.get("message"));
               return result;
            }

            String mac = reply.getTagValue("ROW", "MAC");
            String returnMac = reply.getTagValue("ROW", "RETURNMAC");
            String returnUrl = reply.getTagValue("ROW", "FORMACTION");
            if (StringUtil.isBlank(mac) || StringUtil.isBlank(returnUrl) || StringUtil.isBlank(returnMac)) {
               log.warn("CC Payment Error: Unable to obtain provider transaction ID[" + paymentData.username + "] amount: [" + paymentData.currency + " " + paymentData.amount + "] : Unable to retrieve redirect details :: formaction [" + returnUrl + "] mac [" + mac + "] returnmac [" + returnMac + "]");
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
            }

            result.put("providerTransactionId", reply.getTagValue("ROW", "ADDITIONALREFERENCE"));
            result.put("responseCode", reply.getTagValue("ROW", "STATUSID"));
            result.put("returnUrl", returnUrl);
            result.put("mac", mac);
            result.put("returnMac", returnMac);
            result.put("reference", reply.getTagValue("ROW", "REF"));
            result.put("externalReference", reply.getTagValue("ROW", "EXTERNALREFERENCE"));
         } catch (ClientProtocolException var12) {
            log.error("Unable to send cc request to GC: " + var12.getMessage());
         }

         return result;
      } catch (PaymentException var13) {
         throw var13;
      } catch (Exception var14) {
         log.warn("CC Payment Error: [" + paymentData.username + "] amount: [" + paymentData.currency + " " + paymentData.amount + "]", var14);
         throw new PaymentException(var14, ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
      }
   }

   private JSONObject sendPaymentApprovalToGlobalCollect(CreditCardData paymentData) throws PaymentException {
      log.info("sendPaymentApprovalToGlobalCollect");

      try {
         StringBuilder request = new StringBuilder();
         request.append("<XML>");
         request.append("<REQUEST>");
         request.append("<ACTION>SET_PAYMENT</ACTION>");
         request.append("<META>");
         request.append("<MERCHANTID>").append(paymentData.merchantID).append("</MERCHANTID>");
         request.append("</META>");
         request.append("<PARAMS>");
         request.append("<PAYMENT>");
         request.append("<ORDERID>").append(paymentData.id).append("</ORDERID>");
         request.append("<PAYMENTPRODUCTID>").append(CreditCardPaymentData.getGlobalCollectPaymentProductId(paymentData.cardType)).append("</PAYMENTPRODUCTID>");
         request.append("</PAYMENT>");
         request.append("</PARAMS>");
         request.append("</REQUEST>");
         request.append("</XML>");
         log.info("request to GC " + request);
         JSONObject result = new JSONObject();

         try {
            SimpleXMLParser reply = new SimpleXMLParser(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.GLOBAL_COLLECT_URL), request.toString(), SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_PAYMENT_STRICT_SSL_CHECK));
            log.info("reply from GC: " + reply);
            JSONObject errorReply = this.parseGlobalCollectErrorMessage(reply);
            if (errorReply.has("message")) {
               log.warn("Unable to complete payment for id [" + paymentData.id + "] :: " + errorReply.getString("globalCollectErrorCode") + " - " + errorReply.getString("globalCollectErrorMessage"));
               result.put("responseCode", PaymentData.StatusEnum.REJECTED.getEnumValue());
               result.put("message", errorReply.get("message"));
               return result;
            }

            result.put("providerTransactionId", reply.getTagValue("ROW", "ADDITIONALREFERENCE"));
            result.put("responseCode", reply.getTagValue("ROW", "STATUSID"));
            result.put("returnUrl", reply.getTagValue("ROW", "FORMACTION"));
            result.put("mac", reply.getTagValue("ROW", "MAC"));
            result.put("returnmac", reply.getTagValue("ROW", "RETURNMAC"));
            result.put("reference", reply.getTagValue("ROW", "REF"));
            result.put("externalreference", reply.getTagValue("ROW", "EXTERNALREFERENCE"));
         } catch (ClientProtocolException var6) {
            log.error("Unable to send cc request to GC: " + var6.getMessage());
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
         } catch (JSONException var7) {
            log.error("exception in parsing json content from account bean :" + var7.getMessage());
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
         }

         return result;
      } catch (PaymentException var8) {
         throw var8;
      } catch (Exception var9) {
         throw new PaymentException(var9, ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
      }
   }

   private JSONObject sendPaymentRejectionToGlobalCollect(CreditCardData paymentData) throws PaymentException {
      log.info("sendPaymentRejectionToGlobalCollect");

      try {
         StringBuilder request = new StringBuilder();
         request.append("<XML>");
         request.append("<REQUEST>");
         request.append("<ACTION>CANCEL_PAYMENT</ACTION>");
         request.append("<META>");
         request.append("<MERCHANTID>").append(paymentData.merchantID).append("</MERCHANTID>");
         request.append("</META>");
         request.append("<PARAMS>");
         request.append("<PAYMENT>");
         request.append("<ORDERID>").append(paymentData.id).append("</ORDERID>");
         request.append("<EFFORTID>").append(1).append("</EFFORTID>");
         request.append("<ATTEMPTID>").append(1).append("</ATTEMPTID>");
         request.append("</PAYMENT>");
         request.append("</PARAMS>");
         request.append("</REQUEST>");
         request.append("</XML>");
         log.info("request to GC " + request);
         JSONObject result = new JSONObject();

         try {
            SimpleXMLParser reply = new SimpleXMLParser(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.GLOBAL_COLLECT_URL), request.toString(), SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_PAYMENT_STRICT_SSL_CHECK));
            log.info("reply from GC: " + reply);
            JSONObject errorReply = this.parseGlobalCollectErrorMessage(reply);
            if (errorReply.has("message")) {
               log.warn("Unable to cancel payment for id [" + paymentData.id + "] :: " + errorReply.getString("globalCollectErrorCode") + " - " + errorReply.getString("globalCollectErrorMessage"));
               result.put("responseCode", paymentData.status.value());
               result.put("message", errorReply.get("message"));
               return result;
            }
         } catch (ClientProtocolException var6) {
            log.error("Unable to send cc request to GC: " + var6.getMessage());
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
         } catch (JSONException var7) {
            log.error("exception in parsing json content from account bean :" + var7.getMessage());
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
         }

         return result;
      } catch (PaymentException var8) {
         throw var8;
      } catch (Exception var9) {
         throw new PaymentException(var9, ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
      }
   }

   private JSONObject sendGetOrderStatusQueryToGlobalCollect(CreditCardData paymentData) throws Exception {
      try {
         StringBuilder request = new StringBuilder();
         request.append("<XML>");
         request.append("<REQUEST>");
         request.append("<ACTION>GET_ORDERSTATUS</ACTION>");
         request.append("<META>");
         request.append("<MERCHANTID>").append(paymentData.merchantID).append("</MERCHANTID>");
         request.append("<VERSION>").append("2.0").append("</VERSION>");
         request.append("</META>");
         request.append("<PARAMS>");
         request.append("<ORDER>");
         request.append("<ORDERID>").append(paymentData.id).append("</ORDERID>");
         request.append("</ORDER>");
         request.append("</PARAMS>");
         request.append("</REQUEST>");
         request.append("</XML>");
         log.info("request to GC " + request);
         JSONObject result = new JSONObject();

         try {
            SimpleXMLParser reply = new SimpleXMLParser(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.GLOBAL_COLLECT_URL), request.toString(), SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_PAYMENT_STRICT_SSL_CHECK));
            log.info("reply from GC: " + reply);
            JSONObject errorReply = this.parseGlobalCollectErrorMessage(reply);
            if (errorReply.has("message")) {
               log.warn("Unable to complete payment for id [" + paymentData.id + "] :: " + errorReply.getString("globalCollectErrorCode") + " - " + errorReply.getString("globalCollectErrorMessage"));
               result.put("responseCode", PaymentData.StatusEnum.REJECTED.getEnumValue());
               result.put("message", errorReply.get("message"));
               return result;
            }

            GlobalCollectCreditCardData.VendorStatus status = GlobalCollectCreditCardData.VendorStatus.fromValue(Integer.parseInt(reply.getTagValue("STATUS", "STATUSID")));
            String amount = reply.getTagValue("STATUS", "AMOUNT");
            String currencyCode = reply.getTagValue("STATUS", "CURRENCYCODE");
            if (null == status || StringUtil.isBlank(amount) || StringUtil.isBlank(currencyCode)) {
               log.warn("CC Payment Error: Unable to retrieve info [" + paymentData.username + "] amount: [" + paymentData.currency + " " + paymentData.amount + "] : Unable to retrieve redirect details :: amount [" + amount + "] currency [" + currencyCode + "]");
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
            }

            if (status.getStatus() == PaymentData.StatusEnum.ONHOLD) {
               result.put("message", "Please complete the credit card payment process. In case you have already done so, please contact merchant@mig.me.");
            }

            result.put("responseCode", status.getStatus().getEnumValue());
            result.put("amount", amount);
            result.put("currency", currencyCode);
         } catch (ClientProtocolException var9) {
            log.error("Unable to send cc request to GC: " + var9.getMessage());
            throw new PaymentException(var9, ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
         }

         log.info("Returning reply from GC: " + result.toString());
         return result;
      } catch (PaymentException var10) {
         throw var10;
      } catch (Exception var11) {
         log.warn("CC Payment Error: [" + paymentData.username + "] amount: [" + paymentData.currency + " " + paymentData.amount + "]");
         throw new PaymentException(var11, ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Something went wrong with your request. Please contact merchant@mig.me."});
      }
   }

   public static String generateReferenceString(String returnMac, String reference) throws UnsupportedEncodingException {
      return String.format("%s||%s", URLDecoder.decode(returnMac, "UTF-8").replace(" ", "+"), reference);
   }

   private JSONObject parseGlobalCollectErrorMessage(SimpleXMLParser xmlResult) throws JSONException {
      JSONObject response = new JSONObject();
      if (xmlResult.containsTag("ERROR")) {
         response.put("globalCollectErrorCode", xmlResult.getTagValue("ERROR", "CODE"));
         response.put("globalCollectErrorMessage", xmlResult.getTagValue("ERROR", "MESSAGE"));
         GlobalCollectCreditCardData.VendorErrorCode errorResult = GlobalCollectCreditCardData.VendorErrorCode.fromValue(Integer.parseInt(xmlResult.getTagValue("ERROR", "CODE")));
         if (null != errorResult && errorResult.getStatus() == PaymentData.StatusEnum.REJECTED) {
            response.put("responseCode", PaymentData.StatusEnum.REJECTED.value());
            response.put("message", "[" + xmlResult.getTagValue("ERROR", "CODE") + "] Your payment has been rejected. Please contact merchant@mig.me");
         } else {
            response.put("responseCode", PaymentData.StatusEnum.VENDOR_FAILED.value());
            response.put("message", "[" + xmlResult.getTagValue("ERROR", "CODE") + "] We are unable to process your request. Please contact merchant@mig.me");
         }
      }

      return response;
   }

   public <T extends PaymentData> PaymentIResponse approve(T paymentDetails, String username) throws PaymentException, Exception {
      log.info(String.format("approve: username: [%s]", username));
      if (paymentDetails.status != PaymentData.StatusEnum.PENDING) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ILLEGAL_TRANSACTION, new Object[]{"Unable to approve payment with id [" + paymentDetails.id + "]. Payment is currently " + paymentDetails.status.name()});
      } else {
         AccountLocal accountEJB;
         try {
            accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         } catch (CreateException var8) {
            throw new PaymentException(var8, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
         }

         CreditCardData paymentData = (CreditCardData)paymentDetails;
         paymentData.loadMetaDetails();
         JSONObject paymentDataJson = this.sendGetOrderStatusQueryToGlobalCollect(paymentData);
         log.info(String.format("approve: paymentDataJson: [%s]", paymentDataJson.toString()));
         paymentData.status = PaymentData.StatusEnum.fromValue(paymentDataJson.getInt("responseCode"));
         if (paymentData.status == PaymentData.StatusEnum.PENDING) {
            this.sendPaymentApprovalToGlobalCollect(paymentData);
            paymentDataJson = this.sendGetOrderStatusQueryToGlobalCollect(paymentData);
            paymentData.status = PaymentData.StatusEnum.fromValue(paymentDataJson.getInt("responseCode"));
            log.info(String.format("approve: pending: paymentDataJson: [%s]", paymentDataJson.toString()));
         }

         if (paymentData.status != PaymentData.StatusEnum.APPROVED) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ILLEGAL_TRANSACTION, new Object[]{"Unable to approve payment. Payment's status in Global Collect is " + paymentData.status.name()});
         } else {
            paymentData.assignUpdatedTime(new Timestamp(System.currentTimeMillis()));

            try {
               accountEJB.updatePayment(paymentData, paymentData.accountEntrySource);
               log.info("CREDIT CARD APPROVAL: " + username + " approved credit card payment [" + paymentData.id + "] for user [" + paymentData.username + "] " + paymentData.currency + " " + paymentData.amount);
            } catch (PaymentException var7) {
               log.warn("CREDIT CARD APPROVAL FAILURE: " + username + " approved credit card payment [" + paymentData.id + "] for user [" + paymentData.username + "] " + paymentData.currency + " " + paymentData.amount, var7);
               throw var7;
            }

            CreditCardIResponse creditCardIResponse = new CreditCardIResponse();
            creditCardIResponse.amount = String.valueOf(paymentData.amount);
            creditCardIResponse.currency = paymentData.currency;
            creditCardIResponse.status = paymentData.status.name();
            return creditCardIResponse;
         }
      }
   }

   public <T extends PaymentData> PaymentIResponse reject(T paymentDetails, String username) throws PaymentException, Exception {
      log.info(String.format("reject payment for [%s]", username));
      if (paymentDetails.status != PaymentData.StatusEnum.PENDING) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ILLEGAL_TRANSACTION, new Object[]{"Unable to reject payment with id [" + paymentDetails.id + "]. Payment is currently " + paymentDetails.status.name()});
      } else {
         AccountLocal accountEJB;
         try {
            accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            MISLocal var4 = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         } catch (CreateException var9) {
            throw new PaymentException(var9, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
         }

         CreditCardData paymentData = (CreditCardData)paymentDetails;
         paymentData.loadMetaDetails();
         JSONObject paymentDataJson = this.sendGetOrderStatusQueryToGlobalCollect(paymentData);
         paymentData.status = PaymentData.StatusEnum.fromValue(paymentDataJson.getInt("responseCode"));
         if (paymentData.status == PaymentData.StatusEnum.PENDING) {
            this.sendPaymentRejectionToGlobalCollect(paymentData);
            paymentDataJson = this.sendGetOrderStatusQueryToGlobalCollect(paymentData);
            paymentData.status = PaymentData.StatusEnum.fromValue(paymentDataJson.getInt("responseCode"));
         }

         if (paymentData.status != PaymentData.StatusEnum.REJECTED) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ILLEGAL_TRANSACTION, new Object[]{"Unable to reject payment. Payment's status in Global Collect is " + paymentData.status.name()});
         } else {
            paymentData.assignUpdatedTime(new Timestamp(System.currentTimeMillis()));

            try {
               accountEJB.updatePayment(paymentData, paymentData.accountEntrySource);
               log.info("CREDIT CARD CANCELLATION: " + username + " rejected credit card payment [" + paymentData.id + "] for user [" + paymentData.username + "] " + paymentData.currency + " " + paymentData.amount);
            } catch (PaymentException var8) {
               log.warn("CREDIT CARD CANCELLATION FAILURE: " + username + " rejected credit card payment [" + paymentData.id + "] for user [" + paymentData.username + "] " + paymentData.currency + " " + paymentData.amount, var8);
               throw var8;
            }

            CreditCardIResponse creditCardIResponse = new CreditCardIResponse();
            creditCardIResponse.amount = String.valueOf(paymentData.amount);
            creditCardIResponse.currency = paymentData.currency;
            creditCardIResponse.status = paymentData.status.name();
            return creditCardIResponse;
         }
      }
   }
}
