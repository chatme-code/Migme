package com.projectgoth.fusion.clientsession;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.HashUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class CodeIgniterEncryptionUtil {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CodeIgniterEncryptionUtil.class));
   private static final int BLOCK_SIZE = 16;

   public static String encryptToBase64(String data) {
      log.debug(String.format("encoding data %s", data));
      return encryptToBase64(data, SSOLogin.getSSOEIDEncryptionKeyInBytes());
   }

   public static String decryptFromBase64(String cipher) {
      if (cipher != null && cipher.indexOf("%") >= 0) {
         try {
            cipher = URLDecoder.decode(cipher, "UTF-8");
         } catch (UnsupportedEncodingException var2) {
            log.warn(String.format("Unable to url decode cipher, using the un-url-decoded version: %s", cipher));
         }
      }

      log.debug(String.format("decoding cipher %s", cipher));
      return decryptFromBase64(cipher, SSOLogin.getSSOEIDEncryptionKeyInBytes());
   }

   public static String encryptToBase64(String data, byte[] keyBytes) {
      try {
         byte[] encryptedBytes = encryptRaw(data.getBytes("UTF-8"), keyBytes);
         return encryptedBytes != null ? new String(Base64.encodeBase64(addNoise(encryptedBytes, keyBytes))) : null;
      } catch (UnsupportedEncodingException var3) {
         return null;
      }
   }

   public static String encryptToBase64(String data, byte[] keyBytes, byte[] iv) {
      try {
         return new String(Base64.encodeBase64(addNoise(encryptRaw(data.getBytes("UTF-8"), keyBytes, iv), keyBytes)));
      } catch (UnsupportedEncodingException var4) {
         return null;
      }
   }

   private static byte[] generateIV() {
      KeyGenerator keyGen = null;

      try {
         keyGen = KeyGenerator.getInstance("AES");
      } catch (NoSuchAlgorithmException var2) {
         log.error(String.format("Unable to get KeyGenerator for AES algorithm, using 0 bytes"), var2);
         return new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      }

      keyGen.init(128);
      return keyGen.generateKey().getEncoded();
   }

   private static Cipher createCipher(int mode, byte[] keyBytes, byte[] ivBytes) {
      SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
      IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
      Cipher cipher = null;

      try {
         cipher = Cipher.getInstance("AES/CBC/NoPadding");
      } catch (NoSuchAlgorithmException var9) {
         log.error(String.format("Unable to get cipher instance"), var9);
         return null;
      } catch (NoSuchPaddingException var10) {
         log.error(String.format("Unable to get cipher instance"), var10);
         return null;
      }

      try {
         cipher.init(mode, keySpec, ivSpec);
         return cipher;
      } catch (InvalidKeyException var7) {
         log.error(String.format("Unable to init cipher"), var7);
         return null;
      } catch (InvalidAlgorithmParameterException var8) {
         log.error(String.format("Unable to init cipher"), var8);
         return null;
      }
   }

   private static byte[] encryptRaw(byte[] dataBytes, byte[] keyBytes) {
      return encryptRaw(dataBytes, keyBytes, generateIV());
   }

   private static byte[] encryptRaw(byte[] dataBytes, byte[] keyBytes, byte[] ivBytes) {
      Cipher cipher = createCipher(1, keyBytes, ivBytes);
      if (cipher == null) {
         return null;
      } else {
         int dataSize = dataBytes.length;
         byte[] paddedDataBytes = null;
         int lenToPad = 16 - dataSize % 16;
         byte[] paddedDataBytes;
         if (lenToPad == 16 && dataSize > 0) {
            paddedDataBytes = dataBytes;
         } else {
            paddedDataBytes = new byte[dataSize + lenToPad];
            Arrays.fill(paddedDataBytes, (byte)0);
            System.arraycopy(dataBytes, 0, paddedDataBytes, 0, dataSize);
         }

         Object var7 = null;

         byte[] cipherBytes;
         try {
            cipherBytes = cipher.doFinal(paddedDataBytes);
         } catch (IllegalBlockSizeException var11) {
            log.error(String.format("Unable to encrypt using cipher"), var11);
            return null;
         } catch (BadPaddingException var12) {
            log.error(String.format("Unable to encrypt using cipher"), var12);
            return null;
         }

         int cipherSize = cipherBytes.length;
         int ivSize = ivBytes.length;
         byte[] cipherWithIVBytes = new byte[ivSize + cipherSize];
         System.arraycopy(ivBytes, 0, cipherWithIVBytes, 0, ivSize);
         System.arraycopy(cipherBytes, 0, cipherWithIVBytes, ivSize, cipherSize);
         return cipherWithIVBytes;
      }
   }

   private static String getSha1InHex(byte[] keyBytes) {
      MessageDigest md = null;

      try {
         md = MessageDigest.getInstance("SHA-1");
      } catch (NoSuchAlgorithmException var3) {
         log.error(String.format("Unable to get message digest instance for sha1"), var3);
         return null;
      }

      md.reset();
      md.update(keyBytes);
      byte[] hashBinary = md.digest();
      return HashUtils.asHex(hashBinary);
   }

   private static byte[] addNoise(byte[] cipherBytes, byte[] keyBytes) {
      byte[] hashHex = getSha1InHex(keyBytes).getBytes();
      int hashSize = hashHex.length;
      int cipherSize = cipherBytes.length;
      byte[] cipherWithNoise = new byte[cipherSize];
      int i = 0;

      for(int j = 0; i < cipherSize; ++j) {
         if (j >= hashSize) {
            j = 0;
         }

         cipherWithNoise[i] = (byte)((cipherBytes[i] + hashHex[j]) % 256);
         ++i;
      }

      return cipherWithNoise;
   }

   private static byte[] removeNoise(byte[] cipherWithNoiseBytes, byte[] keyBytes) {
      byte[] hashHex = getSha1InHex(keyBytes).getBytes();
      int hashSize = hashHex.length;
      int cipherSize = cipherWithNoiseBytes.length;
      byte[] cipherWithoutNoise = new byte[cipherSize];
      int i = 0;

      for(int j = 0; i < cipherSize; ++j) {
         if (j >= hashSize) {
            j = 0;
         }

         cipherWithoutNoise[i] = (byte)((cipherWithNoiseBytes[i] - hashHex[j]) % 256);
         ++i;
      }

      return cipherWithoutNoise;
   }

   public static String decryptFromBase64(String cipher, byte[] keyBytes) {
      try {
         byte[] decryptedBytes = decryptRaw(removeNoise(Base64.decodeBase64(cipher.getBytes("UTF-8")), keyBytes), keyBytes);
         return decryptedBytes != null ? new String(decryptedBytes) : null;
      } catch (UnsupportedEncodingException var3) {
         return null;
      }
   }

   private static byte[] decryptRaw(byte[] cBytes, byte[] keyBytes) {
      if (16 > cBytes.length) {
         log.error(String.format("Incorrect cipher bytes: cipher bytes %d is shorter than expected IV size %d", cBytes.length, 16));
         return null;
      } else {
         int dataSize = cBytes.length - 16;
         byte[] ivBytes = new byte[16];
         System.arraycopy(cBytes, 0, ivBytes, 0, 16);
         byte[] cipherDataBytes = new byte[dataSize];
         System.arraycopy(cBytes, 16, cipherDataBytes, 0, dataSize);
         Cipher cipher = createCipher(2, keyBytes, ivBytes);
         if (cipher == null) {
            return null;
         } else {
            Object var6 = null;

            byte[] cipherBytes;
            try {
               cipherBytes = cipher.doFinal(cipherDataBytes);
            } catch (IllegalBlockSizeException var10) {
               log.error(String.format("Unable to decrypt using cipher"), var10);
               return null;
            } catch (BadPaddingException var11) {
               log.error(String.format("Unable to decrypt using cipher"), var11);
               return null;
            }

            int i;
            for(i = cipherBytes.length - 1; i >= 0 && cipherBytes[i] == 0; --i) {
            }

            int cipherSize = cipherBytes.length;
            if (i < 0) {
               return new byte[0];
            } else {
               if (i < cipherSize) {
                  byte[] unpaddedCipherBytes = new byte[i + 1];
                  System.arraycopy(cipherBytes, 0, unpaddedCipherBytes, 0, i + 1);
                  cipherBytes = unpaddedCipherBytes;
               }

               return cipherBytes;
            }
         }
      }
   }
}
