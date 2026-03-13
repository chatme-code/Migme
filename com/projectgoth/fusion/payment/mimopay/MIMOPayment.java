package com.projectgoth.fusion.payment.mimopay;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.HashUtils;
import com.projectgoth.fusion.common.HttpClientUtils;
import com.projectgoth.fusion.common.JSONUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MessageBundle;
import com.projectgoth.fusion.common.PaymentUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentException;
import com.projectgoth.fusion.payment.PaymentIResponse;
import com.projectgoth.fusion.payment.PaymentInterface;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.ejb.CreateException;
import javax.xml.bind.JAXBException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class MIMOPayment implements PaymentInterface {
   private static final Integer COUNTRY_ID_INDONESIA = 107;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MIMOPayment.class));
   private static final MIMOPayment INSTANCE = new MIMOPayment();
   private static final String RESOURCE_MESSAGE_BUNDLE_NAME = "resource.Credit_Notification_Messages";
   private static final String RESOURCE_MESSAGE_RET_CODE_PREFIX_KEY = "MIMOPAY_RETCODE_";
   private static final String RESOURCE_MESSAGE_UNKNOWN_RET_CODE_KEY = "MIMOPAY_RETCODE_UNKNOWN";
   private static final String RESOURCE_MESSAGE_NULL_RET_CODE_KEY = "MIMOPAY_RETCODE_NULL";
   private static final String RESOURCE_MESSAGE_BUSY_KEY = "MIMOPAY_BUSY_PROCESSING";
   private static int REQUIRED_TRANSACTION_ID_LENGTH = 10;
   private static final Charset UTF_8 = Charset.forName("UTF-8");

   private MIMOPayment() {
   }

   public static MIMOPayment getInstance() {
      return INSTANCE;
   }

   public boolean isAccessAllowed(UserData userData) throws PaymentException, Exception {
      if (userData == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[0]);
      } else if ((userData.countryID == null || !userData.countryID.equals(COUNTRY_ID_INDONESIA)) && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.ONLY_FOR_INDONESIAN_USERS)) {
         return false;
      } else if (userData.type == UserData.TypeEnum.MIG33_TOP_MERCHANT && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.NON_TOP_MERCHANTS_ACCESS_ONLY_ENABLED)) {
         return false;
      } else {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.ENABLED_TO_ALL_USERS)) {
            try {
               AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
               if (!accountEJB.isPaymentAllowedToUser(userData.userID, GuardCapabilityEnum.MIMO_PAYMENT_ALLOWED.value())) {
                  return false;
               }
            } catch (CreateException var3) {
               log.error("Unable to get guardset for MIMO Payment.", var3);
               throw new Exception("Unable to continue with payment.");
            } catch (FusionEJBException var4) {
               log.error("Unable to get guardset for MIMO Payment: ", var4);
               throw new Exception("Unable to continue with payment.");
            }
         }

         return true;
      }
   }

   public Map<String, Object> clientInitiatePayment(JSONObject paymentDetails) throws PaymentException, Exception {
      if (JSONUtils.hasNonNullProperty(paymentDetails, "userID")) {
         int userID = paymentDetails.getInt("userID");
         if (JSONUtils.hasNonNullProperty(paymentDetails, "rechargeType")) {
            String rechargeTypeName = paymentDetails.getString("rechargeType");
            MIMOPayment.RechargeTypeEnum rechargeTypeEnum = MIMOPayment.RechargeTypeEnum.fromTypeName(rechargeTypeName);
            if (rechargeTypeEnum == MIMOPayment.RechargeTypeEnum.RELOAD) {
               JSONObject accountEntrySource = paymentDetails.getJSONObject("accountEntrySource");
               if (JSONUtils.hasNonNullProperty(paymentDetails, "accountEntrySource")) {
                  String reloadKey = accountEntrySource.getString("ipAddress");
                  String sessionID = accountEntrySource.getString("sessionID");
                  String mobileDevice = accountEntrySource.getString("mobileDevice");
                  String userAgent = accountEntrySource.getString("userAgent");
                  AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(reloadKey, sessionID, mobileDevice, userAgent);
                  reloadKey = null;
                  if (JSONUtils.hasNonNullProperty(paymentDetails, "reloadKey")) {
                     reloadKey = paymentDetails.getString("reloadKey");
                     return this.clientRequestCreditReload(userID, reloadKey, accountEntrySourceData);
                  } else {
                     throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"reloadKey"});
                  }
               } else {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"accountEntrySource"});
               }
            } else {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_CREDIT_RECHARGE_TYPE, new Object[]{rechargeTypeEnum});
            }
         } else {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"rechargeType"});
         }
      } else {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"userID"});
      }
   }

   private static void validateReloadKey(String reloadKey) throws PaymentException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.ENABLE_RELOAD_KEY_VALIDATION) && SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.EXPECTED_RELOAD_KEY_LENGTH) != reloadKey.trim().length()) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_RELOAD_KEY, new Object[]{reloadKey});
      }
   }

   public static String formatOurTransactionID(int paymentID) throws PaymentException {
      String ourTransactionId = String.valueOf(paymentID);
      if (ourTransactionId.length() > REQUIRED_TRANSACTION_ID_LENGTH) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.GENERATED_PAYMENT_ID_TOO_LONG, new Object[0]);
      } else {
         return StringUtil.padLeft(ourTransactionId, '0', REQUIRED_TRANSACTION_ID_LENGTH);
      }
   }

   public static long getUnixTimestamp(long currentTimeMillies) {
      return currentTimeMillies / 1000L;
   }

   public static String generateHashKey(String merchantCode, String ourFormattedTransactionID, String secretKey) throws GeneralSecurityException {
      String data = merchantCode + ourFormattedTransactionID + secretKey;
      byte[] hashBytes = HashUtils.md5(data);
      return HashUtils.asHex(hashBytes);
   }

   public static <TResponse> TResponse dispatchRequest(Object requestData, Class<TResponse> expectedResponseType) throws IOException, JAXBException, GeneralSecurityException, PaymentException {
      HttpClientUtils.HttpClientConfig clientConfig = new HttpClientUtils.HttpClientConfig();
      clientConfig.keepAliveInSecs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.KEEP_ALIVE_IN_SEC);
      clientConfig.maxConnections = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.MAX_CONNECTIONS);
      clientConfig.strictHttpsCertCheck = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.STRICT_HTTPS_CERT_CHECK);
      clientConfig.timeOutInMillis = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.TIME_OUT_IN_MILLIS);
      clientConfig.username = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.GATEWAY_USERNAME);
      clientConfig.password = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.GATEWAY_PASSWORD);
      String xmlRequestData = MIMOXMLUtils.serializeToXML(requestData);
      String xmlResponseData = null;
      TResponse result = null;
      String url = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.VENDOR_BASE_URL) + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.CREDIT_RELOAD_PATH);
      boolean enableGatewayRequestResponseLogging = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.ENABLE_GATEWAY_REQUEST_RESPONSE_LOGGING);

      try {
         HttpPost postRequest = new HttpPost(url);
         postRequest.addHeader("Content-Type", "application/xml");
         postRequest.setEntity(new StringEntity(xmlRequestData, UTF_8.name()));
         xmlResponseData = getResponseString(clientConfig, postRequest);
         StringReader reader = new StringReader(xmlResponseData);

         try {
            result = MIMOXMLUtils.deserializeFromXML(reader, expectedResponseType);
         } finally {
            reader.close();
         }

         if (enableGatewayRequestResponseLogging) {
            log.info("URI:[" + url + "]\nRequest:[" + xmlRequestData + "]\nResponse:[" + xmlResponseData + "]");
         }

         return result;
      } catch (GeneralSecurityException var17) {
         logOutcomes(url, xmlRequestData, xmlResponseData, var17);
         throw var17;
      } catch (PaymentException var18) {
         logOutcomes(url, xmlRequestData, xmlResponseData, var18);
         throw var18;
      } catch (Exception var19) {
         logOutcomes(url, xmlRequestData, xmlResponseData, var19);
         IOException ioEx = new IOException("Unable to complete request to vendor gateway");
         ioEx.initCause(var19);
         throw ioEx;
      }
   }

   private static String getResponseString(HttpClientUtils.HttpClientConfig clientConfig, HttpPost postRequest) throws GeneralSecurityException, PaymentException {
      String xmlResponseData;
      try {
         xmlResponseData = HttpClientUtils.getResponseString(clientConfig, postRequest, UTF_8);
      } catch (GeneralSecurityException var4) {
         throw var4;
      } catch (IOException var5) {
         throw new PaymentException(var5, ErrorCause.PaymentErrorReasonType.FAILED_TO_RETRIEVE_RESPONSE_FROM_VENDOR, new Object[]{var5.getMessage()});
      }

      if (xmlResponseData == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_VENDOR_DATA, new Object[]{"Received a blank response"});
      } else {
         return xmlResponseData;
      }
   }

   private static void logOutcomes(String url, String xmlRequestData, String xmlResponseData, Exception ex) {
      log.error("URI:[" + url + "]\nRequest:[" + xmlRequestData + "]\nResponse:[" + xmlResponseData + "]" + "\nException:[" + ex + "]");
   }

   public Map<String, Object> clientRequestCreditReload(int userID, String reloadKey, AccountEntrySourceData accountEntrySourceData) throws PaymentException, Exception {
      validateReloadKey(reloadKey);
      String reloadKeyLockKey = userID + ":" + StringUtil.trimmedLowerCase(reloadKey);
      Timestamp currentTimestamp;
      int reloadKeyRecyclePeriodDays;
      if (MemCachedClientWrapper.addOrIncr(MemCachedKeySpaces.CommonKeySpace.MIMOPAY_LOCK_RELOADKEY, reloadKeyLockKey) != 1L) {
         HashMap<String, Object> initiatePaymentResult = new HashMap();
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         currentTimestamp = new Timestamp(System.currentTimeMillis());
         reloadKeyRecyclePeriodDays = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.RELOAD_KEY_RECYCLE_PERIOD_DAYS);
         MIMOVoucherData storedMIMOPaymentData = (MIMOVoucherData)accountEJB.getPaymentByVoucher(PaymentData.TypeEnum.MIMOPAY.value(), (Integer)null, reloadKey, currentTimestamp, reloadKeyRecyclePeriodDays);
         if (storedMIMOPaymentData == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.PAYMENT_DOES_NOT_EXIST, new Object[0]);
         } else if (storedMIMOPaymentData.userId != userID) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RELOAD_CARD_USED_BY_ANOTHER_USER, new Object[0]);
         } else {
            String statusMessage;
            if (storedMIMOPaymentData.status == PaymentData.StatusEnum.ONHOLD) {
               statusMessage = MessageBundle.getMessage("MIMOPAY_BUSY_PROCESSING");
            } else {
               statusMessage = getStatusMessage(storedMIMOPaymentData.vendorStatusCode, storedMIMOPaymentData.vendorTransactionId);
            }

            initiatePaymentResult.put("paymentData", storedMIMOPaymentData);
            initiatePaymentResult.put("statusMessage", statusMessage);
            return initiatePaymentResult;
         }
      } else {
         HashMap var33;
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userEJB.loadUserFromID(userID);
            if (userData == null) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[0]);
            }

            if (!this.isAccessAllowed(userData)) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.NO_ACCESS_TO_PAYMENT_VENDOR, new Object[]{userData.username, PaymentData.TypeEnum.MIMOPAY.displayName()});
            }

            currentTimestamp = new Timestamp(System.currentTimeMillis());
            reloadKeyRecyclePeriodDays = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.RELOAD_KEY_RECYCLE_PERIOD_DAYS);
            AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            MIMOVoucherData storedMIMOPaymentData = (MIMOVoucherData)accountEJB.getPaymentByVoucher(PaymentData.TypeEnum.MIMOPAY.value(), PaymentData.StatusEnum.PENDING.value(), reloadKey, currentTimestamp, reloadKeyRecyclePeriodDays);
            if (storedMIMOPaymentData == null) {
               String merchantCode = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.MERCHANT_CODE);
               String creditReloadPType = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.CREDIT_RELOAD_PTYPE);
               String gameCode = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.GAME_CODE);
               MIMOVoucherData newMIMOVoucherData = new MIMOVoucherData();
               newMIMOVoucherData.assignCreatedTime(new Timestamp(System.currentTimeMillis()));
               newMIMOVoucherData.assignUpdatedTime((Date)null);
               newMIMOVoucherData.username = userData.username;
               newMIMOVoucherData.userId = userData.userID;
               newMIMOVoucherData.amount = 0.0D;
               newMIMOVoucherData.currency = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.RELOAD_VALUE_CURRENY);
               newMIMOVoucherData.voucherCode = reloadKey;
               newMIMOVoucherData.status = PaymentData.StatusEnum.ONHOLD;
               newMIMOVoucherData.merchantCode = merchantCode;
               newMIMOVoucherData.creditReloadPType = creditReloadPType;
               newMIMOVoucherData.vendorStatusCode = null;
               newMIMOVoucherData.gameCode = gameCode;
               newMIMOVoucherData.vendorRemark = null;
               newMIMOVoucherData.rvalue = null;
               storedMIMOPaymentData = (MIMOVoucherData)accountEJB.createPayment(newMIMOVoucherData);
               String formattedTransactionId = formatOurTransactionID(storedMIMOPaymentData.id);
               CreditReloadRequest creditReloadRequest = new CreditReloadRequest();
               creditReloadRequest.serviceName = MIMOPayment.ServiceNameEnum.CREDIT_RELOAD.getServiceCode();
               creditReloadRequest.merchantCode = merchantCode;
               creditReloadRequest.gameCode = gameCode;
               creditReloadRequest.userID = String.valueOf(userID);
               creditReloadRequest.transID = formattedTransactionId;
               creditReloadRequest.reloadCardKey = reloadKey;
               creditReloadRequest.pType = creditReloadPType;
               creditReloadRequest.timestamp = getUnixTimestamp(System.currentTimeMillis());
               creditReloadRequest.hashKey = generateHashKey(merchantCode, formattedTransactionId, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.GATEWAY_SHARED_SECRET));
               CreditReloadResponse creditReloadResponse = null;

               try {
                  creditReloadResponse = (CreditReloadResponse)dispatchRequest(creditReloadRequest, CreditReloadResponse.class);
                  storedMIMOPaymentData = saveResponse(accountEJB, storedMIMOPaymentData, creditReloadResponse, accountEntrySourceData);
               } catch (Exception var26) {
                  log.error("Exception occurred: ", var26);
                  storedMIMOPaymentData.status = PaymentData.StatusEnum.PENDING;
                  newMIMOVoucherData.assignUpdatedTime(new Timestamp(System.currentTimeMillis()));

                  try {
                     storedMIMOPaymentData = (MIMOVoucherData)accountEJB.updatePayment(storedMIMOPaymentData, accountEntrySourceData);
                     log.info("Successfully update transaction id " + storedMIMOPaymentData.id + " status:" + storedMIMOPaymentData.status + ".");
                  } catch (PaymentException var25) {
                     if (var25.getErrorCause() == ErrorCause.PaymentErrorReasonType.DB_UPDATE_PAYMENT_FAILED) {
                        storedMIMOPaymentData = (MIMOVoucherData)accountEJB.getPaymentById(storedMIMOPaymentData.id);
                        log.info("Unsuccessful status update to Transaction id " + storedMIMOPaymentData.id + " status is already :" + storedMIMOPaymentData.status + ".");
                     }
                  }
               }
            } else if (storedMIMOPaymentData.userId != userID) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.RELOAD_CARD_USED_BY_ANOTHER_USER, new Object[0]);
            }

            HashMap<String, Object> initiatePaymentResult = new HashMap();
            initiatePaymentResult.put("paymentData", storedMIMOPaymentData);
            initiatePaymentResult.put("statusMessage", getStatusMessage(storedMIMOPaymentData.vendorStatusCode, storedMIMOPaymentData.vendorTransactionId));
            var33 = initiatePaymentResult;
         } finally {
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.MIMOPAY_LOCK_RELOADKEY, reloadKeyLockKey);
         }

         return var33;
      }
   }

   private static String getStatusMessage(Integer retCode, String vendorTransactionID) {
      String key;
      MIMOPayment.MIMOResultCodeEnum resultCodeEnum;
      if (retCode != null) {
         resultCodeEnum = MIMOPayment.MIMOResultCodeEnum.getMIMOResult(retCode);
         if (resultCodeEnum != null) {
            key = "MIMOPAY_RETCODE_" + retCode;
         } else {
            key = "MIMOPAY_RETCODE_UNKNOWN";
         }
      } else {
         key = "MIMOPAY_RETCODE_NULL";
      }

      resultCodeEnum = null;
      Locale locale;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.ONLY_FOR_INDONESIAN_USERS)) {
         locale = MessageBundle.INDONESIAN_LOCALE;
      } else {
         locale = MessageBundle.DEFAULT_LOCALE;
      }

      return MessageBundle.getMessage("resource.Credit_Notification_Messages", locale, key, retCode, vendorTransactionID);
   }

   private static MIMOVoucherData saveResponse(AccountLocal accountEJB, MIMOVoucherData currentMIMOVoucherData, CreditReloadResponse vendorResponse, AccountEntrySourceData accountEntrySourceData) throws PaymentException {
      MIMOPayment.ServiceNameEnum serviceName = MIMOPayment.ServiceNameEnum.fromServiceCode(vendorResponse.serviceName);
      if (serviceName != MIMOPayment.ServiceNameEnum.CREDIT_RELOAD) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_VENDOR_DATA, new Object[]{"Wrong service name " + serviceName});
      } else if (vendorResponse.retCode == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"CreditReloadResponse.retCode"});
      } else if (StringUtil.isBlank(vendorResponse.transID)) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"CreditReloadResponse.transID"});
      } else {
         int transID = Integer.parseInt(vendorResponse.transID);
         if (currentMIMOVoucherData.id != transID) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_VENDOR_DATA, new Object[]{"Wrong CreditReloadResponse.transID"});
         } else {
            MIMOPayment.MIMOResultCodeEnum mimoResultCodeEnum = MIMOPayment.MIMOResultCodeEnum.getMIMOResult(vendorResponse.retCode);
            PaymentData.StatusEnum paymentStatus;
            if (mimoResultCodeEnum == null) {
               paymentStatus = PaymentData.StatusEnum.PENDING;
            } else {
               paymentStatus = mimoResultCodeEnum.getStatusOnSyncUpdate();
            }

            if (mimoResultCodeEnum == MIMOPayment.MIMOResultCodeEnum.SUCCESSFUL && StringUtil.isBlank(vendorResponse.mimoTransactionID)) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"CreditReloadResponse.mimoTransactionID"});
            } else {
               currentMIMOVoucherData.amount = vendorResponse.reloadValue == null ? 0.0D : vendorResponse.reloadValue;
               currentMIMOVoucherData.assignUpdatedTime(new Timestamp(System.currentTimeMillis()));
               currentMIMOVoucherData.rvalue = vendorResponse.reloadValue;
               currentMIMOVoucherData.status = paymentStatus;
               currentMIMOVoucherData.vendorRemark = vendorResponse.remark;
               currentMIMOVoucherData.vendorStatusCode = vendorResponse.retCode;
               currentMIMOVoucherData.vendorTimestamp = vendorResponse.timestamp;
               currentMIMOVoucherData.vendorTransactionId = StringUtil.isBlank(vendorResponse.mimoTransactionID) ? "" : vendorResponse.mimoTransactionID;

               MIMOVoucherData storedMIMOPaymentData;
               try {
                  storedMIMOPaymentData = (MIMOVoucherData)accountEJB.updatePayment(currentMIMOVoucherData, accountEntrySourceData);
               } catch (PaymentException var10) {
                  if (var10.getErrorCause() != ErrorCause.PaymentErrorReasonType.DB_UPDATE_PAYMENT_FAILED) {
                     throw var10;
                  }

                  storedMIMOPaymentData = (MIMOVoucherData)accountEJB.getPaymentById(currentMIMOVoucherData.id);
                  log.info("Unsuccessful status update to Transaction id " + storedMIMOPaymentData.id + " status is already :" + storedMIMOPaymentData.status + ".");
               }

               return storedMIMOPaymentData;
            }
         }
      }
   }

   public Map<String, Object> updatePaymentStatus(JSONObject paymentDetails) throws Exception {
      throw new UnsupportedOperationException("Not applicable for this vendor");
   }

   public String getCurrencyForUser(UserData userData) {
      String[] supportedCurrencies = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.SUPPORTED_CURRENCIES);
      String[] arr$ = supportedCurrencies;
      int len$ = supportedCurrencies.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String currency = arr$[i$];
         currency = PaymentUtils.normalizeCurrency(currency);
         if (currency.equalsIgnoreCase(userData.currency)) {
            return currency;
         }
      }

      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.DEFAULT_CURRENCY);
   }

   public <T extends PaymentData> PaymentIResponse clientInitiatePayment(T paymentDetails) throws PaymentException, Exception {
      return null;
   }

   public <T extends PaymentData> PaymentIResponse updatePaymentStatus(T paymentDetails) throws PaymentException, Exception {
      return null;
   }

   public PaymentIResponse onPaymentAuthorized(String token, JSONObject params) throws PaymentException, Exception {
      return null;
   }

   public <T extends PaymentData> PaymentIResponse approve(T paymentDetails, String username) throws PaymentException, Exception {
      return null;
   }

   public <T extends PaymentData> PaymentIResponse reject(T paymentDetails, String username) throws PaymentException, Exception {
      return null;
   }

   public static enum ServiceNameEnum implements EnumUtils.IEnumValueGetter<String> {
      CREDIT_RELOAD("GV_TOPUP"),
      CREDIT_ELOAD("GV_ELOAD"),
      PENDING_STATUS_INQUIRY("GV_INQUIRY");

      private String serviceCode;
      private static HashMap<String, MIMOPayment.ServiceNameEnum> lookupByValue = new HashMap();

      private ServiceNameEnum(String serviceCode) {
         this.serviceCode = StringUtil.trimmedUpperCase(serviceCode);
      }

      public String getEnumValue() {
         return this.getServiceCode();
      }

      public static MIMOPayment.ServiceNameEnum fromServiceCode(String serviceCode) {
         return serviceCode == null ? null : (MIMOPayment.ServiceNameEnum)lookupByValue.get(StringUtil.trimmedUpperCase(serviceCode));
      }

      public String getServiceCode() {
         return this.serviceCode;
      }

      static {
         EnumUtils.populateLookUpMap(lookupByValue, MIMOPayment.ServiceNameEnum.class);
      }
   }

   public static enum MIMOResultCodeEnum implements EnumUtils.IEnumValueGetter<Integer> {
      SUCCESSFUL(50000, PaymentData.StatusEnum.APPROVED),
      PENDING_TRANSACTION(50001, PaymentData.StatusEnum.PENDING),
      RECORD_NOT_FOUND(50002, PaymentData.StatusEnum.REJECTED),
      INVALID_TRANSACTION_ID(50003, PaymentData.StatusEnum.REJECTED),
      RETRY_REQUIRED_BY_CLIENT(50004, PaymentData.StatusEnum.TIMEOUT),
      INVALID_RELOAD_CARD(51001, PaymentData.StatusEnum.REJECTED),
      RELOAD_CARD_EXPIRED(51002, PaymentData.StatusEnum.REJECTED),
      USED_RELOAD_CARD(51003, PaymentData.StatusEnum.REJECTED),
      UNKNOWN_ERROR(51999, PaymentData.StatusEnum.PENDING),
      INVALID_XML(80000, PaymentData.StatusEnum.REJECTED),
      XML_PARAMETER_VALIDATION_FAILED(80001, PaymentData.StatusEnum.REJECTED),
      DATABASE_CONNECTION_TIMEOUT(81001, PaymentData.StatusEnum.VENDOR_FAILED),
      DATABASE_ERROR(81002, PaymentData.StatusEnum.VENDOR_FAILED),
      TIMEOUT(82001, PaymentData.StatusEnum.VENDOR_FAILED),
      USERID_TEMPORARILY_BLOCKED(10000, PaymentData.StatusEnum.REJECTED),
      VOUCHER_CODE_TEMPORARILY_BLOCKED(10001, PaymentData.StatusEnum.REJECTED);

      private Integer code;
      private PaymentData.StatusEnum statusOnSyncUpdate;
      private static final HashMap<Integer, MIMOPayment.MIMOResultCodeEnum> lookUpMap = new HashMap();

      public int getCode() {
         return this.code;
      }

      public PaymentData.StatusEnum getStatusOnSyncUpdate() {
         return this.statusOnSyncUpdate;
      }

      private MIMOResultCodeEnum(int code, PaymentData.StatusEnum onlineStatus) {
         this.code = code;
         this.statusOnSyncUpdate = onlineStatus;
      }

      public Integer getEnumValue() {
         return this.code;
      }

      public static MIMOPayment.MIMOResultCodeEnum getMIMOResult(int resCode) {
         return (MIMOPayment.MIMOResultCodeEnum)lookUpMap.get(resCode);
      }

      static {
         EnumUtils.populateLookUpMap(lookUpMap, MIMOPayment.MIMOResultCodeEnum.class);
      }
   }

   public static enum RechargeTypeEnum implements EnumUtils.IEnumValueGetter<String> {
      RELOAD("RELOAD", MIMOPayment.ServiceNameEnum.CREDIT_RELOAD),
      ELOAD("ELOAD", MIMOPayment.ServiceNameEnum.CREDIT_ELOAD);

      private String typeName;
      private MIMOPayment.ServiceNameEnum serviceName;
      private static HashMap<String, MIMOPayment.RechargeTypeEnum> lookupByValue = new HashMap();

      public static MIMOPayment.RechargeTypeEnum fromTypeName(String name) {
         return name == null ? null : (MIMOPayment.RechargeTypeEnum)lookupByValue.get(StringUtil.trimmedUpperCase(name));
      }

      private RechargeTypeEnum(String typeName, MIMOPayment.ServiceNameEnum serviceName) {
         this.typeName = StringUtil.trimmedUpperCase(typeName);
         this.serviceName = serviceName;
      }

      public String getEnumValue() {
         return this.typeName;
      }

      public String getTypeName() {
         return this.typeName;
      }

      public MIMOPayment.ServiceNameEnum getServiceName() {
         return this.serviceName;
      }

      static {
         EnumUtils.populateLookUpMap(lookupByValue, MIMOPayment.RechargeTypeEnum.class);
      }
   }
}
