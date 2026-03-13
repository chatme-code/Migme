package com.projectgoth.fusion.payment;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.payment.creditcard.CreditCardPayment;
import com.projectgoth.fusion.payment.mimopay.MIMOPayment;
import com.projectgoth.fusion.payment.mol.MOLPayment;
import com.projectgoth.fusion.payment.paypal.PaypalPayment;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class PaymentFactory {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PaymentFactory.class));
   private static final Map<PaymentData.TypeEnum, PaymentInterface> PAYMENT_INTERFACE_LOOKUP = new HashMap();

   private PaymentFactory() {
   }

   public static PaymentInterface getPaymentDAO(String paymentTypeCodeStr) throws PaymentException {
      PaymentData.TypeEnum paymentTypeEnum = PaymentData.TypeEnum.fromCode(paymentTypeCodeStr);
      if (paymentTypeEnum == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_PAYMENT_VENDOR, new Object[]{paymentTypeCodeStr});
      } else {
         return getPaymentDAO(paymentTypeEnum);
      }
   }

   public static PaymentInterface getPaymentDAO(PaymentData.TypeEnum paymentTypeEnum) throws PaymentException {
      PaymentInterface paymentObject = (PaymentInterface)PAYMENT_INTERFACE_LOOKUP.get(paymentTypeEnum);
      if (paymentObject == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.PAYMENT_INTERFACE_NOT_REGISTERED, new Object[]{paymentTypeEnum});
      } else {
         return paymentObject;
      }
   }

   static {
      PAYMENT_INTERFACE_LOOKUP.put(PaymentData.TypeEnum.MOL, MOLPayment.getInstance());
      PAYMENT_INTERFACE_LOOKUP.put(PaymentData.TypeEnum.MIMOPAY, MIMOPayment.getInstance());
      PAYMENT_INTERFACE_LOOKUP.put(PaymentData.TypeEnum.PAYPAL, PaypalPayment.getInstance());
      PAYMENT_INTERFACE_LOOKUP.put(PaymentData.TypeEnum.CREDIT_CARD, CreditCardPayment.getInstance());
   }
}
