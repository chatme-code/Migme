package com.projectgoth.fusion.common;

import java.text.DecimalFormat;

public class Numerics {
   private static final int MAX_DECIMAL_PLACES = 6;
   private static final double ROUNDING_FACTOR = 5.0E-7D;
   private static final int[] dps = new int[7];
   public static final ThreadLocal<DecimalFormat> TL_TWO_DECIMAL_DIGITS_FORMAT;

   public static String toTwoDecMoneyDigit(double d) {
      double abs_d = Math.abs(d);
      if (abs_d == 0.0D) {
         return "0.00";
      } else {
         String str = ((DecimalFormat)TL_TWO_DECIMAL_DIGITS_FORMAT.get()).format(abs_d);
         return !(d >= 0.0D) && !str.equals("0.00") ? "-" + str : str;
      }
   }

   public static double round(double value, int decimalPlaces) {
      return Math.rint(value * (double)dps[decimalPlaces] + 5.0E-7D) / (double)dps[decimalPlaces];
   }

   public static double floor(double value, int decimalPlaces) {
      return Math.floor(value * (double)dps[decimalPlaces] + 5.0E-7D) / (double)dps[decimalPlaces];
   }

   public static double ceil(double value, int decimalPlaces) {
      return Math.ceil(value * (double)dps[decimalPlaces] - 5.0E-7D) / (double)dps[decimalPlaces];
   }

   public static int floorInt(int value, int toNearest) {
      return value / toNearest * toNearest;
   }

   public static int ceilInt(int value, int toNearest) {
      return (value + toNearest - 1) / toNearest * toNearest;
   }

   static {
      for(int i = 0; i < dps.length; ++i) {
         dps[i] = (int)Math.pow(10.0D, (double)i);
      }

      TL_TWO_DECIMAL_DIGITS_FORMAT = new ThreadLocal<DecimalFormat>() {
         protected DecimalFormat initialValue() {
            return new DecimalFormat("0.00");
         }
      };
   }
}
