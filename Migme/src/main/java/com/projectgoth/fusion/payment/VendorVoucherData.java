package com.projectgoth.fusion.payment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

public class VendorVoucherData extends PaymentData {
   public int voucherVoucherId;
   public String voucherCode;

   protected VendorVoucherData(ResultSet rs) throws SQLException {
      super(rs);
      this.voucherVoucherId = rs.getInt("vendorVoucherId");
      this.voucherCode = rs.getString("voucherCode");
   }

   public VendorVoucherData(PaymentData.TypeEnum vendorType) {
      super(vendorType);
   }

   public VendorVoucherData(JSONObject jsonObject) throws JSONException, ParseException {
      super(jsonObject);
   }

   protected void deserializeExtraFields(ResultSet rs) throws SQLException {
      super.deserializeExtraFields(rs);
   }

   public PaymentData.StatusEnum getInitialStatus() {
      return PaymentData.StatusEnum.ONHOLD;
   }
}
