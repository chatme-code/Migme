package com.projectgoth.fusion.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class HashUtils {
   private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
   private static final int FNV32_PRIME = 16777619;
   private static final long FNV32_HASH = 2166136261L;

   public static byte[] toByteArray(char[] input) {
      byte[] bytes = new byte[input.length];

      for(int i = 0; i < input.length; ++i) {
         bytes[i] = (byte)input[i];
      }

      return bytes;
   }

   public static byte[] sha256(String input) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         digest.update(input.getBytes("ISO8859_1"));
         return digest.digest();
      } catch (NoSuchAlgorithmException var2) {
      } catch (UnsupportedEncodingException var3) {
      }

      return new byte[0];
   }

   public static byte[] sha256(char[] input) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         digest.update(toByteArray(input));
         return digest.digest();
      } catch (NoSuchAlgorithmException var2) {
         return new byte[0];
      }
   }

   public static byte[] md5(String input) {
      try {
         MessageDigest digest = MessageDigest.getInstance("MD5");
         digest.update(input.getBytes("ISO8859_1"));
         return digest.digest();
      } catch (NoSuchAlgorithmException var2) {
      } catch (UnsupportedEncodingException var3) {
      }

      return new byte[0];
   }

   public static long md5asLong(String input) {
      byte[] data = md5(input);
      long result = (long)(data[3] & 255) << 24 | (long)(data[2] & 255) << 16 | (long)(data[1] & 255) << 8 | (long)(data[0] & 255);
      return result;
   }

   public static long fnv(String input) {
      byte[] data = input.getBytes();
      long hash = 2166136261L;
      byte[] arr$ = data;
      int len$ = data.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         byte b = arr$[i$];
         hash = (hash ^ (long)b) * 16777619L;
      }

      hash += hash << 13;
      hash ^= hash >> 7;
      hash += hash << 3;
      hash ^= hash >> 17;
      hash += hash << 5;
      return hash;
   }

   public static long truncateToUnsigned32Bits(long input) {
      return input & 4294967295L;
   }

   public static String asHex(byte[] hash) {
      char[] buf = new char[hash.length * 2];
      int i = 0;

      for(int var3 = 0; i < hash.length; ++i) {
         buf[var3++] = HEX_CHARS[hash[i] >>> 4 & 15];
         buf[var3++] = HEX_CHARS[hash[i] & 15];
      }

      return new String(buf);
   }
}
