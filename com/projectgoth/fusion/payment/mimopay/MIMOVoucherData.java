package com.projectgoth.fusion.payment.mimopay;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.JSONUtils;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentDataFactory;
import com.projectgoth.fusion.payment.VendorVoucherData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class MIMOVoucherData extends VendorVoucherData {
   public static final PaymentDataFactory.ObjectCreator OBJECT_CREATOR = new PaymentDataFactory.ObjectCreator() {
      public PaymentData createFrom(ResultSet rs) throws SQLException {
         return new MIMOVoucherData(rs);
      }

      public MIMOVoucherData createFrom(JSONObject jsonObject) throws JSONException, ParseException {
         return new MIMOVoucherData(jsonObject);
      }
   };
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MIMOVoucherData.class));
   public String merchantCode;
   public String creditReloadPType;
   public Integer vendorStatusCode;
   public String gameCode;
   public String vendorRemark;
   public Double rvalue;
   public Long vendorTimestamp;

   public MIMOVoucherData() {
      super(PaymentData.TypeEnum.MIMOPAY);
   }

   protected MIMOVoucherData(ResultSet rs) throws SQLException {
      super(rs);
   }

   protected MIMOVoucherData(JSONObject jsonObject) throws JSONException, ParseException {
      super(jsonObject);
   }

   protected void deserializeExtraFields(ResultSet rs) throws SQLException {
      byte[] extraFields = rs.getBytes("description");

      try {
         if (extraFields != null) {
            JSONObject jsonObj = new JSONObject(new String(extraFields));
            this.merchantCode = JSONUtils.getString(jsonObj, "merchantCode");
            this.creditReloadPType = JSONUtils.getString(jsonObj, "creditReloadPType");
            this.vendorStatusCode = JSONUtils.getInteger(jsonObj, "vendorStatusCode");
            this.gameCode = JSONUtils.getString(jsonObj, "gameCode");
            this.vendorRemark = JSONUtils.getString(jsonObj, "vendorRemark");
            this.rvalue = JSONUtils.getDouble(jsonObj, "rvalue");
            this.vendorTimestamp = JSONUtils.getLong(jsonObj, "vendorTimestamp");
         }
      } catch (JSONException var4) {
         log.error("Error in retrieving MIMO extra fields: ", var4);
      }

   }

   public boolean enableStrictAmountCheck() {
      return false;
   }

   public JSONObject serializeExtraFieldsToJSON() throws JSONException {
      JSONObject jsonObj = new JSONObject();
      jsonObj.put("merchantCode", this.merchantCode);
      jsonObj.put("creditReloadPType", this.creditReloadPType);
      jsonObj.put("vendorStatusCode", this.vendorStatusCode);
      jsonObj.put("gameCode", this.gameCode);
      jsonObj.put("vendorRemark", this.vendorRemark);
      jsonObj.put("rvalue", this.rvalue);
      jsonObj.put("vendorTimestamp", this.vendorTimestamp);
      return jsonObj;
   }
}
