package com.projectgoth.fusion.payment.creditcard;

import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentException;
import com.projectgoth.fusion.payment.PaymentIResponse;
import org.json.JSONException;
import org.json.JSONObject;

public class CreditCardIResponse extends PaymentIResponse {
   public String result;
   public String mac;
   public String returnMac;
   public String returnUrl;
   public String amount;
   public String currency;
   public String message;
   public String status;
   public String vendorTransactionId;

   public String toJSON(PaymentIResponse.ReturnType returnType) throws JSONException {
      if (returnType == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"An error occurred in your request. Please contact merchant@mig.me."});
      } else {
         JSONObject response = new JSONObject();
         switch(returnType) {
         case CREATE:
            response.put("redirectUrl", this.returnUrl);
            response.put("ref", this.vendorTransactionId);
            response.put("returnMac", this.returnMac);
            break;
         case GET_COMPACT_DETAILS:
            response.put("status", this.result);
            response.put("amount", this.amount);
            response.put("currency", this.currency);
            if (this.result.equals(PaymentData.StatusEnum.REJECTED.name())) {
               response.put("message", "Your payment has been rejected.");
            } else if (this.result.equals(PaymentData.StatusEnum.APPROVED.name())) {
               response.put("message", "Your payment has been approved.");
            } else if (null == this.message) {
               response.put("message", "Your payment is still being processed.");
            } else {
               response.put("message", this.message);
            }
            break;
         case APPROVE:
         case REJECT:
            response.put("status", this.status);
            response.put("amount", this.amount);
            response.put("currency", this.currency);
            break;
         default:
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"An error occurred in your request. Please contact merchant@mig.me."});
         }

         return response.toString();
      }
   }
}
