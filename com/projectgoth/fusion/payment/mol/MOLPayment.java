package com.projectgoth.fusion.payment.mol;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.HashUtils;
import com.projectgoth.fusion.common.HttpClientUtils;
import com.projectgoth.fusion.common.JSONUtils;
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
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class MOLPayment implements PaymentInterface {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MOLPayment.class));
   private static MOLPayment instance = new MOLPayment();
   public static final String MOL_QUERY_STATUS_SUCCESS = "1";
   private static final String URL_ENCODER_ENCODING = "UTF-8";

   private static String generateSignature(String data) throws NoSuchAlgorithmException {
      MessageDigest md = MessageDigest.getInstance("SHA1");
      byte[] hashBytes = md.digest(data.getBytes());
      return HashUtils.asHex(hashBytes);
   }

   private MOLPayment() {
   }

   public static MOLPayment getInstance() {
      return instance;
   }

   private static String normalizeCurrency(String currency) {
      return currency == null ? null : currency.trim().toUpperCase();
   }

   public Map<String, Object> clientInitiatePayment(JSONObject paymentDetails) throws PaymentException, Exception {
      try {
         if (JSONUtils.hasNonNullProperty(paymentDetails, "username")) {
            String username = StringUtil.trimmedLowerCase(paymentDetails.getString("username"));
            if (StringUtil.isBlank(username)) {
               throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Username is blank.");
            } else if (JSONUtils.hasNonNullProperty(paymentDetails, "amount")) {
               double amount = paymentDetails.getDouble("amount");
               if (!(amount < 0.0D) && !Double.isNaN(amount)) {
                  if (JSONUtils.hasNonNullProperty(paymentDetails, "currency")) {
                     String currency = PaymentUtils.normalizeCurrency(paymentDetails.getString("currency"));
                     if (StringUtil.isBlank(currency)) {
                        throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Currency code is blank.");
                     } else {
                        UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                        UserData userData = userEJB.loadUserByUsernameOrAlias(username, false, false);
                        if (userData == null) {
                           throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[]{username});
                        } else {
                           boolean userHasAccess = this.isAccessAllowed(userData);
                           if (!userHasAccess) {
                              throw new PaymentException(ErrorCause.PaymentErrorReasonType.NO_ACCESS_TO_PAYMENT_VENDOR, new Object[]{username, PaymentData.TypeEnum.MOL.displayName()});
                           } else {
                              AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
                              int maximumPendingPaymentsCount = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MAXIMUM_PENDING_PAYMENTS_COUNT);
                              if (maximumPendingPaymentsCount > 0 && accountEJB.getPendingPaymentsCount(userData.userID, PaymentData.TypeEnum.MOL.value()) >= maximumPendingPaymentsCount) {
                                 throw new PaymentException(ErrorCause.PaymentErrorReasonType.TOO_MANY_PENDING_PAYMENTS, new Object[]{PaymentData.TypeEnum.MOL.displayName()});
                              } else {
                                 String currencyMOL = this.getCurrencyForUser(userData);
                                 if (!currencyMOL.equalsIgnoreCase(currency)) {
                                    throw new PaymentException(ErrorCause.PaymentErrorReasonType.INCORRECT_CURRENCY, new Object[0]);
                                 } else {
                                    double minAmount = accountEJB.convertCurrency(SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MIN_AMOUNT), SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.DEFAULT_CURRENCY), currency);
                                    if (amount < minAmount) {
                                       throw new PaymentException(ErrorCause.PaymentErrorReasonType.PURCHASE_AMOUNT_BELOW_MINIMUM, new Object[]{currency, PaymentUtils.formatAmountInCurrency(minAmount)});
                                    } else {
                                       double maxAmount = accountEJB.convertCurrency(SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MAX_AMOUNT), SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.DEFAULT_CURRENCY), currency);
                                       if (amount > maxAmount) {
                                          throw new PaymentException(ErrorCause.PaymentErrorReasonType.PURCHASE_AMOUNT_ABOVE_MAXIMUM, new Object[]{currency, PaymentUtils.formatAmountInCurrency(maxAmount)});
                                       } else {
                                          String merchantID = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MERCHANT_ID);
                                          String vendorBaseURL = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.VENDOR_BASE_URL);
                                          String heartBeatPath = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.HEARTBEAT_PATH);
                                          log.debug("fetching heartbeat...");
                                          String heartBeat = fetchHeartBeat(merchantID, vendorBaseURL, heartBeatPath);
                                          if (log.isDebugEnabled()) {
                                             log.debug("heartbeat " + heartBeat);
                                          }

                                          MOLPaymentData molPaymentData = new MOLPaymentData();
                                          molPaymentData.transDateTime = null;
                                          molPaymentData.assignCreatedTime(new Timestamp(System.currentTimeMillis()));
                                          molPaymentData.assignUpdatedTime((Date)null);
                                          molPaymentData.username = userData.username;
                                          molPaymentData.userId = userData.userID;
                                          molPaymentData.amount = amount;
                                          molPaymentData.currency = currency;
                                          molPaymentData.merchantID = merchantID;
                                          molPaymentData.paymentDescription = "Payment for migme credits worth " + currency + " " + PaymentUtils.formatAmountInCurrency(amount) + ".";
                                          log.debug("creating payment");
                                          PaymentData storedPaymentData = accountEJB.createPayment(molPaymentData);
                                          String merchantPIN = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MERCHANT_PIN);
                                          String purchasePath = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.PURCHASE_PATH);
                                          String merchantTransactionID = storedPaymentData.id.toString();
                                          log.debug("generating signature");
                                          String amountString = String.valueOf(molPaymentData.amount);
                                          String signature = generateSignatureForPurchase(merchantID, merchantTransactionID, amountString, currency, merchantPIN, heartBeat);
                                          StringBuilder loginUrl = (new StringBuilder(512)).append(vendorBaseURL).append("/").append(purchasePath).append("?").append("MerchantID=").append(URLEncoder.encode(merchantID, "UTF-8")).append("&Amount=").append(URLEncoder.encode(amountString, "UTF-8")).append("&MRef_ID=").append(URLEncoder.encode(merchantTransactionID, "UTF-8")).append("&Description=").append(URLEncoder.encode(molPaymentData.paymentDescription, "UTF-8")).append("&Currency=").append(URLEncoder.encode(currency, "UTF-8")).append("&HeartBeat=").append(URLEncoder.encode(heartBeat, "UTF-8")).append("&Signature=").append(URLEncoder.encode(signature, "UTF-8"));
                                          HashMap<String, Object> initiatePaymentResult = new HashMap();
                                          initiatePaymentResult.put("paymentData", storedPaymentData);
                                          initiatePaymentResult.put("loginUrl", loginUrl.toString());
                                          return initiatePaymentResult;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  } else {
                     throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Currency code is not supplied.");
                  }
               } else {
                  throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Amount should be a positive value");
               }
            } else {
               throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Amount is not supplied.");
            }
         } else {
            throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Username is not supplied.");
         }
      } catch (CreateException var29) {
         throw new Exception("Unable to proceed with MOL payment.", var29);
      } catch (NoSuchAlgorithmException var30) {
         throw new Exception("Unable to proceed with MOL payment.", var30);
      } catch (UnsupportedEncodingException var31) {
         throw new Exception("Unable to proceed with MOL payment.", var31);
      } catch (PaymentException var32) {
         throw var32;
      } catch (FusionRestException var33) {
         throw var33;
      } catch (Exception var34) {
         throw new Exception("Unable to proceed with MOL payment.", var34);
      }
   }

   private static Document getXMLResponseAsDocument(HttpUriRequest request) throws GeneralSecurityException, IOException {
      HttpClientUtils.HttpClientConfig clientConfig = new HttpClientUtils.HttpClientConfig();
      clientConfig.keepAliveInSecs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.KEEP_ALIVE_IN_SEC);
      clientConfig.maxConnections = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MAX_CONNECTIONS);
      clientConfig.strictHttpsCertCheck = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.STRICT_HTTPS_CERT_CHECK);
      clientConfig.timeOutInMillis = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.TIME_OUT_IN_MILLIS);
      return HttpClientUtils.getXMLResponseAsDocument(clientConfig, request);
   }

   public static String fetchHeartBeat(String merchantID, String vendorBaseURL, String heartBeatPath) throws Exception {
      try {
         URI heartBeatURI = (new URL(new URL(vendorBaseURL), heartBeatPath)).toURI();
         HttpPost httpPost = new HttpPost(heartBeatURI);
         List<NameValuePair> nvps = new ArrayList();
         nvps.add(new BasicNameValuePair("MerchantID", merchantID));
         httpPost.setEntity(new UrlEncodedFormEntity(nvps));
         Document doc = getXMLResponseAsDocument(httpPost);
         if (doc == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{"Returned XML document is null"});
         } else {
            Element elem = (Element)doc.getFirstChild();
            String resCode = MOLXMLUtils.getChildNodeTagValue(elem, "ResCode");
            String heartBeat = "";
            if (resCode != null && Integer.parseInt(resCode.trim()) == MOLPayment.MOLResultCodeEnum.TRANSACTION_SUCCESSFULLY_COMPLETED.getCode()) {
               heartBeat = MOLXMLUtils.getChildNodeTagValue(elem, "HB");
               if (heartBeat == null) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{"HB tag not found"});
               } else if (heartBeat.length() == 0) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{"HB tag has blank value"});
               } else if (heartBeat.equals("00000000-0000-0000-0000-000000000000")) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{"HeartBeat value is invalid:" + heartBeat});
               } else {
                  return heartBeat;
               }
            } else {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{"Unable to fetch heartbeat to MOL Service Provider.Vendor Result Code=" + resCode});
            }
         }
      } catch (PaymentException var10) {
         throw new Exception("Unable to initiate MOL payment.", var10);
      } catch (MalformedURLException var11) {
         throw new Exception("Unable to initiate MOL payment.", var11);
      } catch (URISyntaxException var12) {
         throw new Exception("Unable to initiate MOL payment.", var12);
      } catch (UnsupportedEncodingException var13) {
         throw new Exception("Unable to initiate MOL payment.", var13);
      } catch (ClientProtocolException var14) {
         throw new Exception("Unable to initiate MOL payment.", var14);
      } catch (IOException var15) {
         throw new Exception("Unable to initiate MOL payment.", var15);
      }
   }

   public boolean isAccessAllowed(UserData userData) throws PaymentException, Exception {
      if (userData == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[0]);
      } else if (userData.type == UserData.TypeEnum.MIG33_TOP_MERCHANT && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.NON_TOP_MERCHANTS_ACCESS_ONLY_ENABLED)) {
         return false;
      } else {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.ENABLED_TO_ALL_USERS)) {
            try {
               AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
               if (!accountEJB.isPaymentAllowedToUser(userData.userID, GuardCapabilityEnum.MOL_PAYMENT_ALLOWED.value())) {
                  return false;
               }
            } catch (CreateException var3) {
               log.error("Unable to get guardset for MOL Payment.", var3);
               throw new Exception("Unable to continue with payment.");
            } catch (FusionEJBException var4) {
               log.error("Unable to get guardset for MOL Payment: ", var4);
               throw new Exception("Unable to continue with payment.");
            }
         }

         return true;
      }
   }

   public String getCurrencyForUser(UserData userData) {
      String[] supportedCurrencies = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.SUPPORTED_CURRENCIES);
      String[] arr$ = supportedCurrencies;
      int len$ = supportedCurrencies.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String currency = arr$[i$];
         currency = PaymentUtils.normalizeCurrency(currency);
         if (currency.equalsIgnoreCase(userData.currency)) {
            return currency;
         }
      }

      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.DEFAULT_CURRENCY);
   }

   public static String generateQueryTransactionStatusRequestSignature(String merchantID, String merchantTransactionIDString, String merchantPIN, String heartBeat) throws NoSuchAlgorithmException {
      String concatInput = merchantID + merchantTransactionIDString + merchantPIN + heartBeat;
      return generateSignature(concatInput.trim().toLowerCase());
   }

   public static MOLQueryTransactionStatusResult getVendorTransactionStatus(String transactionId) throws Exception {
      String merchantID = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MERCHANT_ID);
      String merchantPIN = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MERCHANT_PIN);
      String vendorBaseURL = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.VENDOR_BASE_URL);
      String heartBeatPath = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.HEARTBEAT_PATH);
      String queryTxStatusPath = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.QUERY_TRANSACTION_STATUS_PATH);
      String heartBeat = fetchHeartBeat(merchantID, vendorBaseURL, heartBeatPath);
      MOLQueryTransactionStatusResult qryTxResult = getVendorTransactionStatus(merchantID, transactionId, merchantPIN, heartBeat, vendorBaseURL, queryTxStatusPath);
      String calcQueryTxStatusResponse = generateQueryTransactionStatusResponseSignature(qryTxResult.resCode, merchantID, transactionId, merchantPIN, qryTxResult.amount, qryTxResult.currency);
      if (!calcQueryTxStatusResponse.equals(qryTxResult.vendorSignature)) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{"Query Transaction Status result has an invalid signature. Supplied:[" + qryTxResult.vendorSignature + "].Calculated:[" + calcQueryTxStatusResponse + "]"});
      } else {
         return qryTxResult;
      }
   }

   public static MOLQueryTransactionStatusResult getVendorTransactionStatus(String merchantID, String transactionId, String merchantPIN, String heartBeat, String vendorBaseURL, String queryTxStatusPath) throws PaymentException, IOException, GeneralSecurityException, ParserConfigurationException, SAXException, URISyntaxException {
      String signature = generateQueryTransactionStatusRequestSignature(merchantID, transactionId, merchantPIN, heartBeat);
      URI heartBeatURI = (new URL(new URL(vendorBaseURL), queryTxStatusPath)).toURI();
      HttpPost httpPost = new HttpPost(heartBeatURI);
      List<NameValuePair> nvps = new ArrayList();
      nvps.add(new BasicNameValuePair("MerchantID", merchantID));
      nvps.add(new BasicNameValuePair("MRef_ID", transactionId));
      nvps.add(new BasicNameValuePair("HeartBeat", heartBeat));
      nvps.add(new BasicNameValuePair("Signature", signature));
      httpPost.setEntity(new UrlEncodedFormEntity(nvps));
      Document doc = getXMLResponseAsDocument(httpPost);
      if (doc == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{"Returned XML document is null"});
      } else {
         return new MOLQueryTransactionStatusResult(doc);
      }
   }

   private static String sanitizeStringForSignatureGeneration(String s) {
      return s == null ? "" : s.toLowerCase();
   }

   public static String generateQueryTransactionStatusResponseSignature(String resultCode, String merchantID, String transactionID, String merchantPIN, String amount, String currency) throws NoSuchAlgorithmException {
      StringBuilder concatInput = new StringBuilder(512);
      concatInput.append(sanitizeStringForSignatureGeneration(resultCode));
      concatInput.append(sanitizeStringForSignatureGeneration(merchantID));
      concatInput.append(sanitizeStringForSignatureGeneration(transactionID));
      concatInput.append(sanitizeStringForSignatureGeneration(merchantPIN));
      concatInput.append(sanitizeStringForSignatureGeneration(amount));
      concatInput.append(sanitizeStringForSignatureGeneration(currency));
      return generateSignature(concatInput.toString());
   }

   public static String generateSignatureForOnlineNotification(String resultCode, String merchantID, String transactionID, String amount, String currency, String vendorTransactionID, String clientMOLUserName, String secretPIN) throws NoSuchAlgorithmException {
      StringBuilder concatInput = new StringBuilder(512);
      concatInput.append(sanitizeStringForSignatureGeneration(resultCode));
      concatInput.append(sanitizeStringForSignatureGeneration(merchantID));
      concatInput.append(sanitizeStringForSignatureGeneration(transactionID));
      concatInput.append(sanitizeStringForSignatureGeneration(amount));
      concatInput.append(sanitizeStringForSignatureGeneration(currency));
      concatInput.append(sanitizeStringForSignatureGeneration(vendorTransactionID));
      concatInput.append(sanitizeStringForSignatureGeneration(clientMOLUserName));
      concatInput.append(sanitizeStringForSignatureGeneration(secretPIN));
      return generateSignature(concatInput.toString());
   }

   public static String generateSignatureForOfflineNotification(String resultCode, String merchantID, String transactionID, String amount, String currency, String vendorTransactionID, String clientMOLUserName, String secretPIN) throws NoSuchAlgorithmException {
      StringBuilder concatInput = new StringBuilder(512);
      concatInput.append(sanitizeStringForSignatureGeneration(resultCode));
      concatInput.append(sanitizeStringForSignatureGeneration(merchantID));
      concatInput.append(sanitizeStringForSignatureGeneration(transactionID));
      concatInput.append(sanitizeStringForSignatureGeneration(amount));
      concatInput.append(sanitizeStringForSignatureGeneration(currency));
      concatInput.append(sanitizeStringForSignatureGeneration(vendorTransactionID));
      concatInput.append(sanitizeStringForSignatureGeneration(clientMOLUserName));
      concatInput.append(sanitizeStringForSignatureGeneration(secretPIN));
      return generateSignature(concatInput.toString());
   }

   public static String generateSignatureForPurchase(String merchantID, String merchantTransactionIDString, String amountString, String currency, String merchantPIN, String heartBeat) throws NoSuchAlgorithmException {
      StringBuilder concatInput = new StringBuilder(512);
      concatInput.append(sanitizeStringForSignatureGeneration(merchantID));
      concatInput.append(sanitizeStringForSignatureGeneration(merchantTransactionIDString));
      concatInput.append(sanitizeStringForSignatureGeneration(amountString));
      concatInput.append(sanitizeStringForSignatureGeneration(currency));
      concatInput.append(sanitizeStringForSignatureGeneration(merchantPIN));
      concatInput.append(sanitizeStringForSignatureGeneration(heartBeat));
      return generateSignature(concatInput.toString());
   }

   public Map<String, Object> updatePaymentStatus(JSONObject paymentDetails) throws Exception {
      String resCode = paymentDetails.getString("ResCode");
      String merchantID = paymentDetails.getString("MerchantID");
      String transactionIDString = paymentDetails.getString("MRef_ID");
      String amount = paymentDetails.getString("Amount");
      String currency = paymentDetails.getString("Currency");
      String vendorTransactionID = paymentDetails.getString("MOLOrderID");
      String clientMolUserName = paymentDetails.getString("MOLUsername");
      String transactionDateTime = paymentDetails.getString("TransDateTime");
      String vendorSignature = paymentDetails.getString("Signature");
      boolean isOnlineNotification = paymentDetails.getBoolean("IsOnline");
      JSONObject accountEntrySource = paymentDetails.getJSONObject("accountEntrySource");
      String ipAddress = accountEntrySource.getString("ipAddress");
      String sessionID = accountEntrySource.getString("sessionID");
      String mobileDevice = accountEntrySource.getString("mobileDevice");
      String userAgent = accountEntrySource.getString("userAgent");
      AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent);
      return this.updatePaymentStatus(isOnlineNotification, resCode, merchantID, transactionIDString, amount, currency, vendorTransactionID, clientMolUserName, transactionDateTime, vendorSignature, accountEntrySourceData);
   }

   private Map<String, Object> updatePaymentStatus(boolean isOnlineNotification, String resCode, String merchantID, String transactionIDString, String amount, String currency, String vendorTransactionID, String clientMolUserName, String transactionDateTime, String vendorSignature, AccountEntrySourceData accountEntrySourceData) throws PaymentException, Exception {
      AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);

      try {
         accountEJB.getPaymentById(Integer.parseInt(transactionIDString));
      } catch (EJBException var25) {
         log.error("Payment with id [" + transactionIDString + "] not found.");
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.PAYMENT_DOES_NOT_EXIST, new Object[0]);
      }

      int resCodeInt = true;
      if (resCode != null) {
         int resCodeInt = Integer.parseInt(resCode.trim());
         PaymentData.StatusEnum paymentStatus = null;
         MOLPayment.MOLResultCodeEnum molResultCode = MOLPayment.MOLResultCodeEnum.getMOLResult(resCodeInt);
         if (molResultCode != null) {
            paymentStatus = molResultCode.getStatusOnSyncUpdate();
         } else {
            paymentStatus = PaymentData.StatusEnum.PENDING;
         }

         String merchantPIN = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MERCHANT_PIN);
         String calcVendorSignature;
         if (isOnlineNotification) {
            calcVendorSignature = generateSignatureForOnlineNotification(resCode, merchantID, transactionIDString, amount, currency, vendorTransactionID, clientMolUserName, merchantPIN);
         } else {
            calcVendorSignature = generateSignatureForOfflineNotification(resCode, merchantID, transactionIDString, amount, currency, vendorTransactionID, clientMolUserName, merchantPIN);
         }

         if (!calcVendorSignature.equals(vendorSignature)) {
            throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "UpdatePaymentStatus has an invalid signature. Supplied:[" + vendorSignature + "].Calculated:[" + calcVendorSignature + "]");
         } else {
            String configuredMerchantID = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.MERCHANT_ID);
            if (!configuredMerchantID.equalsIgnoreCase(merchantID)) {
               throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Wrong MerchantID:" + merchantID);
            } else {
               if (isOnlineNotification) {
                  String vendorBaseURL = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.VENDOR_BASE_URL);
                  String heartBeatPath = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.HEARTBEAT_PATH);
                  String queryTxStatusPath = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.QUERY_TRANSACTION_STATUS_PATH);
                  String heartBeat = fetchHeartBeat(configuredMerchantID, vendorBaseURL, heartBeatPath);
                  MOLQueryTransactionStatusResult queryTxStatusResult = getVendorTransactionStatus(configuredMerchantID, transactionIDString, merchantPIN, heartBeat, vendorBaseURL, queryTxStatusPath);
                  String calcQueryTxStatusResponse = generateQueryTransactionStatusResponseSignature(queryTxStatusResult.resCode, configuredMerchantID, transactionIDString, merchantPIN, queryTxStatusResult.amount, queryTxStatusResult.currency);
                  if (!calcQueryTxStatusResponse.equals(queryTxStatusResult.vendorSignature)) {
                     throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Failed to validate online notification.Query Transaction Status result has an invalid signature. Supplied:[" + queryTxStatusResult.vendorSignature + "].Calculated:[" + calcQueryTxStatusResponse + "]");
                  }

                  if (queryTxStatusResult.resCode == null || Integer.parseInt(queryTxStatusResult.resCode.trim()) != MOLPayment.MOLResultCodeEnum.TRANSACTION_SUCCESSFULLY_COMPLETED.getCode()) {
                     throw new IOException("Failed to validate online notification.Query transaction status Result Code = " + queryTxStatusResult.resCode);
                  }

                  if (!transactionIDString.equals(queryTxStatusResult.transactionId)) {
                     throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Failed to validate online notification.TransactionID supplied=[" + transactionIDString + "].TransactionID from query status=" + queryTxStatusResult.transactionId);
                  }

                  if (!vendorTransactionID.equals(queryTxStatusResult.vendorTransactionId)) {
                     throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Failed to validate online notification.VendorTransactionID supplied=[" + vendorTransactionID + "].VendorTransactionID from query status=" + queryTxStatusResult.vendorTransactionId);
                  }

                  if (Double.parseDouble(amount.trim()) != Double.parseDouble(queryTxStatusResult.amount.trim())) {
                     throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Failed to validate online notification.Amount supplied=[" + amount + "].Amount from query status=" + queryTxStatusResult.amount);
                  }

                  if (!normalizeCurrency(currency).equalsIgnoreCase(normalizeCurrency(queryTxStatusResult.currency))) {
                     throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Failed to validate online notification.Currency supplied=[" + currency + "].Currency from query status=" + queryTxStatusResult.currency);
                  }

                  if (queryTxStatusResult.status.equals("1")) {
                     if (molResultCode != MOLPayment.MOLResultCodeEnum.TRANSACTION_SUCCESSFULLY_COMPLETED) {
                        throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Query status returned inconsistent state molResult=" + molResultCode.name() + ",queryTxStatusResult=" + queryTxStatusResult.status);
                     }
                  } else if (molResultCode == MOLPayment.MOLResultCodeEnum.TRANSACTION_SUCCESSFULLY_COMPLETED) {
                     throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "Query status returned inconsistent state molResult=" + molResultCode.name() + ",queryTxStatusResult=" + queryTxStatusResult.status);
                  }
               }

               int transactionId = Integer.parseInt(transactionIDString);
               MOLPaymentData updPaymentData = (MOLPaymentData)accountEJB.getPaymentById(transactionId);
               updPaymentData.amount = Double.parseDouble(amount);
               updPaymentData.currency = normalizeCurrency(currency);
               updPaymentData.vendorTransactionId = vendorTransactionID;
               updPaymentData.vendorType = PaymentData.TypeEnum.MOL;
               updPaymentData.status = paymentStatus;
               updPaymentData.assignUpdatedTime(new Timestamp(System.currentTimeMillis()));
               updPaymentData.transDateTime = transactionDateTime;
               updPaymentData.vendorStatusUpdResCode = resCode;
               updPaymentData.asynchStatusUpdResCode = null;
               Object storedPaymentData = null;

               try {
                  storedPaymentData = accountEJB.updatePayment(updPaymentData, accountEntrySourceData);
                  log.info("Successfully update transaction id " + updPaymentData.id + " status:" + updPaymentData.status + ". Received resCode:" + resCode);
               } catch (PaymentException var26) {
                  if (var26.getErrorCause() == ErrorCause.PaymentErrorReasonType.DB_UPDATE_PAYMENT_FAILED) {
                     storedPaymentData = (MOLPaymentData)accountEJB.getPaymentById(transactionId);
                     log.info("Unsuccessful status update to Transaction id " + ((PaymentData)storedPaymentData).id + " status is already :" + ((PaymentData)storedPaymentData).status + ". Received resCode:" + resCode);
                  }
               }

               HashMap<String, Object> initiatePaymentResult = new HashMap();
               initiatePaymentResult.put("paymentData", storedPaymentData);
               return initiatePaymentResult;
            }
         }
      } else {
         throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "ResCode not present");
      }
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

   public static enum MOLResultCodeEnum implements EnumUtils.IEnumValueGetter<Integer> {
      TRANSACTION_SUCCESSFULLY_COMPLETED(100, PaymentData.StatusEnum.APPROVED),
      ORDER_NOT_FOUND(105, PaymentData.StatusEnum.INVALID),
      GENERIC_ERROR(200, PaymentData.StatusEnum.VENDOR_FAILED),
      FAILED_TO_DEDUCT_MOLPOINTS(201, PaymentData.StatusEnum.VENDOR_FAILED),
      UNEXPECTED_ERROR_OCCURS_DURING_GENERATE_ORDER_ID(202, PaymentData.StatusEnum.VENDOR_FAILED),
      FAILED_TO_REGISTER_USER(203, PaymentData.StatusEnum.VENDOR_FAILED),
      FAILED_TO_RETRIEVE_USER_WALLET(204, PaymentData.StatusEnum.VENDOR_FAILED),
      PASSWORD_HASH_NOT_MATCH(205, PaymentData.StatusEnum.REJECTED),
      FAILED_TO_ACTIVATE_USER_ACCOUNT(206, PaymentData.StatusEnum.VENDOR_FAILED),
      FAILED_TO_RETRIEVE_TRANSACTION_SUMMARY(207, PaymentData.StatusEnum.VENDOR_FAILED),
      FAILED_TO_GENERATE_HEARTBEAT(208, PaymentData.StatusEnum.VENDOR_FAILED),
      DUPLICATION_EMAIL_FOUND(209, PaymentData.StatusEnum.REJECTED),
      FAILED_TO_UPDATE_OFFLINE_MESSAGE(210, PaymentData.StatusEnum.PENDING),
      INCOMPLETE_PARAMETERS_ON_SUBMISSION(400, PaymentData.StatusEnum.REJECTED),
      INVALID_PARAMETERS_ON_SUBMISSION(401, PaymentData.StatusEnum.REJECTED),
      INVALID_AMOUNT(403, PaymentData.StatusEnum.REJECTED),
      INVALID_SHA1_CHECKSUM(405, PaymentData.StatusEnum.REJECTED),
      INVALID_MERCHANT_ID(600, PaymentData.StatusEnum.REJECTED),
      REQUEST_ARE_NOT_SUBMITTED_FROM_THE_MERCHANT_IP_ADDRESS(601, PaymentData.StatusEnum.REJECTED),
      DUPLICATED_MERCHANT_REFERENCE_ID(602, PaymentData.StatusEnum.REJECTED),
      UNABLE_TO_DETERMINE_MERCHANT_ACCOUNT(603, PaymentData.StatusEnum.VENDOR_FAILED),
      INSUFFICIENT_BALANCE_TO_CONTINUE_THE_ORDER(800, PaymentData.StatusEnum.REJECTED),
      USER_SESSION_HAS_EXPIRED(801, PaymentData.StatusEnum.REJECTED),
      USER_HAS_CHOSEN_TO_CANCEL_THE_TRANSACTION(802, PaymentData.StatusEnum.CANCELLED),
      USER_IS_SPENDING_TOO_MUCH_TIME_AT_A_CERTAIN_PAGE(803, PaymentData.StatusEnum.REJECTED),
      EXPIRED_HEARTBEAT_VALUE(999, PaymentData.StatusEnum.REJECTED);

      private Integer code;
      private PaymentData.StatusEnum statusOnSyncUpdate;
      private static final HashMap<Integer, MOLPayment.MOLResultCodeEnum> lookUpMap = new HashMap();

      public int getCode() {
         return this.code;
      }

      public PaymentData.StatusEnum getStatusOnSyncUpdate() {
         return this.statusOnSyncUpdate;
      }

      private MOLResultCodeEnum(int code, PaymentData.StatusEnum onlineStatus) {
         this.code = code;
         this.statusOnSyncUpdate = onlineStatus;
      }

      public Integer getEnumValue() {
         return this.code;
      }

      public static MOLPayment.MOLResultCodeEnum getMOLResult(int resCode) {
         return (MOLPayment.MOLResultCodeEnum)lookUpMap.get(resCode);
      }

      static {
         EnumUtils.populateLookUpMap(lookUpMap, MOLPayment.MOLResultCodeEnum.class);
      }
   }
}
