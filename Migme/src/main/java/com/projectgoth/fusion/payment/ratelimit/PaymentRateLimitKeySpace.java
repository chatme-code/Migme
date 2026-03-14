package com.projectgoth.fusion.payment.ratelimit;

import com.projectgoth.fusion.payment.PaymentData;

public interface PaymentRateLimitKeySpace {
   PaymentRateLimitType getRateLimitType();

   PaymentData.TypeEnum getVendorType();

   String getSubNamespace();
}
