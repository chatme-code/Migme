package com.projectgoth.fusion.common;

public class SimpleCryptUtils {
   public static byte[] xor(String inputText, String key) {
      return xor(inputText, key.getBytes());
   }

   public static byte[] xor(String inputText, byte[] key) {
      byte[] clearTextBytes = inputText.getBytes();
      byte[] cipherTextBytes = new byte[clearTextBytes.length];

      for(int i = 0; i < clearTextBytes.length; ++i) {
         cipherTextBytes[i] = (byte)(clearTextBytes[i] ^ key[i % key.length]);
      }

      return cipherTextBytes;
   }
}
