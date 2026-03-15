package com.projectgoth.fusion.payment;

import com.projectgoth.fusion.common.EnumUtils;
import java.util.HashMap;
import org.json.JSONException;

public abstract class PaymentIResponse {
   public PaymentData paymentData;
   public Integer code;
   public String response;

   public abstract String toJSON(PaymentIResponse.ReturnType var1) throws JSONException;

   public static enum ReturnType implements EnumUtils.IEnumValueGetter<Integer> {
      CREATE(1),
      GET_COMPACT_DETAILS(2),
      GET_FULL_DETAILS(3),
      UPDATE(4),
      APPROVE(5),
      REJECT(6);

      private Integer value;
      private static HashMap<Integer, PaymentIResponse.ReturnType> lookupByValue = new HashMap();

      private ReturnType(int value) {
         this.value = value;
      }

      public static PaymentIResponse.ReturnType fromValue(int value) {
         return (PaymentIResponse.ReturnType)lookupByValue.get(value);
      }

      public Integer getEnumValue() {
         return this.value;
      }

      static {
         EnumUtils.populateLookUpMap(lookupByValue, PaymentIResponse.ReturnType.class);
      }
   }
}
