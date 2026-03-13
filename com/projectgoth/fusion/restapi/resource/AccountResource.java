package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.HashObjectUtils;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CashReceiptData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.recommendation.collector.DataCollectorUtils;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.ReverseCreditData;
import com.projectgoth.fusion.restapi.data.ThirdPartyPaymentData;
import java.util.Hashtable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/account")
public class AccountResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AccountResource.class));

   @POST
   @Path("/reverse/transfer_credit")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Boolean> reverseTransferCredit(DataHolder<ReverseCreditData> postRequest) throws FusionRestException {
      String reqId = generateRequestID();
      ReverseCreditData data = (ReverseCreditData)postRequest.data;
      log.info("[" + reqId + "];received reverseTransferCredit;data=[" + data + "]");

      try {
         AccountLocal accountLocal = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountLocal.reverseTransferCredit(data.accountEntryID, data.misUserName, new AccountEntrySourceData(AccountResource.class));
      } catch (Exception var5) {
         log.error(String.format("[" + reqId + "] Failed to reverse transfer credit, param:%s, due to:%s", data, var5), var5);
         throw new FusionRestException(101, "[" + reqId + "] Internal Error, Failed to reverse transfer credit, please try again later.");
      }

      return new DataHolder(true);
   }

   @POST
   @Path("/reverse/ttcredit")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Boolean> reverseTTCredit(DataHolder<ReverseCreditData> postRequest) throws FusionRestException {
      ReverseCreditData data = (ReverseCreditData)postRequest.data;

      try {
         AccountLocal accountLocal = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountLocal.reverseTTCredit(data.accountEntryID, data.misUserName, new AccountEntrySourceData(AccountResource.class));
      } catch (Exception var4) {
         log.error(String.format("Failed to reverse transfer credit, param:%s, due to:%s", data, var4), var4);
         throw new FusionRestException(101, "Internal Error, Failed to reverse transfer credit, please try again later");
      }

      return new DataHolder(true);
   }

   private static String generateRequestID() {
      return DataCollectorUtils.newErrorID();
   }

   @POST
   @Path("/cashreceipt/match")
   @Consumes({"application/json"})
   public void matchCashReceipt(DataHolder<CashReceiptData> cashReceiptDataHolder) throws FusionRestException {
      String reqId = generateRequestID();
      CashReceiptData cashReceiptData = (CashReceiptData)cashReceiptDataHolder.data;
      log.info("[" + reqId + "];received matchCashReceipt;data=[" + cashReceiptData + "]");
      Integer id = cashReceiptData.id;
      if (id == null) {
         String msg = "[" + reqId + "] Cash receipt id must not be null";
         log.warn(msg);
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING.getCode(), msg);
      } else {
         try {
            MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            misBean.matchCashReceipt(cashReceiptData, new AccountEntrySourceData(AccountResource.class));
         } catch (Exception var6) {
            log.error(String.format("[" + reqId + "] Failed to match  cash receipt, id:%s, due to:%s", id, var6), var6);
            throw new FusionRestException(101, "[" + reqId + "] Internal Error, Failed to match  cash receipt id [" + id + "].Exception:" + var6.getMessage());
         }
      }
   }

   @POST
   @Path("/cashreceipt/create")
   @Consumes({"application/json"})
   public DataHolder<CashReceiptData> createCashReceipt(DataHolder<CashReceiptData> cashReceiptDataHolder) throws FusionRestException {
      String reqId = generateRequestID();
      CashReceiptData cashReceiptData = (CashReceiptData)cashReceiptDataHolder.data;
      log.info("[" + reqId + "];received createCashReceipt;data=[" + cashReceiptData + "]");

      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CashReceiptData result = misBean.createCashReceipt(cashReceiptData, new AccountEntrySourceData(AccountResource.class));
         log.info("[" + reqId + "];createCashReceipt inserted new id=[" + cashReceiptData.id + "]");
         return new DataHolder(result);
      } catch (Exception var6) {
         log.error(String.format("[" + reqId + "] Failed to create  cash receipt, due to:%s", var6), var6);
         throw new FusionRestException(101, "[" + reqId + "] Internal Error, Failed to create  cash receipt:" + var6.getMessage());
      }
   }

   @POST
   @Path("{username}/paythirdparty")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<AccountEntryData> thirdPartyAPIDebit(@PathParam("username") String username, DataHolder<ThirdPartyPaymentData> paymentDataHolder) throws FusionRestException {
      ThirdPartyPaymentData paymentData = (ThirdPartyPaymentData)paymentDataHolder.data;

      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         AccountEntryData accountEntryData = accountEJB.thirdPartyAPIDebit(username, paymentData.reference, paymentData.description, paymentData.amount, paymentData.currency, new AccountEntrySourceData(paymentData.ipAddress, paymentData.sessionId, paymentData.mobileDevice, paymentData.userAgent));
         log.info("[" + paymentData.reference + "];thirdPartyAPIDebit inserted new id=[" + accountEntryData.id + "]");
         return new DataHolder(accountEntryData);
      } catch (Exception var6) {
         FusionRestException.RestException errorCode = FusionRestException.RestException.ERROR;
         if (var6 instanceof EJBExceptionWithErrorCause && ((EJBExceptionWithErrorCause)var6).getErrorCause() == ErrorCause.TransferCreditErrorReasonType.INSUFFICIENT_CREDIT) {
            errorCode = FusionRestException.RestException.INSUFFICIENT_CREDIT;
         }

         log.error(String.format("[" + paymentData.reference + "] Failed to do third party payment for user(%s), due to:%s", username, var6), var6);
         throw new FusionRestException(errorCode, "[" + paymentData.reference + "] Internal Error, Failed to do third party payment:" + var6.getMessage());
      }
   }

   @GET
   @Path("getaccountentry")
   @Produces({"application/json"})
   public DataHolder<Hashtable> getAccountEntry(@QueryParam("id") long id) throws FusionRestException {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         return new DataHolder(HashObjectUtils.dataObjectToHashtable(accountBean.getAccountEntry(id)));
      } catch (Exception var4) {
         log.error(String.format("Error in getaccountentry id:%s", id), var4);
         throw new FusionRestException(101, "Unable to getaccountentry");
      }
   }
}
