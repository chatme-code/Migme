package com.projectgoth.fusion.payment;

import com.projectgoth.fusion.common.EnumUtils;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class PaymentMetaDetails implements Serializable {
   public Integer id;
   public PaymentMetaDetails.MetaType type;
   public String value;

   public PaymentMetaDetails(PaymentMetaDetails.MetaType type) {
      this.type = type;
   }

   public PaymentMetaDetails(PaymentMetaDetails.MetaType type, String value) {
      this.type = type;
      this.value = value;
   }

   public PaymentMetaDetails(ResultSet rs) throws SQLException {
      this.type = PaymentMetaDetails.MetaType.valueOf(rs.getString("type"));
      this.id = rs.getInt("id");
      this.value = rs.getString("value");
   }

   public static enum MetaType implements EnumUtils.IEnumValueGetter<Integer> {
      FIRST_NAME(1),
      LAST_NAME(2),
      PAYPAL_ACCOUNT(3),
      AUTO_APPROVE(4),
      GC_CC_REFERENCE(5),
      GC_CC_STATUS(6),
      GC_CC_MERCHANTID(7),
      GC_CC_CARD_TYPE(8),
      GC_CC_FRAUD(9);

      private int code;
      private static HashMap<Integer, PaymentMetaDetails.MetaType> lookupByCode = new HashMap();

      private MetaType(int code) {
         this.code = code;
      }

      public int code() {
         return this.code;
      }

      public Integer getEnumValue() {
         return this.code;
      }

      public static PaymentMetaDetails.MetaType fromCode(int code) {
         return (PaymentMetaDetails.MetaType)lookupByCode.get(code);
      }

      static {
         EnumUtils.IEnumValueExtractor<Integer, PaymentMetaDetails.MetaType> vendorCodeExtractor = new EnumUtils.IEnumValueExtractor<Integer, PaymentMetaDetails.MetaType>() {
            public Integer getValue(PaymentMetaDetails.MetaType enumConst) {
               return enumConst.code;
            }
         };
         EnumUtils.populateLookUpMap(lookupByCode, PaymentMetaDetails.MetaType.class, vendorCodeExtractor);
      }
   }
}
