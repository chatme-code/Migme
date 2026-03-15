package com.projectgoth.fusion.payment.paypal;

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

public class PaypalPaymentData extends PaymentData {
   public String paypalAccount;
   public String token;
   public String action;
   public String returnUrl;
   public String cancelUrl;
   public static final PaymentDataFactory.ObjectCreator OBJECT_CREATOR = new PaymentDataFactory.ObjectCreator() {
      public PaymentData createFrom(ResultSet rs) throws SQLException {
         return new PaypalPaymentData(rs);
      }

      public PaymentData createFrom(JSONObject jsonObject) throws JSONException, ParseException {
         return new PaypalPaymentData(jsonObject);
      }
   };

   public PaypalPaymentData(ResultSet rs) throws SQLException {
      super(rs);
   }

   public PaypalPaymentData() {
      super(PaymentData.TypeEnum.PAYPAL);
   }

   public PaypalPaymentData(JSONObject jsonObject) throws JSONException, ParseException {
      super(jsonObject);
      if (jsonObject.has("paypalAccount")) {
         this.details.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.PAYPAL_ACCOUNT, jsonObject.getString("paypalAccount")));
         this.paypalAccount = jsonObject.getString("paypalAccount");
      }

      if (jsonObject.has("token")) {
         this.token = jsonObject.getString("token");
      }

      if (jsonObject.has("returnUrl")) {
         this.returnUrl = jsonObject.getString("returnUrl");
      }

      if (jsonObject.has("cancelUrl")) {
         this.cancelUrl = jsonObject.getString("cancelUrl");
      }

   }

   protected void deserializeExtraFields(JSONObject additionDetails) throws JSONException {
      if (additionDetails.has("paypalAccount")) {
         this.paypalAccount = additionDetails.getString("paypalAccount");
      }

      if (additionDetails.has("action")) {
         this.action = additionDetails.getString("action");
      }

   }

   public List<PaymentMetaDetails> getDetails() {
      List<PaymentMetaDetails> listDetails = new ArrayList();
      if (this.paypalAccount != null) {
         listDetails.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.PAYPAL_ACCOUNT, this.paypalAccount));
      }

      return listDetails;
   }

   public void setDetails(List<PaymentMetaDetails> details) {
      Iterator i$ = details.iterator();

      while(i$.hasNext()) {
         PaymentMetaDetails detail = (PaymentMetaDetails)i$.next();
         if (detail.type == PaymentMetaDetails.MetaType.PAYPAL_ACCOUNT) {
            this.paypalAccount = detail.value;
         }
      }

   }
}
