package com.projectgoth.fusion.payment;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.JSONUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.payment.paypal.PaypalPayment;
import com.projectgoth.fusion.payment.paypal.PaypalTransactionTokenData;
import com.projectgoth.fusion.restapi.data.BooleanData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

@Provider
@Path("/payment")
public class PaymentResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PaymentResource.class));

   @GET
   @Path("/credit_card")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Vector> getCreditCardTransactions(@QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate, @QueryParam("sortBy") String sortBy, @QueryParam("sortOrder") String sortOrder, @QueryParam("showAuth") String showAuth, @QueryParam("showPend") String showPend, @QueryParam("showRej") String showRej, @QueryParam("username") String username, @QueryParam("displayLimit") @DefaultValue("30") int displayLimit) throws FusionRestException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         Vector transactions = accountEJB.getCreditCardTransactions(startDate, endDate, sortBy, sortOrder, showAuth, showPend, showRej, username, displayLimit);
         return new DataHolder(transactions);
      } catch (Exception var12) {
         log.error("Exception while calling getCreditCardTransactions", var12);
         throw new FusionRestException(101, var12.getMessage());
      }
   }

   @GET
   @Path("/credit_card/hml")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Vector> getCreditCardHMLTransactions(@QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate, @QueryParam("sortBy") String sortBy, @QueryParam("sortOrder") String sortOrder, @QueryParam("showAuth") String showAuth, @QueryParam("showPend") String showPend, @QueryParam("showRej") String showRej, @QueryParam("username") String username, @QueryParam("displayLimit") @DefaultValue("30") int displayLimit) throws FusionRestException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         Vector transactions = accountEJB.getCreditCardHMLTransactions(startDate, endDate, sortBy, sortOrder, showAuth, showPend, showRej, username, displayLimit);
         return new DataHolder(transactions);
      } catch (Exception var12) {
         log.error("Exception while calling getCreditCardTransactions", var12);
         throw new FusionRestException(101, var12.getMessage());
      }
   }

   public static boolean isPaymentVendorAccessibleFromViewType(PaymentData.TypeEnum paymentVendorTypeEnum, PaymentData.ViewEnum paymentViewTypeEnum) {
      SystemPropertyEntities.Payments enabledVendorsKeyEnum = SystemPropertyEntities.Payments.getSystemPropertyKeyForEnabledVendors(paymentViewTypeEnum.name());
      if (enabledVendorsKeyEnum != null) {
         String[] availablePaymentVendorList = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)enabledVendorsKeyEnum);
         String[] arr$ = availablePaymentVendorList;
         int len$ = availablePaymentVendorList.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String availablePaymentVendorStr = arr$[i$];
            PaymentData.TypeEnum availablePaymentVendorTypeEnum = PaymentData.TypeEnum.fromCode(availablePaymentVendorStr);
            if (availablePaymentVendorTypeEnum != null) {
               if (availablePaymentVendorTypeEnum == paymentVendorTypeEnum) {
                  return true;
               }
            } else {
               log.warn(availablePaymentVendorStr + " is not known! Consider amending or removing it from the system property key " + enabledVendorsKeyEnum);
            }
         }
      } else {
         log.info("Enabled-vendor-system property key for view type [" + paymentViewTypeEnum.name() + "] is not available/cannot be determined");
      }

      return false;
   }

   @PUT
   @Consumes({"application/json"})
   @Produces({"application/json"})
   @Path("/{paymentVendorType}")
   public DataHolder<Map<String, Object>> createPayment(@PathParam("paymentVendorType") String paymentVendorTypeStr, String jsonDataString) throws FusionRestException {
      try {
         boolean thirdPartyPaymentIsEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments.THIRD_PARTY_PAYMENT_ENABLED);
         if (!thirdPartyPaymentIsEnabled) {
            throw new FusionRestException(FusionRestException.RestException.THIRD_PARTY_PAYMENT_DISABLED);
         } else if (paymentVendorTypeStr == null) {
            throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Payment vendor type not supplied");
         } else {
            PaymentData.TypeEnum paymentVendorTypeEnum = PaymentData.TypeEnum.fromCode(paymentVendorTypeStr);
            if (paymentVendorTypeEnum == null) {
               throw new FusionRestException(FusionRestException.RestException.INVALID_PAYMENT_VENDOR, "Payment vendor type is not supported " + paymentVendorTypeStr);
            } else {
               JSONObject jsonObject = new JSONObject(jsonDataString);
               String paymentViewTypeCode = StringUtil.trimmedLowerCase(JSONUtils.getString(jsonObject, "view"));
               if (StringUtil.isBlank(paymentViewTypeCode)) {
                  throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Payment view type is blank");
               } else {
                  PaymentData.ViewEnum paymentViewTypeEnum = PaymentData.ViewEnum.fromName(paymentViewTypeCode);
                  if (paymentViewTypeEnum == null) {
                     throw new FusionRestException(FusionRestException.RestException.UNSUPPORTED_PAYMENT_VIEW_TYPE, "Payment view is unsupported " + paymentViewTypeCode);
                  } else {
                     boolean accessAllowed = isPaymentVendorAccessibleFromViewType(paymentVendorTypeEnum, paymentViewTypeEnum);
                     if (!accessAllowed) {
                        throw new FusionRestException(FusionRestException.RestException.UNSUPPORTED_PAYMENT_VIEW_TYPE, "Payment via " + paymentVendorTypeEnum.code().toUpperCase() + " is not accessible for this view type " + paymentViewTypeEnum.value());
                     } else {
                        PaymentInterface paymentDAO = PaymentFactory.getPaymentDAO(paymentVendorTypeEnum);
                        if (paymentDAO != null) {
                           DataHolder<Map<String, Object>> result = new DataHolder(paymentDAO.clientInitiatePayment(jsonObject));
                           return result;
                        } else {
                           log.error("Sanity check failed. PaymentDAO should have been assigned");
                           throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
                        }
                     }
                  }
               }
            }
         }
      } catch (JSONException var11) {
         log.error("Invalid JSON Data:" + jsonDataString, var11);
         throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA);
      } catch (CreateException var12) {
         log.error("EJB create failure. JSON Data:" + jsonDataString, var12);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var12.getMessage());
      } catch (PaymentException var13) {
         log.error("PaymentException occurred. JSON Data:" + jsonDataString, var13);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var13.getMessage());
      } catch (FusionRestException var14) {
         log.error("FusionRestException occurred [" + var14.getMessage() + "] " + ". JSON Data:" + jsonDataString, var14);
         throw var14;
      } catch (Exception var15) {
         log.error("Unhandled exception clientInitatePayment [" + var15.getMessage() + "] " + ". JSON Data:" + jsonDataString, var15);
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to initiate payment via vendor '" + paymentVendorTypeStr + "'");
      }
   }

   @POST
   @Consumes({"application/json"})
   @Produces({"application/json"})
   @Path("/{paymentVendorType}")
   public DataHolder<Map<String, Object>> updatePayment(@PathParam("paymentVendorType") String paymentVendorTypeStr, String jsonDataString) throws FusionRestException {
      log.info("Received paymentVendorType=[" + paymentVendorTypeStr + "] " + "jsonDataString=" + jsonDataString);

      try {
         if (paymentVendorTypeStr == null) {
            throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Payment vendor type not supplied");
         } else {
            PaymentData.TypeEnum paymentVendorTypeEnum = PaymentData.TypeEnum.fromCode(paymentVendorTypeStr);
            if (paymentVendorTypeEnum == null) {
               throw new FusionRestException(FusionRestException.RestException.INVALID_PAYMENT_VENDOR, "Payment vendor type is not supported " + paymentVendorTypeStr);
            } else {
               PaymentInterface paymentDAO = PaymentFactory.getPaymentDAO(paymentVendorTypeEnum);
               JSONObject jsonObject = new JSONObject(jsonDataString);
               DataHolder<Map<String, Object>> result = new DataHolder(paymentDAO.updatePaymentStatus(jsonObject));
               return result;
            }
         }
      } catch (JSONException var7) {
         log.error("Invalid json data input : " + jsonDataString, var7);
         throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA);
      } catch (PaymentException var8) {
         log.error("Error in updating payment.", var8);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var8.getMessage());
      } catch (FusionRestException var9) {
         log.error("Processing failed. Input:" + jsonDataString);
         throw var9;
      } catch (Exception var10) {
         log.error("Unhandled exception updatePaymentStatus.Input:" + jsonDataString, var10);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      }
   }

   @PUT
   @Consumes({"application/json"})
   @Produces({"application/json"})
   @Path("/PAYPAL")
   public DataHolder<String> createPayment(String jsonDataString) throws FusionRestException {
      return this.createPayment(PaymentData.TypeEnum.fromCode("PAYPAL"), jsonDataString);
   }

   @GET
   @Produces({"application/json"})
   @Path("/PAYPAL/{token}")
   public DataHolder<String> getPaypalPaymentDetails(@PathParam("token") String tokenStr, @QueryParam("view") String viewStr) throws FusionRestException {
      return this.initiatePaymentAuthorization(PaymentData.TypeEnum.fromCode("PAYPAL"), tokenStr, viewStr, (JSONObject)null);
   }

   @PUT
   @Consumes({"application/json"})
   @Produces({"application/json"})
   @Path("/v2/{paymentVendorType}")
   public DataHolder<String> createPaymentV2(@PathParam("paymentVendorType") String paymentVendorTypeStr, String jsonRequestData) throws FusionRestException {
      if (StringUtil.isBlank(paymentVendorTypeStr)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Payment vendor type not supplied");
      } else {
         PaymentData.TypeEnum paymentVendorTypeEnum = PaymentData.TypeEnum.fromCode(paymentVendorTypeStr);
         if (paymentVendorTypeEnum == null) {
            throw new FusionRestException(FusionRestException.RestException.INVALID_PAYMENT_VENDOR, "Payment vendor type is not supported " + paymentVendorTypeStr);
         } else {
            try {
               JSONObject jsonObj = new JSONObject(jsonRequestData);
               return this.createPayment(paymentVendorTypeEnum, jsonObj.getString("data"));
            } catch (JSONException var5) {
               log.warn("Improper payload format: " + jsonRequestData);
               throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "You may have accessd the feature improperly. Please contact merchant@mig.me.");
            }
         }
      }
   }

   @POST
   @Produces({"application/json"})
   @Path("/v2/{paymentVendorType}/{token}")
   public DataHolder<String> initiatePaymentAuthorization(@PathParam("paymentVendorType") String paymentVendorTypeStr, @PathParam("token") String tokenStr, String jsonRequestData) throws FusionRestException {
      if (StringUtil.isBlank(paymentVendorTypeStr)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Payment vendor type not supplied");
      } else {
         PaymentData.TypeEnum paymentVendorTypeEnum = PaymentData.TypeEnum.fromCode(paymentVendorTypeStr);
         if (paymentVendorTypeEnum == null) {
            throw new FusionRestException(FusionRestException.RestException.INVALID_PAYMENT_VENDOR, "Payment vendor type is not supported " + paymentVendorTypeStr);
         } else {
            JSONObject jsonData = null;
            String view = null;

            try {
               JSONObject jsonObj = new JSONObject(jsonRequestData);
               jsonData = jsonObj.getJSONObject("data");
               view = jsonData.getString("view");
            } catch (JSONException var8) {
               log.warn("Invalid json format :: " + var8.getMessage());
               throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA);
            }

            return this.initiatePaymentAuthorization(paymentVendorTypeEnum, tokenStr, view, jsonData);
         }
      }
   }

   @POST
   @Produces({"application/json"})
   @Path("/{paymentId}/approve")
   public DataHolder<String> approvePayment(@PathParam("paymentId") Integer paymentIdStr, String requestData) throws FusionRestException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         PaymentData paymentDetails = null;

         try {
            paymentDetails = accountEJB.getPaymentById(paymentIdStr);
         } catch (EJBException var9) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to retrieve payment with id : " + paymentIdStr);
         }

         if (null == paymentDetails) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Payment does not exist.");
         } else {
            paymentDetails.accountEntrySource = new AccountEntrySourceData(PaymentResource.class);
            PaymentInterface paymentDAO = PaymentFactory.getPaymentDAO(paymentDetails.vendorType);
            String username = null;

            try {
               JSONObject jsonRequestData = new JSONObject(requestData);
               if (jsonRequestData.has("username")) {
                  username = jsonRequestData.getString("username");
               }
            } catch (JSONException var8) {
               log.info("No staff name provided for approving payment with id [" + paymentIdStr + "]");
            }

            PaymentIResponse response = paymentDAO.approve(paymentDetails, username);
            if (response != null) {
               return new DataHolder(response.toJSON(PaymentIResponse.ReturnType.APPROVE));
            } else {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{"An error occurred in the request."});
            }
         }
      } catch (CreateException var10) {
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      } catch (PaymentException var11) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, var11.getMessage());
      } catch (FusionRestException var12) {
         throw var12;
      } catch (Exception var13) {
         log.warn("Unhandled exception in approving credit card", var13);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "An error occurred in your request.");
      }
   }

   @POST
   @Produces({"application/json"})
   @Path("/{paymentId}/reject")
   public DataHolder<String> rejectPayment(@PathParam("paymentId") Integer paymentIdStr, String requestData) throws FusionRestException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         PaymentData paymentDetails = null;

         try {
            paymentDetails = accountEJB.getPaymentById(paymentIdStr);
         } catch (EJBException var9) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to retrieve payment with id : " + paymentIdStr);
         }

         if (null == paymentDetails) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Payment does not exist.");
         } else {
            paymentDetails.accountEntrySource = new AccountEntrySourceData(PaymentResource.class);
            PaymentInterface paymentDAO = PaymentFactory.getPaymentDAO(paymentDetails.vendorType);
            String username = null;

            try {
               JSONObject jsonRequestData = new JSONObject(requestData);
               if (jsonRequestData.has("username")) {
                  username = jsonRequestData.getString("username");
               }
            } catch (JSONException var8) {
               log.info("No staff name provided for rejecting payment with id [" + paymentIdStr + "]");
            }

            PaymentIResponse response = paymentDAO.reject(paymentDetails, username);
            if (response != null) {
               return new DataHolder(response.toJSON(PaymentIResponse.ReturnType.REJECT));
            } else {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[]{"An error occurred in the request."});
            }
         }
      } catch (CreateException var10) {
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      } catch (PaymentException var11) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, var11.getMessage());
      } catch (FusionRestException var12) {
         throw var12;
      } catch (Exception var13) {
         log.warn("Unhandled exception in approving credit card", var13);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "An error occurred in your request.");
      }
   }

   private DataHolder<String> createPayment(PaymentData.TypeEnum paymentVendorTypeEnum, String jsonDataString) throws FusionRestException {
      try {
         boolean thirdPartyPaymentIsEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments.THIRD_PARTY_PAYMENT_ENABLED);
         if (!thirdPartyPaymentIsEnabled) {
            throw new FusionRestException(FusionRestException.RestException.THIRD_PARTY_PAYMENT_DISABLED);
         } else {
            JSONObject jsonObject = new JSONObject(jsonDataString);
            String paymentViewTypeCode = StringUtil.trimmedLowerCase(JSONUtils.getString(jsonObject, "view"));
            if (StringUtil.isBlank(paymentViewTypeCode)) {
               throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Payment view type is blank");
            } else {
               PaymentData.ViewEnum paymentViewTypeEnum = PaymentData.ViewEnum.fromName(paymentViewTypeCode);
               if (paymentViewTypeEnum == null) {
                  log.warn("Attempt to access " + paymentViewTypeEnum.name() + " from an invalid view: " + paymentViewTypeCode);
                  throw new FusionRestException(FusionRestException.RestException.UNSUPPORTED_PAYMENT_VIEW_TYPE, "You may have accessed this page improperly. Please contact merchant@mig.me.");
               } else {
                  boolean accessAllowed = isPaymentVendorAccessibleFromViewType(PaymentData.TypeEnum.PAYPAL, paymentViewTypeEnum);
                  if (!accessAllowed) {
                     throw new FusionRestException(FusionRestException.RestException.UNSUPPORTED_PAYMENT_VIEW_TYPE, "Payment via " + PaymentData.TypeEnum.PAYPAL.code().toUpperCase() + " is not accessible for this view type " + paymentViewTypeEnum.value());
                  } else {
                     PaymentInterface paymentDAO = PaymentFactory.getPaymentDAO(paymentVendorTypeEnum);
                     if (paymentDAO != null) {
                        PaymentData paymentDetails = PaymentDataFactory.getPayment(paymentVendorTypeEnum, jsonObject);
                        PaymentIResponse response = paymentDAO.clientInitiatePayment(paymentDetails);
                        if (response != null) {
                           return new DataHolder(response.toJSON(PaymentIResponse.ReturnType.CREATE));
                        } else {
                           throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[0]);
                        }
                     } else {
                        log.error("Sanity check failed. PaymentDAO should have been assigned");
                        throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
                     }
                  }
               }
            }
         }
      } catch (JSONException var11) {
         log.error("Invalid JSON Data:" + jsonDataString, var11);
         throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA);
      } catch (PaymentException var12) {
         log.error("PaymentException occurred. JSON Data:" + jsonDataString, var12);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var12.getMessage());
      } catch (FusionRestException var13) {
         log.error("FusionRestException occurred [" + var13.getMessage() + "] " + ". JSON Data:" + jsonDataString, var13);
         throw var13;
      } catch (Exception var14) {
         log.error("Unhandled exception clientInitatePayment [" + var14.getMessage() + "] " + ". JSON Data:" + jsonDataString, var14);
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to initiate payment via vendor '" + PaymentData.TypeEnum.PAYPAL.code() + "'");
      }
   }

   private DataHolder<String> initiatePaymentAuthorization(PaymentData.TypeEnum paymentVendorTypeEnum, String tokenStr, String viewStr, JSONObject jsonParams) throws FusionRestException {
      try {
         boolean thirdPartyPaymentIsEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments.THIRD_PARTY_PAYMENT_ENABLED);
         if (!thirdPartyPaymentIsEnabled) {
            throw new FusionRestException(FusionRestException.RestException.THIRD_PARTY_PAYMENT_DISABLED);
         } else if (StringUtil.isBlank(viewStr)) {
            throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Payment view type is blank");
         } else {
            PaymentData.ViewEnum paymentViewTypeEnum = PaymentData.ViewEnum.fromName(viewStr);
            if (paymentViewTypeEnum == null) {
               throw new FusionRestException(FusionRestException.RestException.UNSUPPORTED_PAYMENT_VIEW_TYPE, "Payment view is unsupported " + viewStr);
            } else {
               boolean accessAllowed = isPaymentVendorAccessibleFromViewType(PaymentData.TypeEnum.PAYPAL, paymentViewTypeEnum);
               if (!accessAllowed) {
                  throw new FusionRestException(FusionRestException.RestException.UNSUPPORTED_PAYMENT_VIEW_TYPE, "Payment via " + PaymentData.TypeEnum.PAYPAL.code().toUpperCase() + " is not accessible for this view type " + paymentViewTypeEnum.value());
               } else {
                  PaymentInterface paymentDAO = PaymentFactory.getPaymentDAO(paymentVendorTypeEnum);
                  if (paymentDAO != null) {
                     PaymentIResponse response = paymentDAO.onPaymentAuthorized(tokenStr, jsonParams);
                     if (response != null) {
                        return new DataHolder(response.toJSON(PaymentIResponse.ReturnType.GET_COMPACT_DETAILS));
                     } else {
                        throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[0]);
                     }
                  } else {
                     log.error("Sanity check failed. PaymentDAO should have been assigned");
                     throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
                  }
               }
            }
         }
      } catch (PaymentException var10) {
         log.error("PaymentException occurred. JSON Data:", var10);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var10.getMessage());
      } catch (FusionRestException var11) {
         log.error("FusionRestException occurred [" + var11.getMessage() + "] ", var11);
         throw var11;
      } catch (Exception var12) {
         log.error("Unhandled exception clientInitatePayment [" + var12.getMessage() + "] ", var12);
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to initiate payment via vendor '" + PaymentData.TypeEnum.PAYPAL.code() + "'");
      }
   }

   @POST
   @Produces({"application/json"})
   @Path("/PAYPAL/{token}")
   public DataHolder<String> updatePaypalPayment(@PathParam("token") String tokenStr, String jsonDataString) throws FusionRestException {
      try {
         boolean thirdPartyPaymentIsEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments.THIRD_PARTY_PAYMENT_ENABLED);
         if (!thirdPartyPaymentIsEnabled) {
            throw new FusionRestException(FusionRestException.RestException.THIRD_PARTY_PAYMENT_DISABLED);
         } else {
            JSONObject jsonObject = new JSONObject(jsonDataString);
            String paymentViewTypeCode = StringUtil.trimmedLowerCase(JSONUtils.getString(jsonObject, "view"));
            if (StringUtil.isBlank(paymentViewTypeCode)) {
               throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Payment view type is blank");
            } else {
               PaymentData.ViewEnum paymentViewTypeEnum = PaymentData.ViewEnum.fromName(paymentViewTypeCode);
               if (paymentViewTypeEnum == null) {
                  throw new FusionRestException(FusionRestException.RestException.UNSUPPORTED_PAYMENT_VIEW_TYPE, "Payment view is unsupported " + paymentViewTypeCode);
               } else {
                  boolean accessAllowed = isPaymentVendorAccessibleFromViewType(PaymentData.TypeEnum.PAYPAL, paymentViewTypeEnum);
                  if (!accessAllowed) {
                     throw new FusionRestException(FusionRestException.RestException.UNSUPPORTED_PAYMENT_VIEW_TYPE, "Payment via " + PaymentData.TypeEnum.PAYPAL.code().toUpperCase() + " is not accessible for this view type " + paymentViewTypeEnum.value());
                  } else {
                     PaymentInterface paymentDAO = PaymentFactory.getPaymentDAO(PaymentData.TypeEnum.PAYPAL);
                     jsonObject.put("type", PaymentData.TypeEnum.PAYPAL.value());
                     PaypalTransactionTokenData paypalTransactionTokenData = PaypalPayment.getTransactionTokenDataFromCache(tokenStr, new Timestamp(System.currentTimeMillis()));
                     if (paypalTransactionTokenData == null) {
                        throw new FusionRestException(FusionRestException.RestException.ERROR, "You do not have an existing payment request.");
                     } else {
                        jsonObject.put("token", tokenStr);
                        jsonObject.put("currency", paypalTransactionTokenData.currency);
                        jsonObject.put("amount", paypalTransactionTokenData.amount);
                        jsonObject.put("username", paypalTransactionTokenData.username);
                        jsonObject.put("id", paypalTransactionTokenData.paymentID);
                        if (paymentDAO != null) {
                           PaymentData paymentData = PaymentDataFactory.getPayment(jsonObject);
                           PaymentIResponse response = paymentDAO.updatePaymentStatus(paymentData);
                           if (response != null) {
                              return new DataHolder(response.toJSON(PaymentIResponse.ReturnType.GET_COMPACT_DETAILS));
                           } else {
                              throw new PaymentException(ErrorCause.PaymentErrorReasonType.VENDOR_SYSTEM_ERROR, new Object[0]);
                           }
                        } else {
                           log.error("Sanity check failed. PaymentDAO should have been assigned");
                           throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
                        }
                     }
                  }
               }
            }
         }
      } catch (JSONException var12) {
         log.error("Invalid JSON Data:" + jsonDataString, var12);
         throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA);
      } catch (PaymentException var13) {
         log.error("PaymentException occurred. JSON Data:" + jsonDataString, var13);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var13.getMessage());
      } catch (FusionRestException var14) {
         log.error("FusionRestException occurred [" + var14.getMessage() + "] " + ". JSON Data:" + jsonDataString, var14);
         throw var14;
      } catch (Exception var15) {
         log.error("Unhandled exception clientInitatePayment [" + var15.getMessage() + "] " + ". JSON Data:" + jsonDataString, var15);
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to initiate payment via vendor '" + PaymentData.TypeEnum.PAYPAL.code() + "'");
      }
   }

   @POST
   @Path("/approvecreditcardpayment")
   @Produces({"application/json"})
   public DataHolder<BooleanData> approveCreditCardPayment(@QueryParam("staffusername") String staffUsername, @QueryParam("creditcardpaymentid") int creditCardPaymentId, @QueryParam("ipaddress") String ipAddress, @QueryParam("sessionid") String sessionID, @QueryParam("mobiledevice") String mobileDevice, @QueryParam("useragent") String userAgent) throws FusionRestException {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountBean.approveCreditCardPayment(staffUsername, creditCardPaymentId, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return new DataHolder(new BooleanData(true));
      } catch (Exception var8) {
         log.error(String.format("Error in approvecreditcardpayment staffUsername:%s", staffUsername), var8);
         throw new FusionRestException(101, "Unable to approvecreditcardpayment");
      }
   }

   @POST
   @Path("/rejectcreditcardpayment")
   @Produces({"application/json"})
   public DataHolder<BooleanData> rejectCreditCardPayment(@QueryParam("staffusername") String staffUsername, @QueryParam("creditcardpaymentid") int creditCardPaymentId, @QueryParam("reason") String reason, @QueryParam("ipaddress") String ipAddress, @QueryParam("sessionid") String sessionID, @QueryParam("mobiledevice") String mobileDevice, @QueryParam("useragent") String userAgent) throws FusionRestException {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountBean.rejectCreditCardPayment(staffUsername, creditCardPaymentId, reason, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return new DataHolder(new BooleanData(true));
      } catch (Exception var9) {
         log.error(String.format("Error in rejectcreditcardpayment staffUsername:%s", staffUsername), var9);
         throw new FusionRestException(101, "Unable to rejectcreditcardpayment");
      }
   }

   @POST
   @Path("/{username}/credituserandsendsms")
   @Produces({"application/json"})
   public DataHolder<BooleanData> creditUserAndSendSMS(@PathParam("username") String username, @QueryParam("amountsent") double amountSent, @QueryParam("amountcredit") double amountCredit, @QueryParam("cashreceiptid") String cashReceiptID, @QueryParam("ipaddress") String ipAddress, @QueryParam("sessionid") String sessionID, @QueryParam("mobiledevice") String mobileDevice, @QueryParam("useragent") String userAgent) throws FusionRestException {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountBean.creditAndNotifyUser(username, amountSent, amountCredit, cashReceiptID, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent), true);
         return new DataHolder(new BooleanData(true));
      } catch (Exception var12) {
         log.error(String.format("Error in credituserandsendsms username:%s", username), var12);
         throw new FusionRestException(101, "Unable to credituserandsendsms");
      }
   }
}
