package com.projectgoth.fusion.payment.mol;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.JSONUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentDataFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class MOLPaymentData extends PaymentData {
   public static final PaymentDataFactory.ObjectCreator OBJECT_CREATOR = new PaymentDataFactory.ObjectCreator() {
      public MOLPaymentData createFrom(ResultSet rs) throws SQLException {
         return new MOLPaymentData(rs);
      }

      public MOLPaymentData createFrom(JSONObject jsonObject) throws JSONException, ParseException {
         return new MOLPaymentData(jsonObject);
      }
   };
   public String merchantID;
   public String paymentDescription;
   public String transDateTime;
   public String vendorStatusUpdResCode;
   public String asynchStatusUpdResCode;
   public String asynchStatusUpdStatusCode;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MOLPaymentData.class));

   protected MOLPaymentData(ResultSet rs) throws SQLException {
      super(rs);
   }

   protected MOLPaymentData(JSONObject jsonObject) throws JSONException, ParseException {
      super(jsonObject);
   }

   public MOLPaymentData() {
      super(PaymentData.TypeEnum.MOL);
   }

   protected void deserializeExtraFields(ResultSet rs) throws SQLException {
      byte[] extraFields = rs.getBytes("description");

      try {
         this.merchantID = null;
         this.paymentDescription = null;
         this.transDateTime = null;
         this.asynchStatusUpdResCode = null;
         this.vendorStatusUpdResCode = null;
         this.asynchStatusUpdStatusCode = null;
         if (extraFields != null) {
            JSONObject jsonObj = new JSONObject(new String(extraFields));
            this.merchantID = JSONUtils.getString(jsonObj, "merchantID");
            this.paymentDescription = JSONUtils.getString(jsonObj, "paymentDescription");
            this.transDateTime = JSONUtils.getString(jsonObj, "transDateTime");
            this.asynchStatusUpdResCode = JSONUtils.getString(jsonObj, "asynchStatusUpdResCode");
            this.vendorStatusUpdResCode = JSONUtils.getString(jsonObj, "vendorStatusUpdResCode");
            this.asynchStatusUpdStatusCode = JSONUtils.getString(jsonObj, "asynchStatusUpdStatusCode");
         }
      } catch (JSONException var4) {
         log.error("Error in retrieving MOL extra fields: ", var4);
      }

   }

   public JSONObject serializeExtraFieldsToJSON() throws JSONException {
      JSONObject jsonObj = new JSONObject();
      jsonObj.put("merchantID", this.merchantID);
      jsonObj.put("paymentDescription", this.paymentDescription);
      jsonObj.put("vendorStatusUpdResCode", this.vendorStatusUpdResCode);
      jsonObj.put("asynchStatusUpdResCode", this.asynchStatusUpdResCode);
      jsonObj.put("transDateTime", this.transDateTime);
      jsonObj.put("asynchStatusUpdStatusCode", this.asynchStatusUpdStatusCode);
      return jsonObj;
   }

   public boolean enableStrictAmountCheck() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL.ENABLED_TO_ALL_USERS);
   }
}
