package com.projectgoth.fusion.payment.paypal;

import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.JSONUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.UsernameUtils;
import com.projectgoth.fusion.payment.PaymentException;
import java.text.ParseException;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

public class PaypalTransactionTokenData {
   public String username;
   public Integer paymentID;
   public double amount;
   public String currency;
   public String paypalUserID;
   public Date createDate;
   public Date expireDate;
   public double usdAmount = 0.0D;

   public static PaypalTransactionTokenData createFromJSONString(String jsonString) throws JSONException, ParseException {
      if (jsonString == null) {
         return null;
      } else {
         JSONObject jsonObj = new JSONObject(jsonString);
         PaypalTransactionTokenData tokenData = new PaypalTransactionTokenData();
         String username = JSONUtils.getString(jsonObj, "username");
         if (username == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"username"});
         } else {
            tokenData.username = UsernameUtils.normalizeUsername(username);
            tokenData.paymentID = JSONUtils.getInteger(jsonObj, "paymentID");
            tokenData.currency = JSONUtils.getString(jsonObj, "currency");
            Double amount = JSONUtils.getDouble(jsonObj, "amount");
            if (amount == null) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"amount"});
            } else {
               Double usdAmount = JSONUtils.getDouble(jsonObj, "usdAmount");
               if (usdAmount != null && !usdAmount.isNaN() && !(usdAmount < 0.0D)) {
                  tokenData.usdAmount = usdAmount;
               } else {
                  tokenData.usdAmount = 0.0D;
               }

               tokenData.amount = amount;
               tokenData.paypalUserID = JSONUtils.getString(jsonObj, "paypalUserID");
               String createDateStr = JSONUtils.getString(jsonObj, "createDate");
               if (StringUtil.isBlank(createDateStr)) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"createDate"});
               } else {
                  tokenData.createDate = DateTimeUtils.getPaymentTransactionTimeFromString(createDateStr);
                  String expireDateStr = JSONUtils.getString(jsonObj, "expireDate");
                  if (StringUtil.isBlank(expireDateStr)) {
                     throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"expireDate"});
                  } else {
                     tokenData.expireDate = DateTimeUtils.getPaymentTransactionTimeFromString(expireDateStr);
                     return tokenData;
                  }
               }
            }
         }
      }
   }

   public String toJSONString() throws JSONException {
      JSONObject jsonObj = new JSONObject();
      jsonObj.put("currency", this.currency);
      jsonObj.put("username", UsernameUtils.normalizeUsername(this.username));
      jsonObj.put("amount", this.amount);
      jsonObj.put("paymentID", this.paymentID);
      jsonObj.put("paypalUserID", this.paypalUserID);
      jsonObj.put("usdAmount", this.usdAmount);
      if (this.createDate != null) {
         jsonObj.put("createDate", DateTimeUtils.getStringForPaymentTransactionTime(this.createDate));
      }

      if (this.expireDate != null) {
         jsonObj.put("expireDate", DateTimeUtils.getStringForPaymentTransactionTime(this.expireDate));
      }

      return jsonObj.toString();
   }
}
