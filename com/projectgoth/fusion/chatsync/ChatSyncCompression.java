package com.projectgoth.fusion.chatsync;

import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFEncoder;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.slice.FusionException;

public class ChatSyncCompression {
   public static final char HEADER_SEPARATOR = '#';
   public static final String LZF_ALGO = "LZF";
   public static final String LZF_HEADER = "LZF#";
   public static final String GZIP_ALGO = "GZP";
   public static final String GZIP_HEADER = "GZP#";

   public static byte[] compressFromString(String value, String algo) throws FusionException {
      byte[] data = Redis.safeEncoderEncode(value);
      return compress(data, algo);
   }

   public static byte[] compress(byte[] data, String algo) throws FusionException {
      try {
         if (StringUtil.isBlank(algo)) {
            return data;
         } else if (algo.equals("LZF")) {
            byte[] compressedBytes = LZFEncoder.encode(data);
            byte[] headerPlusCompressed = new byte[compressedBytes.length + 4];
            headerPlusCompressed[0] = 76;
            headerPlusCompressed[1] = 90;
            headerPlusCompressed[2] = 70;
            headerPlusCompressed[3] = 35;
            System.arraycopy(compressedBytes, 0, headerPlusCompressed, 4, compressedBytes.length);
            return headerPlusCompressed;
         } else if (algo.equals("GZP")) {
            throw new FusionException("Unimplemented");
         } else {
            throw new FusionException("Unrecognized compression algorithm specified for chat sync msgs");
         }
      } catch (Exception var5) {
         throw new FusionException("Compression of message failed: " + var5.getMessage());
      }
   }

   public static String adaptiveDecompressToString(byte[] storedBytes) throws FusionException {
      byte[] decompressed = adaptiveDecompress(storedBytes);
      String sDecompressed = Redis.safeEncoderEncode(decompressed);
      return sDecompressed;
   }

   public static byte[] adaptiveDecompress(byte[] storedBytes) throws FusionException {
      if (bytesStartWith(storedBytes, "LZF#")) {
         try {
            return LZFDecoder.decode(storedBytes, "LZF#".length(), storedBytes.length - "LZF#".length());
         } catch (Exception var2) {
            throw new FusionException("LZF decompression of stored data failed: " + var2.getMessage());
         }
      } else {
         return storedBytes;
      }
   }

   private static boolean bytesStartWith(byte[] test, String startsWith) {
      if (test.length < startsWith.length()) {
         return false;
      } else {
         for(int i = 0; i < startsWith.length(); ++i) {
            if (test[i] != startsWith.charAt(i)) {
               return false;
            }
         }

         return true;
      }
   }
}
