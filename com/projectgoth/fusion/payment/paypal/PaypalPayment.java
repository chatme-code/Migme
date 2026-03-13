package com.projectgoth.fusion.payment.paypal;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.HttpClientUtils;
import com.projectgoth.fusion.common.JSONUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedDistributedLock;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.PaymentUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
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
import com.projectgoth.fusion.payment.ratelimit.PaymentRateLimiter;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.CreateException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class PaypalPayment implements PaymentInterface {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PaypalPayment.class));
   private static final Charset UTF_8 = Charset.forName("UTF-8");
   private static final String PAYPAL_PAYMENT_ERROR_MESSAGE = "Unable to continue with paypal payment. Please contact merchant@mig.me for more details.";
   private static final PaypalPayment INSTANCE = new PaypalPayment();
   private static final PaymentRateLimiter paymentRateLimiter;

   private PaypalPayment() {
   }

   public static PaypalPayment getInstance() {
      return INSTANCE;
   }

   public Map<String, Object> clientInitiatePayment(JSONObject paymentDetails) throws PaymentException, Exception {
      throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNSUPPORTED_PAYMENT_METHOD, new Object[0]);
   }

   public Map<String, Object> updatePaymentStatus(JSONObject paymentDetails) throws Exception {
      throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNSUPPORTED_PAYMENT_METHOD, new Object[0]);
   }

   public <T extends PaymentData> PaypalPaymentIResponse clientInitiatePayment(T pData) throws PaymentException, Exception {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_RateLimit.ENABLED)) {
         String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_RateLimit.INITIATE_PAYMENT_ACCESS_RATELIMIT);

         try {
            MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.INITIATE_PAYPAL_PAYMENT.toString(), String.valueOf(pData.userId), rateLimit);
         } catch (MemCachedRateLimiter.LimitExceeded var16) {
            log.warn(String.format("user:%s has reached the INITIATE_PAYPAL_PAYMENT rate limit with rate limit %s ", pData.userId, rateLimit));
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"login attempts"});
         } catch (MemCachedRateLimiter.FormatError var17) {
            log.error("Formatting error in rate limiter expression", var17);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         }
      }

      PaypalPaymentIResponse resp = new PaypalPaymentIResponse();

      try {
         PaypalPaymentData paymentData = (PaypalPaymentData)pData;
         this.preValidate(paymentData);
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userEJB.loadUserByUsernameOrAlias(paymentData.username, false, false);
         if (userData == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[]{paymentData.username});
         } else if (!this.isAccessAllowed(userData)) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.NO_ACCESS_TO_PAYMENT_VENDOR, new Object[]{paymentData.username, PaymentData.TypeEnum.PAYPAL.displayName()});
         } else if (!isWhiteListed(userData.username) && isBlackListed(userData, paymentData.accountEntrySource.ipAddress, (String)null)) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.NO_ACCESS_TO_PAYMENT_VENDOR, new Object[]{paymentData.username, PaymentData.TypeEnum.PAYPAL.displayName()});
         } else {
            AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            Double thresholdUSD = Restrictions.getMinimumAmount(userData.type);
            Double thresholdAmount = accountEJB.convertCurrency(thresholdUSD, "USD", paymentData.currency);
            if (paymentData.amount < thresholdAmount) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.PURCHASE_AMOUNT_BELOW_MINIMUM, new Object[]{paymentData.currency, PaymentUtils.formatAmountInCurrency(thresholdAmount)});
            } else {
               thresholdUSD = Restrictions.getMaximumAmount(userData.type);
               thresholdAmount = accountEJB.convertCurrency(thresholdUSD, "USD", paymentData.currency);
               if (paymentData.amount > thresholdAmount) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.PURCHASE_AMOUNT_ABOVE_MAXIMUM, new Object[]{paymentData.currency, PaymentUtils.formatAmountInCurrency(thresholdAmount)});
               } else {
                  Double amountUSD = paymentData.amount;
                  if (!paymentData.currency.toUpperCase().equals("USD")) {
                     amountUSD = accountEJB.convertCurrency(paymentData.amount, paymentData.currency, "USD");
                  }

                  this.checkTransactionRateLimit(userData, paymentData, amountUSD);
                  String returnUrl = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.RETURN_URL);
                  String cancelUrl = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.CANCEL_URL);
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.CLIENT_DEFINED_RETURN_URLS_ENABLED)) {
                     if (!StringUtil.isBlank(paymentData.returnUrl)) {
                        if (log.isDebugEnabled()) {
                           log.debug("Overriding paypal return url with: " + paymentData.returnUrl);
                        }

                        returnUrl = paymentData.returnUrl;
                     }

                     if (!StringUtil.isBlank(paymentData.cancelUrl)) {
                        if (log.isDebugEnabled()) {
                           log.debug("Overriding paypal cancel url with: " + paymentData.cancelUrl);
                        }

                        cancelUrl = paymentData.cancelUrl;
                     }
                  }

                  returnUrl = URLEncoder.encode(returnUrl, UTF_8.name());
                  cancelUrl = URLEncoder.encode(cancelUrl, UTF_8.name());
                  JSONObject paypalResponse = setExpressCheckout(paymentData.amount, paymentData.currency, paymentData.returnUrl, paymentData.cancelUrl);
                  String paypalToken = paypalResponse.getString("TOKEN");
                  insertTransactionTokenToCache(paypalToken, userData.username, paymentData.currency, paymentData.amount, (Integer)null, (String)null, new Timestamp(System.currentTimeMillis()));
                  resp.token = paypalToken;
                  resp.paymentData = paymentData;
                  return resp;
               }
            }
         }
      } catch (CreateException var14) {
         log.error("unable to create beans", var14);
         throw var14;
      } catch (PaymentException var15) {
         log.error("error occurred in initiating payment", var15);
         throw var15;
      }
   }

   public <T extends PaymentData> PaypalPaymentIResponse updatePaymentStatus(T pData) throws PaymentException, Exception {
      try {
         PaypalPaymentData updPaymentDataRequest = (PaypalPaymentData)pData;
         this.preValidate(updPaymentDataRequest);
         if (StringUtil.isBlank(updPaymentDataRequest.action)) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"updPaymentDataRequest.action"});
         } else {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            PaypalTransactionTokenData storedTokenData = getTransactionTokenDataFromCache(updPaymentDataRequest.token, now);
            if (storedTokenData == null) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.ILLEGAL_TRANSACTION, new Object[]{"You do not have an active payment."});
            } else if (StringUtil.isBlank(storedTokenData.paypalUserID)) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"storedTokenData.paypalUserID"});
            } else {
               AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
               PaypalPaymentData storedPaypalPaymentData = (PaypalPaymentData)accountEJB.getPaymentById(storedTokenData.paymentID);
               if (StringUtil.isBlank(storedPaypalPaymentData.paypalAccount)) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"storedPaypalPaymentData.paypalAccount"});
               } else {
                  updPaymentDataRequest.paypalAccount = storedPaypalPaymentData.paypalAccount;
                  UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  UserData userData = userEJB.loadUserByUsernameOrAlias(updPaymentDataRequest.username, false, false);
                  if (userData == null) {
                     throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[0]);
                  } else if (userData.userID != storedPaypalPaymentData.userId) {
                     throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISMATCH_USER_INFORMATION, new Object[0]);
                  } else {
                     boolean userHasAccess = this.isAccessAllowed(userData);
                     if (!userHasAccess) {
                        throw new PaymentException(ErrorCause.PaymentErrorReasonType.NO_ACCESS_TO_PAYMENT_VENDOR, new Object[]{updPaymentDataRequest.username, PaymentData.TypeEnum.PAYPAL.displayName()});
                     } else {
                        Double amountUSD = updPaymentDataRequest.amount;
                        if (!updPaymentDataRequest.currency.equalsIgnoreCase("USD")) {
                           amountUSD = accountEJB.convertCurrency(updPaymentDataRequest.amount, updPaymentDataRequest.currency, "USD");
                        }

                        List hits;
                        if (updPaymentDataRequest.action.equalsIgnoreCase("CONFIRM")) {
                           hits = this.preallocateRateLimit(userEJB, updPaymentDataRequest, updPaymentDataRequest.paypalAccount, amountUSD);

                           try {
                              boolean confirmed = false;
                              JSONObject paypalConfirmationResponse = this.confirmPayment(updPaymentDataRequest.token, updPaymentDataRequest.paypalAccount, updPaymentDataRequest.amount, updPaymentDataRequest.currency);
                              String paypalConfirmationResponseStatus = JSONUtils.getString(paypalConfirmationResponse, "status");
                              if (paypalConfirmationResponseStatus != null && paypalConfirmationResponseStatus.equalsIgnoreCase("ERROR")) {
                                 updPaymentDataRequest.status = PaymentData.StatusEnum.VENDOR_FAILED;
                              } else {
                                 confirmed = true;
                                 JSONObject queryTransactionDetailsResponse = this.getPaypalTransactionDetails(updPaymentDataRequest.token);
                                 String vendorTransactionID = JSONUtils.getString(queryTransactionDetailsResponse, "PAYMENTREQUEST_0_TRANSACTIONID");
                                 if (StringUtil.isBlank(vendorTransactionID)) {
                                    throw new PaymentException(ErrorCause.PaymentErrorReasonType.PAYMENT_STILL_PENDING_IN_VENDOR, new Object[]{"Paypal"});
                                 }

                                 updPaymentDataRequest.vendorTransactionId = vendorTransactionID;
                                 updPaymentDataRequest.status = PaymentData.StatusEnum.APPROVED;
                              }

                              this.releaseRateLimitPreallocation(confirmed, hits);
                           } catch (Exception var25) {
                              this.releaseRateLimitPreallocation(false, hits);
                              if (var25 instanceof PaymentException) {
                                 PaymentException pex = (PaymentException)var25;
                                 throw pex;
                              }

                              log.error("Error confirming transaction ", var25);
                              throw new PaymentException(var25, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
                           }
                        } else if (updPaymentDataRequest.action.equalsIgnoreCase("CANCEL")) {
                           updPaymentDataRequest.status = PaymentData.StatusEnum.CANCELLED;
                        } else {
                           updPaymentDataRequest.status = PaymentData.StatusEnum.PENDING;
                        }

                        hits = null;
                        updPaymentDataRequest.assignUpdatedTime(new Timestamp(System.currentTimeMillis()));

                        try {
                           PaymentData storedPaymentData;
                           try {
                              storedPaymentData = accountEJB.updatePayment(updPaymentDataRequest, updPaymentDataRequest.accountEntrySource);
                              log.info("Successfully update transaction id " + storedPaymentData.id + " status:" + storedPaymentData.status + ".");
                           } catch (PaymentException var23) {
                              if (var23.getErrorCause() == ErrorCause.PaymentErrorReasonType.DB_UPDATE_PAYMENT_FAILED) {
                                 storedPaymentData = accountEJB.getPaymentById(updPaymentDataRequest.id);
                                 log.info("Unsuccessful status update to Transaction id " + storedPaymentData.id + " status is already :" + storedPaymentData.status + ".");
                              }
                           }
                        } finally {
                           if (updPaymentDataRequest.status != PaymentData.StatusEnum.PENDING) {
                              deleteTransactionToken(updPaymentDataRequest.token);
                           }

                        }

                        PaypalPaymentIResponse resp = new PaypalPaymentIResponse();
                        resp.token = updPaymentDataRequest.token;
                        resp.amount = updPaymentDataRequest.amount;
                        resp.currency = updPaymentDataRequest.currency;
                        resp.amountUSD = amountUSD;
                        resp.result = updPaymentDataRequest.status.name();
                        return resp;
                     }
                  }
               }
            }
         }
      } catch (CreateException var26) {
         log.error("Create exception in updating paypal transaction.", var26);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      }
   }

   private static void logOutcomes(String url, String xmlRequestData, String xmlResponseData, Exception ex) {
      log.error("URI:[" + url + "]\nRequest:[" + xmlRequestData + "]\nResponse:[" + xmlResponseData + "]" + "\nException:[" + ex + "]");
   }

   public static JSONObject setExpressCheckout(double amount, String currency, String returnUrl, String cancelUrl) throws PaymentException {
      try {
         String request = "PAYMENTREQUEST_0_AMT=" + String.valueOf(amount) + "&PAYMENTREQUEST_0_PAYMENTACTION=Sale" + "&RETURNURL=" + returnUrl + "&CANCELURL=" + cancelUrl + "&PAYMENTREQUEST_0_CURRENCYCODE=" + currency + "&REQCONFIRMSHIPPING=0" + "&NOSHIPPING=1" + "&L_PAYMENTREQUEST_0_NAME0=migme credits" + "&L_PAYMENTREQUEST_0_AMT0=" + amount + "&L_PAYMENTREQUEST_0_ITEMCATEGORY0=Digital" + "&METHOD=SETEXPRESSCHECKOUT" + "&USER=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.USERNAME) + "&PWD=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.PASSWORD) + "&SIGNATURE=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.SIGNATURE) + "&VERSION=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.API_VERSION);
         String response = dispatchPaypalRequest(request);
         JSONObject jsonResponse = processResponseFromAPI(response);
         if (jsonResponse.has("L_ERRORCODE0")) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{jsonResponse.get("L_LONGMESSAGE0")});
         } else {
            return jsonResponse;
         }
      } catch (UnsupportedEncodingException var8) {
         log.error("malformed url in sending paypal request", var8);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Unable to continue with paypal payment. Please contact merchant@mig.me for more details."});
      } catch (JSONException var9) {
         log.error("Unable to digest response form paypal", var9);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      } catch (IOException var10) {
         log.error("Error in sending paypal request", var10);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      }
   }

   public PaypalPaymentIResponse onPaymentAuthorized(String token, JSONObject params) throws PaymentException {
      if (StringUtil.isBlank(token)) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"Token"});
      } else {
         try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            PaypalTransactionTokenData storedTokenData = getTransactionTokenDataFromCache(token, now);
            if (storedTokenData == null) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.ILLEGAL_TRANSACTION, new Object[]{"You do not have an active payment."});
            } else {
               MemCachedDistributedLock.LockInstance tokenTransactionLock = MemCachedDistributedLock.getDistributedLock(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN_LOCK, token);
               if (tokenTransactionLock == null) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[]{"Timeout while trying to process token"});
               } else {
                  PaypalPaymentIResponse var29;
                  try {
                     storedTokenData = getTransactionTokenDataFromCache(token, now);
                     if (storedTokenData == null) {
                        throw new PaymentException(ErrorCause.PaymentErrorReasonType.ILLEGAL_TRANSACTION, new Object[]{"You do not have an active payment."});
                     }

                     JSONObject paypalTokenDetailsResponse = this.getPaypalTransactionDetails(token);
                     String currency = storedTokenData.currency;
                     double amount = storedTokenData.amount;
                     String username = storedTokenData.username;
                     String paypalUserID = paypalTokenDetailsResponse.getString("PAYERID");
                     double usdAmount = storedTokenData.usdAmount;
                     if (storedTokenData.paymentID == null) {
                        UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                        UserData userData = userEJB.loadUserByUsernameOrAlias(username, false, false);
                        if (userData == null) {
                           throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[0]);
                        }

                        PaypalPaymentData paymentData = new PaypalPaymentData();
                        paymentData.assignCreatedTime(new Timestamp(System.currentTimeMillis()));
                        paymentData.assignUpdatedTime((Date)null);
                        paymentData.username = userData.username;
                        paymentData.userId = userData.userID;
                        paymentData.amount = amount;
                        paymentData.currency = currency;
                        paymentData.paypalAccount = paypalUserID;
                        AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
                        if (!currency.toUpperCase().equals("USD")) {
                           usdAmount = accountEJB.convertCurrency(amount, currency, "USD");
                        } else {
                           usdAmount = amount;
                        }

                        this.checkTransactionRateLimit(userData, paymentData, usdAmount);
                        this.checkPaypalAccountRateLimit(userData, paymentData, paymentData.paypalAccount);
                        paymentData = (PaypalPaymentData)accountEJB.createPayment(paymentData);
                        storedTokenData.paymentID = paymentData.id;
                        storedTokenData.paypalUserID = paypalUserID;
                        storedTokenData.usdAmount = usdAmount;
                        now = new Timestamp(System.currentTimeMillis());
                        updateTransactionToken(token, storedTokenData, now);
                     }

                     PaypalPaymentIResponse mthdResponse = new PaypalPaymentIResponse();
                     mthdResponse.amount = amount;
                     mthdResponse.amountUSD = usdAmount;
                     mthdResponse.currency = currency;
                     mthdResponse.token = token;
                     var29 = mthdResponse;
                  } finally {
                     tokenTransactionLock.release(false);
                  }

                  return var29;
               }
            }
         } catch (CreateException var25) {
            log.error("Error occurred in retrieving paypal payment details", var25);
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
         } catch (PaymentException var26) {
            log.error("Payment exception occurred.", var26);
            throw var26;
         } catch (Exception var27) {
            log.error("Malformed response from paypal", var27);
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_VENDOR_DATA, new Object[]{"Unable to continue with paypal payment. Please contact merchant@mig.me for more details."});
         }
      }
   }

   private JSONObject getPaypalTransactionDetails(String token) throws PaymentException {
      try {
         String request = "TOKEN=" + token + "&METHOD=GETEXPRESSCHECKOUTDETAILS" + "&USER=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.USERNAME) + "&PWD=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.PASSWORD) + "&SIGNATURE=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.SIGNATURE) + "&VERSION=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.API_VERSION);
         String response = dispatchPaypalRequest(request);
         JSONObject jsonResponse = processResponseFromAPI(response);
         if (jsonResponse.has("L_ERRORCODE0")) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{jsonResponse.get("L_LONGMESSAGE0")});
         } else {
            return jsonResponse;
         }
      } catch (JSONException var5) {
         log.error("Malformed response from transaction details", var5);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Unable to continue with paypal payment. Please contact merchant@mig.me for more details."});
      } catch (UnsupportedEncodingException var6) {
         log.error("malformed url in sending paypal request", var6);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Unable to continue with paypal payment. Please contact merchant@mig.me for more details."});
      } catch (IOException var7) {
         log.error("Error while sending paypal request", var7);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      }
   }

   public JSONObject confirmPayment(String token, String userID, double amount, String currency) throws PaymentException {
      try {
         String request = "&TOKEN=" + token + "&PAYERID=" + userID + "&PAYMENTREQUEST_0_PAYMENTACTION=Sale" + "&PAYMENTREQUEST_0_AMT=" + amount + "&PAYMENTREQUEST_0_CURRENCYCODE=" + currency + "&METHOD=DOEXPRESSCHECKOUTPAYMENT" + "&USER=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.USERNAME) + "&PWD=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.PASSWORD) + "&SIGNATURE=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.SIGNATURE) + "&VERSION=" + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.API_VERSION);
         String response = dispatchPaypalRequest(request);
         JSONObject jsonResponse = processResponseFromAPI(response);

         try {
            JSONObject jsonDetails = new JSONObject();
            if (jsonResponse.has("ACK") && jsonResponse.getString("ACK").equalsIgnoreCase("SUCCESS")) {
               jsonDetails.put("amount", jsonResponse.getDouble("PAYMENTINFO_0_AMT"));
               jsonDetails.put("token", token);
               jsonDetails.put("currency", jsonResponse.getString("PAYMENTINFO_0_CURRENCYCODE"));
               jsonDetails.put("status", jsonResponse.getString("PAYMENTINFO_0_PAYMENTSTATUS"));
               return jsonDetails;
            } else {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{jsonResponse.get("L_LONGMESSAGE0")});
            }
         } catch (JSONException var10) {
            log.error("Malformed response from paypal", var10);
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_VENDOR_DATA, new Object[]{"Unable to continue with paypal payment. Please contact mercahnt@mig.me."});
         }
      } catch (JSONException var11) {
         log.error("malformed url in sending paypal request", var11);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Unable to continue with paypal payment. Please contact merchant@mig.me for more details."});
      } catch (UnsupportedEncodingException var12) {
         log.error("malformed url in sending paypal request", var12);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Unable to continue with paypal payment. Please contact merchant@mig.me for more details."});
      } catch (IOException var13) {
         log.error("malformed url in sending paypal request", var13);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      }
   }

   public String getCurrencyForUser(UserData userData) {
      String[] supportedCurrencies = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.SUPPORTED_CURRENCIES);
      String[] arr$ = supportedCurrencies;
      int len$ = supportedCurrencies.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String currency = arr$[i$];
         currency = PaymentUtils.normalizeCurrency(currency);
         if (currency.equalsIgnoreCase(userData.currency)) {
            return currency;
         }
      }

      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.DEFAULT_CURRENCY);
   }

   public boolean isAccessAllowed(UserData userData) throws PaymentException, Exception {
      if (userData == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[0]);
      } else if (userData.type == UserData.TypeEnum.MIG33_TOP_MERCHANT && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.NON_TOP_MERCHANTS_ACCESS_ONLY_ENABLED)) {
         return false;
      } else {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.ENABLED_TO_ALL_USERS)) {
            try {
               AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
               if (!accountEJB.isPaymentAllowedToUser(userData.userID, GuardCapabilityEnum.PAYPAL_PAYMENT_ALLOWED.value())) {
                  return false;
               }
            } catch (CreateException var3) {
               log.error("Unable to get guardset for PAYPAL Payment.", var3);
               throw new Exception("Unable to continue with payment.");
            } catch (FusionEJBException var4) {
               log.error("Unable to get guardset for PAYPAL Payment: ", var4);
               throw new Exception("Unable to continue with payment.");
            }
         }

         return true;
      }
   }

   public boolean isBelowLimits(String username) {
      return true;
   }

   private static String getTransactionTokenLocalKey(String tokenStr) {
      return StringUtil.trimmedUpperCase(tokenStr);
   }

   public static PaypalTransactionTokenData insertTransactionTokenToCache(String token, String username, String currency, double amount, Integer paymentId, String paypalUserID, Date currentDate) throws JSONException {
      String tokenLocalKey = getTransactionTokenLocalKey(token);
      long tokenTTL = (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.EXPRESS_CHECKOUT_SESSION_TIMEOUT_MILLIS);
      long createTs = currentDate.getTime();
      long expireTs = createTs + tokenTTL;
      PaypalTransactionTokenData transactionTokenData = new PaypalTransactionTokenData();
      transactionTokenData.currency = currency;
      transactionTokenData.username = username;
      transactionTokenData.amount = amount;
      transactionTokenData.paymentID = paymentId;
      transactionTokenData.paypalUserID = paypalUserID;
      transactionTokenData.createDate = currentDate;
      transactionTokenData.expireDate = new Timestamp(expireTs);
      String tokenDataStr = transactionTokenData.toJSONString();
      log.info("Inserting " + MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey) + " => [" + tokenDataStr + "] TTL:[" + tokenTTL + "] ms");
      if (!MemCachedClientWrapper.add(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey, tokenDataStr, tokenTTL)) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_VENDOR_DATA, new Object[]{"Duplicate token received from PAYPAL. Please try again."});
      } else {
         return transactionTokenData;
      }
   }

   public static void deleteTransactionToken(String token) {
      String tokenLocalKey = getTransactionTokenLocalKey(token);
      log.info("Deleting " + MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey));
      MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey);
   }

   public static PaypalTransactionTokenData updateTransactionToken(String token, PaypalTransactionTokenData tokenData, Date currentDate) throws JSONException, ParseException {
      if (tokenData != null) {
         String tokenLocalKey = getTransactionTokenLocalKey(token);
         long tokenMaxTTL = (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.EXPRESS_CHECKOUT_SESSION_TIMEOUT_MILLIS);
         long newTokenTTL = tokenData.expireDate.getTime() - currentDate.getTime();
         if (newTokenTTL > 0L) {
            if (newTokenTTL > tokenMaxTTL) {
               newTokenTTL = tokenMaxTTL;
               tokenData.expireDate = new Timestamp(tokenData.createDate.getTime() + tokenMaxTTL);
            }

            String storedTokenDataStr = tokenData.toJSONString();
            log.info("Updating " + MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey) + " => [" + storedTokenDataStr + "] new TTL:[" + newTokenTTL + "] ms");
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey, storedTokenDataStr, newTokenTTL);
         } else {
            log.error("Updating " + MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey) + " cancelled due to TTL has reached.");
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey);
         }
      }

      return tokenData;
   }

   public static PaypalTransactionTokenData getTransactionTokenDataFromCache(String token, Date currentDate) throws JSONException, ParseException {
      String tokenLocalKey = getTransactionTokenLocalKey(token);
      String storedTokenDataStr = (String)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey);
      if (storedTokenDataStr == null) {
         log.error("PAYPAL Token:" + token + " not in memcached.");
         return null;
      } else {
         PaypalTransactionTokenData storedTokenData = PaypalTransactionTokenData.createFromJSONString(storedTokenDataStr);
         if (currentDate.after(storedTokenData.expireDate)) {
            log.error("PAYPAL Token: " + token + " expireDate: [" + storedTokenData.expireDate + "], current action time [" + currentDate + "]. Will delete it from cache.");
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.PAYPAL_TXN, tokenLocalKey);
            return null;
         } else {
            return storedTokenData;
         }
      }
   }

   private static JSONObject processResponseFromAPI(String response) throws JSONException, UnsupportedEncodingException {
      String decodedResponse = URLDecoder.decode(response, UTF_8.name());
      JSONObject jsonResponse = new JSONObject();
      String[] keyValPairs = decodedResponse.split("&");
      String[] arr$ = keyValPairs;
      int len$ = keyValPairs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String responseElement = arr$[i$];
         String[] elems = responseElement.split("=");
         jsonResponse.put(elems[0], elems[1]);
      }

      return jsonResponse;
   }

   public static boolean isWhiteListed(String username) {
      return SystemProperty.isValueInArray(username, SystemPropertyEntities.Payments_PAYPAL.WHITELIST_USERNAME);
   }

   public static boolean isBlackListed(UserData user, String ipAddress, String paypalAccount) {
      if (user != null && SystemProperty.isValueInArray(user.username, SystemPropertyEntities.Payments_PAYPAL.BLACKLIST_USERNAME)) {
         return true;
      } else if (user != null && SystemProperty.isValueInIntegerArray(user.countryID, SystemPropertyEntities.Payments_PAYPAL.BLACKLIST_COUNTRY)) {
         return true;
      } else if (ipAddress != null && SystemProperty.isValueInArray(ipAddress, SystemPropertyEntities.Payments_PAYPAL.BLACKLIST_IP_ADDRESS)) {
         return true;
      } else {
         return paypalAccount != null && SystemProperty.isValueInArray(paypalAccount, SystemPropertyEntities.Payments_PAYPAL.BLACKLIST_PAYPAL_ACCOUNT);
      }
   }

   public void releaseRateLimitPreallocation(boolean doConfirm, List<PaymentRateLimiter.HitInstance> hitList) {
      Iterator i$ = hitList.iterator();

      while(i$.hasNext()) {
         PaymentRateLimiter.HitInstance hit = (PaymentRateLimiter.HitInstance)i$.next();
         hit.end(doConfirm);
      }

   }

   public List<PaymentRateLimiter.HitInstance> preallocateRateLimit(UserLocal userEJB, PaypalPaymentData paymentData, String paypalUserID, double amountUSD) throws PaymentException {
      long preallocationLife = 120000L;
      List<PaymentRateLimiter.HitInstance> hitList = new ArrayList();
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_RateLimit.ENABLED)) {
         return hitList;
      } else {
         UserData userData = userEJB.loadUserByUsernameOrAlias(paymentData.username, false, false);
         if (userData == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[0]);
         } else {
            long amountValue = (long)amountUSD * 10000L;

            try {
               PaymentRateLimiter.HitInstance hit = paymentRateLimiter.beginHit(PaypalInitialValueProviders.AllCountPerCountry.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerCountryRateLimiter(userData), 1L, (Long)PaypalInitialValueProviders.AllCountPerCountry.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Long.class), preallocationLife, PaypalInitialValueProviders.AllCountPerCountry.INSTANCE, new PaypalInitialValueProviders.AllCountPerCountry.Param(userData.countryID, userData.type));
               if (hit != null) {
                  hitList.add(hit);
                  hit = paymentRateLimiter.beginHit(PaypalInitialValueProviders.AllValuePerCountry.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerCountryRateLimiter(userData), amountValue, (long)((Double)PaypalInitialValueProviders.AllValuePerCountry.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Double.class) * 10000.0D), preallocationLife, PaypalInitialValueProviders.AllValuePerCountry.INSTANCE, new PaypalInitialValueProviders.AllValuePerCountry.Param(userData.countryID, userData.type));
                  if (hit != null) {
                     hitList.add(hit);
                     hit = paymentRateLimiter.beginHit(PaypalInitialValueProviders.AllCountPerUserType.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerUserTypeRateLimiter(userData.type), 1L, (Long)PaypalInitialValueProviders.AllCountPerUserType.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Long.class), preallocationLife, PaypalInitialValueProviders.AllCountPerUserType.INSTANCE, userData.type);
                     if (hit != null) {
                        hitList.add(hit);
                        hit = paymentRateLimiter.beginHit(PaypalInitialValueProviders.AllValuePerUserType.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerUserTypeRateLimiter(userData.type), amountValue, (long)((Double)PaypalInitialValueProviders.AllValuePerUserType.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Double.class) * 10000.0D), preallocationLife, PaypalInitialValueProviders.AllValuePerUserType.INSTANCE, userData.type);
                        if (hit != null) {
                           hitList.add(hit);
                           hit = paymentRateLimiter.beginHit(PaypalInitialValueProviders.AllValuePerUserID.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerUserIDRateLimiter(userData), amountValue, (long)((Double)PaypalInitialValueProviders.AllValuePerUserID.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Double.class) * 10000.0D), preallocationLife, PaypalInitialValueProviders.AllValuePerUserID.INSTANCE, userData.userID);
                           if (hit != null) {
                              hitList.add(hit);
                              hit = paymentRateLimiter.beginHit(PaypalInitialValueProviders.AllCountPerUserID.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerUserIDRateLimiter(userData), 1L, (Long)PaypalInitialValueProviders.AllCountPerUserID.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Long.class), preallocationLife, PaypalInitialValueProviders.AllCountPerUserID.INSTANCE, userData.userID);
                              if (hit != null) {
                                 hitList.add(hit);
                                 hit = paymentRateLimiter.beginHit(PaypalInitialValueProviders.AllCountPerPaypalUser.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerVendorUserIDRateLimiter(paypalUserID), 1L, (Long)PaypalInitialValueProviders.AllCountPerPaypalUser.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Long.class), preallocationLife, PaypalInitialValueProviders.AllCountPerPaypalUser.INSTANCE, paypalUserID);
                                 if (hit != null) {
                                    hitList.add(hit);
                                    return hitList;
                                 } else {
                                    log.warn("Paypal rate limit exceeded;limit type=[total transaction count];scope=[per user paypalUserID];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "];paypalUserID=[" + paypalUserID + "]");
                                    throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"number of requests"});
                                 }
                              } else {
                                 log.warn("Paypal rate limit exceeded;limit type=[total transaction count];scope=[per user ID];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
                                 throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"number of requests"});
                              }
                           } else {
                              log.warn("Paypal rate limit exceeded;limit type=[total transaction amount];scope=[per user ID];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
                              throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"amount"});
                           }
                        } else {
                           log.warn("Paypal rate limit exceeded;limit type=[total transaction amount];scope=[per user type];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
                           throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"amount"});
                        }
                     } else {
                        log.warn("Paypal rate limit exceeded;limit type=[total transaction count];scope=[per user type];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
                        throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"number of requests"});
                     }
                  } else {
                     log.warn("Paypal rate limit exceeded;limit type=[total transaction amount];scope=[per country,per user type];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
                     throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"amount"});
                  }
               } else {
                  log.warn("Paypal rate limit exceeded;limit type=[total transaction count];scope=[per country,per user type];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"number of requests"});
               }
            } catch (Exception var15) {
               this.releaseRateLimitPreallocation(false, hitList);
               if (var15 instanceof PaymentException) {
                  PaymentException pex = (PaymentException)var15;
                  log.warn("beginHit:" + pex.getMessage());
                  throw pex;
               } else {
                  log.error("beginHit error", var15);
                  throw new PaymentException(var15, ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
               }
            }
         }
      }
   }

   public void checkTransactionRateLimit(UserData userData, PaypalPaymentData paymentData, double amountUSD) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_RateLimit.ENABLED)) {
         long amountValue = (long)amountUSD * 10000L;
         if (paymentRateLimiter.checkWithoutHit(PaypalInitialValueProviders.AllCountPerCountry.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerCountryRateLimiter(userData), 1L, (Long)PaypalInitialValueProviders.AllCountPerCountry.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Long.class), PaypalInitialValueProviders.AllCountPerCountry.INSTANCE, new PaypalInitialValueProviders.AllCountPerCountry.Param(userData.countryID, userData.type))) {
            log.warn("Paypal rate limit (check without hit);limit type=[total transaction count];scope=[per country,per user type];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"number of requests"});
         } else if (paymentRateLimiter.checkWithoutHit(PaypalInitialValueProviders.AllValuePerCountry.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerCountryRateLimiter(userData), amountValue, (long)((Double)PaypalInitialValueProviders.AllValuePerCountry.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Double.class) * 10000.0D), PaypalInitialValueProviders.AllValuePerCountry.INSTANCE, new PaypalInitialValueProviders.AllValuePerCountry.Param(userData.countryID, userData.type))) {
            log.warn("Paypal rate limit (check without hit);limit type=[total transaction value];scope=[per country,per user type];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"amount"});
         } else if (paymentRateLimiter.checkWithoutHit(PaypalInitialValueProviders.AllCountPerUserType.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerUserTypeRateLimiter(userData.type), 1L, (Long)PaypalInitialValueProviders.AllCountPerUserType.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Long.class), PaypalInitialValueProviders.AllCountPerUserType.INSTANCE, userData.type)) {
            log.warn("Paypal rate limit (check without hit);limit type=[total transaction count];scope=[per user type];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"number of requests"});
         } else if (paymentRateLimiter.checkWithoutHit(PaypalInitialValueProviders.AllValuePerUserType.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerUserTypeRateLimiter(userData.type), amountValue, (long)((Double)PaypalInitialValueProviders.AllValuePerUserType.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Double.class) * 10000.0D), PaypalInitialValueProviders.AllValuePerUserType.INSTANCE, userData.type)) {
            log.warn("Paypal rate limit (check without hit);limit type=[total transaction value];scope=[per user type];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"amount"});
         } else if (paymentRateLimiter.checkWithoutHit(PaypalInitialValueProviders.AllValuePerUserID.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerUserIDRateLimiter(userData), amountValue, (long)((Double)PaypalInitialValueProviders.AllValuePerUserID.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Double.class) * 10000.0D), PaypalInitialValueProviders.AllValuePerUserID.INSTANCE, userData.userID)) {
            log.warn("Paypal rate limit (check without hit);limit type=[total transaction value];scope=[per userid];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"amount"});
         } else if (paymentRateLimiter.checkWithoutHit(PaypalInitialValueProviders.AllCountPerUserID.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerUserIDRateLimiter(userData), 1L, (Long)PaypalInitialValueProviders.AllCountPerUserID.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Long.class), PaypalInitialValueProviders.AllCountPerUserID.INSTANCE, userData.userID)) {
            log.warn("Paypal rate limit (check without hit);limit type=[total transaction count];scope=[per userid];countryID=[" + userData.countryID + "];userType=[" + userData.type + "];userID=[" + userData.userID + "]");
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"number of requests"});
         }
      }
   }

   private void checkPaypalAccountRateLimit(UserData userData, PaypalPaymentData paymentData, String paypalUserID) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_RateLimit.ENABLED)) {
         if (userData == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_USER, new Object[0]);
         } else if (paymentRateLimiter.checkWithoutHit(PaypalInitialValueProviders.AllCountPerPaypalUser.INSTANCE.getRateLimitKeySpace(), getLocalKeyForPerVendorUserIDRateLimiter(paypalUserID), 1L, (Long)PaypalInitialValueProviders.AllCountPerPaypalUser.INSTANCE.getRateLimitKeySpace().getUserRateLimitValue(userData.type, Long.class), PaypalInitialValueProviders.AllCountPerPaypalUser.INSTANCE, paypalUserID)) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RATE_LIMIT_EXCEEDED, new Object[]{"number of requests"});
         }
      }
   }

   private static String getLocalKeyForPerCountryRateLimiter(UserData userData) {
      int countryID = userData.countryID == null ? -1 : userData.countryID;
      return MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(countryID), String.valueOf(userData.type.value()));
   }

   private static String getLocalKeyForPerUserIDRateLimiter(UserData userData) {
      int userID = userData.userID == null ? -1 : userData.userID;
      return MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(userID));
   }

   private static String getLocalKeyForPerUserTypeRateLimiter(UserData.TypeEnum userType) {
      return userType == null ? "" : String.valueOf(userType.value());
   }

   private static String getLocalKeyForPerVendorUserIDRateLimiter(String paypalUserID) {
      if (StringUtil.isBlank(paypalUserID)) {
         return "";
      } else {
         String paypalUIDKey = paypalUserID.toLowerCase().trim();
         if (log.isDebugEnabled()) {
            log.debug("PaypalUserID : " + paypalUserID + "->" + paypalUIDKey);
         }

         return paypalUIDKey;
      }
   }

   private void preValidate(PaypalPaymentData paymentData) throws PaymentException {
      if (StringUtil.isBlank(paymentData.username)) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"Username."});
      } else if (StringUtil.isBlank(paymentData.currency)) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"Currency."});
      } else if (paymentData.accountEntrySource == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"AccountEntrySource."});
      }
   }

   public static String dispatchPaypalRequest(String requestData) throws IOException, PaymentException {
      HttpClientUtils.HttpClientConfig clientConfig = new HttpClientUtils.HttpClientConfig();
      clientConfig.keepAliveInSecs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.KEEP_ALIVE_IN_SEC);
      clientConfig.maxConnections = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.MAX_CONNECTIONS);
      clientConfig.strictHttpsCertCheck = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.STRICT_HTTPS_CERT_CHECK);
      clientConfig.timeOutInMillis = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.TIME_OUT_IN_MILLIS);
      String responseData = null;
      String url = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.VENDOR_BASE_URL) + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.SET_EXPRESS_CHECKOUT_URL);
      boolean enableGatewayRequestResponseLogging = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.ENABLE_GATEWAY_REQUEST_RESPONSE_LOGGING);

      try {
         HttpPost postRequest = new HttpPost(url);
         postRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
         postRequest.setEntity(new StringEntity(requestData, UTF_8.name()));
         responseData = getResponseString(clientConfig, postRequest);
         if (enableGatewayRequestResponseLogging) {
            log.info("URI:[" + url + "]\nRequest:[" + requestData + "]\nResponse:[" + responseData + "]");
         }

         return responseData;
      } catch (PaymentException var7) {
         logOutcomes(url, requestData, responseData, var7);
         throw var7;
      } catch (Exception var8) {
         logOutcomes(url, requestData, responseData, var8);
         IOException ioEx = new IOException("Unable to complete request to vendor gateway");
         ioEx.initCause(var8);
         throw ioEx;
      }
   }

   private static String getResponseString(HttpClientUtils.HttpClientConfig clientConfig, HttpPost postRequest) throws PaymentException {
      String responseData;
      try {
         responseData = HttpClientUtils.getResponseString(clientConfig, postRequest, UTF_8);
      } catch (GeneralSecurityException var4) {
         log.error("security exception occured while making paypal payment", var4);
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"Unable to continue with paypal payment. Please contact merchant@mig.me for more details."});
      } catch (IOException var5) {
         log.error("unable to send request to paypal", var5);
         throw new PaymentException(var5, ErrorCause.PaymentErrorReasonType.FAILED_TO_RETRIEVE_RESPONSE_FROM_VENDOR, new Object[]{var5.getMessage()});
      }

      if (responseData == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_VENDOR_DATA, new Object[]{"Received a blank response"});
      } else {
         return responseData;
      }
   }

   public <T extends PaymentData> PaymentIResponse approve(T paymentDetails, String username) throws PaymentException, Exception {
      return null;
   }

   public <T extends PaymentData> PaymentIResponse reject(T paymentDetails, String username) throws PaymentException, Exception {
      return null;
   }

   static {
      paymentRateLimiter = new PaymentRateLimiter(MemCachedKeySpaces.RateLimitKeySpace.PAYMENT_RATE_LIMIT, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NAMESPACE);
   }
}
