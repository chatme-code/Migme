package com.projectgoth.fusion.payment.paypal;

import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.payment.PaymentException;
import com.projectgoth.fusion.payment.PaymentIResponse;
import org.json.JSONException;
import org.json.JSONObject;

public class PaypalPaymentIResponse extends PaymentIResponse {
   public String token;
   public Double amount;
   public String currency;
   public Double amountUSD;
   public String result;

   public String toJSON(PaymentIResponse.ReturnType returnType) throws JSONException {
      if (returnType == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"An error occurred in your request. Please contact merchant@mig.me."});
      } else {
         JSONObject response = new JSONObject();
         switch(returnType) {
         case CREATE:
            response.put("token", this.token);
            return response.toString();
         case GET_COMPACT_DETAILS:
            response.put("amount", this.amount);
            response.put("currency", this.currency);
            response.put("result", this.result);
            response.put("amountUSD", this.amountUSD);
            return response.toString();
         default:
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ERROR, new Object[]{"An error occurred in your request. Please contact merchant@mig.me."});
         }
      }
   }
}
