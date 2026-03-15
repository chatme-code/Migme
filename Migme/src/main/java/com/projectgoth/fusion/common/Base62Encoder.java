package com.projectgoth.fusion.common;

public class Base62Encoder {
   public static String encode(int num) {
      return encode((long)num);
   }

   public static String encode(long num) {
      if (num == 0L) {
         return "0";
      } else {
         StringBuilder builder;
         for(builder = new StringBuilder(); num > 0L; num /= 62L) {
            long r = num % 62L;
            if (r < 10L) {
               builder.append(r);
            } else if (r < 36L) {
               builder.append((char)((int)(97L + r - 10L)));
            } else {
               builder.append((char)((int)(65L + r - 36L)));
            }
         }

         return builder.toString();
      }
   }
}
