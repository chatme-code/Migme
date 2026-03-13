package com.projectgoth.fusion.payment.ratelimit;

public enum PaymentRateLimitType {
   TOTAL_SUCCESS_VALUE_PER_USERID(1),
   TOTAL_SUCCESS_VALUE_PER_USERTYPE(2),
   TOTAL_SUCCESS_VALUE_PER_COUNTRY(3),
   TOTAL_SUCCESS_COUNT_PER_USERID(4),
   TOTAL_SUCCESS_COUNT_PER_USERTYPE(5),
   TOTAL_SUCCESS_COUNT_PER_COUNTRY(6),
   TOTAL_SUCCESS_COUNT_PER_VENDOR_USERID(7);

   int typeCode;

   private PaymentRateLimitType(int typeCode) {
      this.typeCode = typeCode;
   }

   public int getTypeCode() {
      return this.typeCode;
   }
}
