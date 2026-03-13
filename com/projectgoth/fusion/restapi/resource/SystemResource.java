package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentException;
import com.projectgoth.fusion.payment.PaymentFactory;
import com.projectgoth.fusion.payment.PaymentInterface;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.slice.RegistryAdminPrx;
import com.projectgoth.fusion.slice.RegistryStats;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.CreateException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import org.json.JSONObject;

@Provider
@Path("/system")
public class SystemResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SystemResource.class));
   private static LazyLoader<String> StatsBuilder = new LazyLoader<String>("SystemResource.StatsBuilder", 60000L) {
      protected String fetchValue() throws Exception {
         try {
            JSONObject json = new JSONObject();
            RegistryAdminPrx registryAdminPrx = EJBIcePrxFinder.getRegistryAdmin();
            RegistryStats stats = registryAdminPrx.getStats();
            json.put("users", stats.numUserProxies);
            json.put("maxusers", stats.maxUserProxies);
            json.put("groups", stats.numGroupChatProxies);
            json.put("maxgroups", stats.maxGroupChatProxies);
            json.put("rooms", stats.numChatRoomProxies);
            json.put("maxrooms", stats.maxChatRoomProxies);
            return json.toString();
         } catch (Exception var4) {
            SystemResource.log.error("Error updating system status:" + var4.getMessage(), var4);
            return "";
         }
      }
   };

   @GET
   @Path("/property/{namespace}/{propertyname}")
   @Produces({"application/json"})
   public DataHolder<String> getProperty(@PathParam("namespace") String namespace, @PathParam("propertyname") String propertyName, @QueryParam("default") String defaultValue) throws FusionRestException {
      boolean isDefaultNS = "default".equalsIgnoreCase(namespace);
      String namespacedPropertyName = isDefaultNS ? propertyName : SystemPropertyEntities.getNameWithNamespace(namespace, propertyName);
      SystemPropertyEntities.SystemPropertyEntryInterface entry = SystemPropertyEntities.getEntry(namespacedPropertyName);
      String value;
      if (entry == null) {
         if (isDefaultNS) {
            value = SystemProperty.get(propertyName, defaultValue);
            if (value == null && defaultValue == null) {
               throw new FusionRestException(FusionRestException.RestException.UNKNOWN_SYSTEM_PROPERTY, String.format("Unrecogized default system property %s", propertyName));
            }
         } else {
            value = SystemProperty.get(namespacedPropertyName, defaultValue);
            if (value == null && defaultValue == null) {
               throw new FusionRestException(FusionRestException.RestException.UNKNOWN_SYSTEM_PROPERTY, String.format("Unrecogized non-default system property %s:%s", namespace, propertyName));
            }
         }
      } else {
         value = SystemProperty.get(entry);
      }

      return new DataHolder(value);
   }

   @GET
   @Path("/payment/options")
   @Produces({"application/json"})
   public DataHolder<String[]> getPaymentOptions(@QueryParam("view") String viewStr, @QueryParam("username") String username) throws FusionRestException {
      if (StringUtil.isBlank(viewStr)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "view param not supplied");
      } else if (StringUtil.isBlank(username)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "username param not supplied");
      } else {
         PaymentData.ViewEnum view = PaymentData.ViewEnum.fromName(viewStr);
         if (view != null) {
            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               UserData userData = userEJB.loadUser(username, false, false);
               if (userData == null) {
                  throw new FusionRestException(FusionRestException.RestException.UNKNOWN_USER_NAME);
               } else {
                  SystemPropertyEntities.Payments availPaymentVendorListSystemKey = SystemPropertyEntities.Payments.getSystemPropertyKeyForEnabledVendors(view.name());
                  List<String> returnOptions = new ArrayList();
                  if (availPaymentVendorListSystemKey != null) {
                     String[] paymentsAvailable = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)availPaymentVendorListSystemKey);
                     if (paymentsAvailable != null) {
                        String[] arr$ = paymentsAvailable;
                        int len$ = paymentsAvailable.length;

                        for(int i$ = 0; i$ < len$; ++i$) {
                           String paymentType = arr$[i$];

                           try {
                              PaymentInterface pDAO = PaymentFactory.getPaymentDAO(paymentType);
                              if (pDAO.isAccessAllowed(userData)) {
                                 returnOptions.add(paymentType);
                              }
                           } catch (PaymentException var15) {
                              ErrorCause cause = var15.getErrorCause();
                              if (cause == ErrorCause.PaymentErrorReasonType.UNKNOWN_PAYMENT_VENDOR) {
                                 log.error("[" + paymentType + "] is unknown. Consider amending or removing from system property " + availPaymentVendorListSystemKey);
                              } else {
                                 if (cause != ErrorCause.PaymentErrorReasonType.PAYMENT_INTERFACE_NOT_REGISTERED) {
                                    throw var15;
                                 }

                                 log.error("Misconfiguration? No payment interface is registered for payment type [" + paymentType + "]. Unable to check permission for username " + username);
                              }
                           }
                        }
                     }
                  } else {
                     log.warn("Enabled-vendors system property key for view [" + view.name() + "] is not available");
                  }

                  return new DataHolder(returnOptions.toArray(new String[returnOptions.size()]));
               }
            } catch (FusionRestException var16) {
               log.error("Unknown user [" + username + "].", var16);
               throw var16;
            } catch (CreateException var17) {
               log.error("Unable to get available views for view " + viewStr + " [" + username + "].", var17);
               throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to retrieve available payment options for " + viewStr);
            } catch (Exception var18) {
               log.error("Unable to get available views for view " + viewStr + " [" + username + "].", var18);
               throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to retrieve available payment options for " + viewStr);
            }
         } else {
            throw new FusionRestException(FusionRestException.RestException.UNSUPPORTED_PAYMENT_VIEW_TYPE);
         }
      }
   }

   @GET
   @Path("/payment/currency")
   @Produces({"application/json"})
   public DataHolder<String> getPaymentOptionCurrency(@QueryParam("paymentType") String paymentTypeStr, @QueryParam("username") String username) throws FusionRestException {
      if (StringUtil.isBlank(paymentTypeStr)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Payment type is not supplied");
      } else if (StringUtil.isBlank(username)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Username is not supplied");
      } else {
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userEJB.loadUser(username, false, false);
            if (userData == null) {
               throw new FusionRestException(FusionRestException.RestException.UNKNOWN_USER_NAME);
            } else {
               PaymentInterface pDAO = null;

               try {
                  pDAO = PaymentFactory.getPaymentDAO(paymentTypeStr);
               } catch (PaymentException var8) {
                  ErrorCause cause = var8.getErrorCause();
                  if (cause == ErrorCause.PaymentErrorReasonType.UNKNOWN_PAYMENT_VENDOR) {
                     throw new FusionRestException(FusionRestException.RestException.INVALID_PAYMENT_VENDOR);
                  }

                  throw var8;
               }

               if (!pDAO.isAccessAllowed(userData)) {
                  throw new FusionRestException(FusionRestException.RestException.ERROR, "Unauthorized access.");
               } else {
                  String currency = pDAO.getCurrencyForUser(userData);
                  if (currency == null) {
                     currency = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments.DEFAULT_CURRENCY);
                  }

                  return new DataHolder(currency);
               }
            }
         } catch (FusionRestException var9) {
            log.error("Unknown user [" + username + "].", var9);
            throw var9;
         } catch (CreateException var10) {
            log.error("Unable to get currency for " + paymentTypeStr + " [" + username + "].", var10);
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to continue with payment.");
         } catch (Exception var11) {
            log.error("Unable to get currency for " + paymentTypeStr + " [" + username + "].", var11);
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to continue with payment.");
         }
      }
   }

   @GET
   @Path("/status")
   @Produces({"application/json"})
   public DataHolder<String> getStatus() throws FusionRestException {
      return new DataHolder(StatsBuilder.getValue());
   }

   @GET
   @Path("/languagelist")
   @Produces({"application/json"})
   public DataHolder<List> getLanguageList() throws FusionRestException {
      try {
         Web webBean = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
         return new DataHolder(webBean.getLanguages());
      } catch (Exception var2) {
         log.error("Error in languagelist", var2);
         throw new FusionRestException(101, "Unable to languagelist");
      }
   }
}
