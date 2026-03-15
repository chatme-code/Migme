package com.projectgoth.fusion.payment;

import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.payment.creditcard.CreditCardData;
import com.projectgoth.fusion.payment.mimopay.MIMOVoucherData;
import com.projectgoth.fusion.payment.mol.MOLPaymentData;
import com.projectgoth.fusion.payment.paypal.PaypalPaymentData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class PaymentDataFactory {
   private static final Map<Integer, PaymentDataFactory.ObjectCreator> objectCreatorLookup = new HashMap();

   public static <T extends PaymentData> T getPayment(ResultSet rs) throws SQLException, Exception {
      Integer type = rs.getInt("type");
      PaymentDataFactory.ObjectCreator objectCreator = (PaymentDataFactory.ObjectCreator)objectCreatorLookup.get(type);
      if (type != null) {
         return objectCreator.createFrom(rs);
      } else {
         throw new Exception("Unknown payment type: [" + type + "]");
      }
   }

   public static <T extends PaymentData> T getPayment(PaymentData.TypeEnum type, JSONObject jsonObject) throws JSONException, ParseException, PaymentException {
      jsonObject.put("type", type.value());
      return getPayment(jsonObject);
   }

   public static <T extends PaymentData> T getPayment(JSONObject jsonObject) throws JSONException, ParseException, PaymentException {
      Integer type = jsonObject.getInt("type");
      PaymentDataFactory.ObjectCreator objectCreator = (PaymentDataFactory.ObjectCreator)objectCreatorLookup.get(type);
      if (type != null) {
         return objectCreator.createFrom(jsonObject);
      } else {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.UNKNOWN_CREDIT_RECHARGE_TYPE, new Object[]{type});
      }
   }

   static {
      objectCreatorLookup.put(PaymentData.TypeEnum.MOL.value(), MOLPaymentData.OBJECT_CREATOR);
      objectCreatorLookup.put(PaymentData.TypeEnum.MIMOPAY.value(), MIMOVoucherData.OBJECT_CREATOR);
      objectCreatorLookup.put(PaymentData.TypeEnum.PAYPAL.value(), PaypalPaymentData.OBJECT_CREATOR);
      objectCreatorLookup.put(PaymentData.TypeEnum.CREDIT_CARD.value(), CreditCardData.OBJECT_CREATOR);
   }

   public interface ObjectCreator {
      PaymentData createFrom(ResultSet var1) throws SQLException;

      PaymentData createFrom(JSONObject var1) throws JSONException, ParseException;
   }
}
