package com.projectgoth.fusion.payment.creditcard;

import com.projectgoth.fusion.data.CreditCardPaymentData;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentDataFactory;
import com.projectgoth.fusion.payment.PaymentMetaDetails;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class CreditCardData extends PaymentData {
   public CreditCardPaymentData.StatusEnum creditCardPaymentStatus;
   public boolean allowAutoApprove = false;
   public CreditCardPaymentData.CardTypeEnum cardType;
   public String merchantID;
   public String authorisationCode;
   public static final PaymentDataFactory.ObjectCreator OBJECT_CREATOR = new PaymentDataFactory.ObjectCreator() {
      public PaymentData createFrom(ResultSet rs) throws SQLException {
         return new CreditCardData(rs);
      }

      public PaymentData createFrom(JSONObject jsonObject) throws JSONException, ParseException {
         CreditCardData ccData = new CreditCardData(jsonObject);
         List<PaymentMetaDetails> details = new ArrayList();
         if (jsonObject.has("creditCardType")) {
            details.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.GC_CC_CARD_TYPE, jsonObject.getString("creditCardType")));
            ccData.cardType = CreditCardPaymentData.CardTypeEnum.fromValue(jsonObject.getInt("creditCardType"));
         }

         return ccData;
      }
   };

   protected CreditCardData(JSONObject jsonObject) throws JSONException, ParseException {
      super(jsonObject);
   }

   public CreditCardData() throws Exception {
      super(PaymentData.TypeEnum.CREDIT_CARD);
   }

   protected CreditCardData(ResultSet rs) throws SQLException {
      super(rs);
   }

   public String getCcPaymentMerchantID() {
      Iterator i$ = this.details.iterator();

      PaymentMetaDetails metaDetail;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         metaDetail = (PaymentMetaDetails)i$.next();
      } while(metaDetail.type != PaymentMetaDetails.MetaType.GC_CC_MERCHANTID);

      return metaDetail.value;
   }

   public boolean getAllowAutoApprove() {
      Iterator i$ = this.details.iterator();

      PaymentMetaDetails metaDetail;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         metaDetail = (PaymentMetaDetails)i$.next();
      } while(metaDetail.type != PaymentMetaDetails.MetaType.AUTO_APPROVE);

      return Boolean.valueOf(metaDetail.value);
   }

   public List<PaymentMetaDetails> getDetails() {
      return this.details;
   }

   public void setDetails(List<PaymentMetaDetails> meta) {
      this.details = meta;
   }

   public void loadMetaDetails() {
      Iterator i$ = this.details.iterator();

      while(i$.hasNext()) {
         PaymentMetaDetails metaDetail = (PaymentMetaDetails)i$.next();
         if (metaDetail.type == PaymentMetaDetails.MetaType.AUTO_APPROVE) {
            this.allowAutoApprove = Boolean.valueOf(metaDetail.value);
         } else if (metaDetail.type == PaymentMetaDetails.MetaType.GC_CC_MERCHANTID) {
            this.merchantID = metaDetail.value;
         } else if (metaDetail.type == PaymentMetaDetails.MetaType.GC_CC_CARD_TYPE) {
            this.cardType = CreditCardPaymentData.CardTypeEnum.fromValue(Integer.parseInt(metaDetail.value));
         }
      }

   }

   public PaymentData.StatusEnum getInitialStatus() {
      return PaymentData.StatusEnum.ONHOLD;
   }

   public CreditCardPaymentData toCreditCardPaymentData() throws Exception {
      CreditCardPaymentData creditCardPaymentData = new CreditCardPaymentData();
      creditCardPaymentData.username = this.username;
      creditCardPaymentData.status = getCreditCardPaymentDataStatus(this.status);
      creditCardPaymentData.allowAutoApprove = this.allowAutoApprove;
      creditCardPaymentData.providerTransactionId = this.vendorTransactionId;
      creditCardPaymentData.dateCreated = this.fetchCreatedTime();
      creditCardPaymentData.amount = this.amount;
      creditCardPaymentData.currency = this.currency;
      creditCardPaymentData.id = this.id;
      return creditCardPaymentData;
   }

   public static CreditCardPaymentData.StatusEnum getCreditCardPaymentDataStatus(PaymentData.StatusEnum status) throws Exception {
      if (status == PaymentData.StatusEnum.PENDING) {
         return CreditCardPaymentData.StatusEnum.AWAITING_APPROVAL;
      } else if (status == PaymentData.StatusEnum.APPROVED) {
         return CreditCardPaymentData.StatusEnum.APPROVED;
      } else if (status == PaymentData.StatusEnum.REJECTED) {
         return CreditCardPaymentData.StatusEnum.DECLINED;
      } else if (status == PaymentData.StatusEnum.CHARGE_BACK) {
         return CreditCardPaymentData.StatusEnum.CHARGE_BACK;
      } else {
         throw new Exception("Status not supported for credit card payments: " + status.name());
      }
   }
}
