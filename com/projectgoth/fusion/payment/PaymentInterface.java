package com.projectgoth.fusion.payment;

import com.projectgoth.fusion.data.UserData;
import java.util.Map;
import org.json.JSONObject;

public interface PaymentInterface {
   boolean isAccessAllowed(UserData var1) throws PaymentException, Exception;

   Map<String, Object> clientInitiatePayment(JSONObject var1) throws PaymentException, Exception;

   Map<String, Object> updatePaymentStatus(JSONObject var1) throws Exception;

   String getCurrencyForUser(UserData var1);

   <T extends PaymentData> PaymentIResponse clientInitiatePayment(T var1) throws PaymentException, Exception;

   <T extends PaymentData> PaymentIResponse updatePaymentStatus(T var1) throws PaymentException, Exception;

   PaymentIResponse onPaymentAuthorized(String var1, JSONObject var2) throws PaymentException, Exception;

   <T extends PaymentData> PaymentIResponse approve(T var1, String var2) throws PaymentException, Exception;

   <T extends PaymentData> PaymentIResponse reject(T var1, String var2) throws PaymentException, Exception;
}
