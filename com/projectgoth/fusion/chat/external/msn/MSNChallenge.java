package com.projectgoth.fusion.chat.external.msn;

import com.projectgoth.fusion.common.StringUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MSNChallenge {
   private static final long CHALLENGE_MAGIC = 242854337L;

   public static String getResponse(String productID, String productKey, String challenge) throws NoSuchAlgorithmException {
      String idChallenge = challenge + productID + StringUtil.repeat("0", 8 - (challenge.length() + productID.length()) % 8);
      String keyChallenge = challenge + productKey;
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      ByteBuffer md5Hash = ByteBuffer.wrap(md5.digest(keyChallenge.getBytes())).order(ByteOrder.LITTLE_ENDIAN);
      ByteBuffer idBuffer = ByteBuffer.wrap(idChallenge.getBytes()).order(ByteOrder.LITTLE_ENDIAN);
      int[] md5Parts = new int[4];

      for(int i = 0; i < 4; ++i) {
         md5Parts[i] = md5Hash.getInt(i * 4) & Integer.MAX_VALUE;
      }

      long high = 0L;
      long low = 0L;

      for(int i = 0; i < idBuffer.capacity() / 4; i += 2) {
         long temp = ((242854337L * (long)idBuffer.getInt() % 2147483647L + high) * (long)md5Parts[0] + (long)md5Parts[1]) % 2147483647L;
         high = (((long)idBuffer.getInt() + temp) % 2147483647L * (long)md5Parts[2] + (long)md5Parts[3]) % 2147483647L;
         low = low + high + temp;
      }

      high = (high + (long)md5Parts[1]) % 2147483647L;
      low = (low + (long)md5Parts[3]) % 2147483647L;
      long key = Long.reverseBytes(high) | Long.reverseBytes(low) >>> 32;
      md5Hash.order(ByteOrder.BIG_ENDIAN);
      return StringUtil.padLeft(Long.toHexString(md5Hash.getLong() ^ key), '0', 16) + StringUtil.padLeft(Long.toHexString(md5Hash.getLong() ^ key), '0', 16);
   }

   public static String getResponseYourPaste(String chlid) {
      String hash = toMD5(chlid + "CFHUR$52U_{VIX5T");
      long[] splitHash = new long[4];

      for(int i = 0; i < 4; ++i) {
         String sub = reverseHex(hash.substring(8 * i, 8 * i + 8));
         splitHash[i] = 2147483647L & Long.parseLong(sub, 16);
      }

      String challenge = chlid + "PROD0101{0RM?UBW";
      int addZeroes = 8 - challenge.length() % 8;

      for(int i = 0; i < addZeroes; ++i) {
         challenge = challenge + "0";
      }

      long[] splitChallenge = new long[challenge.length() / 4];

      int i;
      for(int i = 0; i < splitChallenge.length; ++i) {
         String sub = challenge.substring(4 * i, 4 * i + 4);
         char[] chars = new char[4];
         sub.getChars(0, sub.length(), chars, 0);
         String hex = "";
         char[] arr$ = chars;
         i = chars.length;

         for(int i$ = 0; i$ < i; ++i$) {
            char c = arr$[i$];
            hex = hex + Integer.toHexString(c);
         }

         hex = reverseHex(hex);
         splitChallenge[i] = Long.parseLong(hex, 16);
      }

      long high = 0L;
      long low = 0L;

      for(int i = 0; i < splitChallenge.length; i += 2) {
         long temp = splitChallenge[i];
         temp = 242854337L * temp % 2147483647L;
         temp += high;
         temp = splitHash[0] * temp + splitHash[1];
         temp %= 2147483647L;
         high = splitChallenge[i + 1];
         high = (high + temp) % 2147483647L;
         high = splitHash[2] * high + splitHash[3];
         high %= 2147483647L;
         low += high + temp;
      }

      high = reverseHex((high + splitHash[1]) % 2147483647L);
      low = reverseHex((low + splitHash[3]) % 2147483647L);
      StringBuilder sb = new StringBuilder();

      for(i = 0; i < 4; ++i) {
         long highlow = i != 0 && i != 2 ? low : high;
         String s = Long.toHexString(Long.parseLong(hash.substring(8 * i, 8 * i + 8), 16) ^ highlow);
         addZeroes = 8 - s.length();

         for(int j = 0; j < addZeroes; ++j) {
            s = "0" + s;
         }

         sb.append(s);
      }

      return sb.toString();
   }

   private static String toMD5(String plain) {
      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         md.update(plain.getBytes());
         byte[] hash = md.digest();
         StringBuffer hexstring = new StringBuffer();

         for(int i = 0; i < hash.length; ++i) {
            String b = Integer.toHexString(255 & hash[i]);
            if (b.length() == 1) {
               b = "0" + b;
            }

            hexstring.append(b);
         }

         return hexstring.toString().toLowerCase();
      } catch (NoSuchAlgorithmException var6) {
         return null;
      }
   }

   private static long reverseHex(long num) {
      return Long.parseLong(reverseHex(Long.toHexString(num)), 16);
   }

   private static String reverseHex(String hex) {
      if (hex.length() % 2 == 1) {
         hex = "0" + hex;
      }

      StringBuilder reversedHex = new StringBuilder();

      for(int i = hex.length(); i >= 2; i -= 2) {
         reversedHex.append(hex.substring(i - 2, i));
      }

      return reversedHex.toString();
   }

   public static void mainTest() {
      try {
         System.out.println(getResponse("PROD0101{0RM?UBW", "CFHUR$52U_{VIX5T", "20268510620116082128476729"));
         System.out.println(getResponseYourPaste("20268510620116082128476729"));
      } catch (Exception var1) {
         var1.printStackTrace();
      }

   }
}
