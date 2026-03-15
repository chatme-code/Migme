package com.projectgoth.fusion.common;

import java.text.DecimalFormat;

public class PaymentUtils {
   public static String normalizeCurrency(String currencyString) {
      return currencyString == null ? null : StringUtil.trimmedUpperCase(currencyString);
   }

   public static String formatAmountInCurrency(double amount) {
      amount = Math.ceil(amount * 100.0D);
      DecimalFormat df = new DecimalFormat("0.00");
      return df.format(amount / 100.0D);
   }
}
