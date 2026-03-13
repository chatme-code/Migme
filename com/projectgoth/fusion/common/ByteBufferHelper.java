package com.projectgoth.fusion.common;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteBufferHelper {
   private static final String READ_MODE = "r";

   public static String readLine(ByteBuffer buffer, String charset) throws UnsupportedEncodingException {
      int position = buffer.position();
      int limit = buffer.limit();

      for(int i = position; i < limit; ++i) {
         if (buffer.get(i) == 13) {
            if (i < limit - 1 && buffer.get(i + 1) == 10) {
               return new String(readBytes(buffer, i - position + 2), 0, i - position, charset);
            }

            return new String(readBytes(buffer, i - position + 1), 0, i - position, charset);
         }

         if (buffer.get(i) == 10) {
            return new String(readBytes(buffer, i - position + 1), 0, i - position, charset);
         }
      }

      throw new BufferUnderflowException();
   }

   public static String readCString(ByteBuffer buffer, String charset) throws UnsupportedEncodingException {
      int position = buffer.position();
      int limit = buffer.limit();

      for(int i = position; i < limit; ++i) {
         if (buffer.get(i) == 0) {
            return new String(readBytes(buffer, i - position + 1), 0, i - position, charset);
         }
      }

      throw new BufferUnderflowException();
   }

   public static ByteBuffer adjustSize(ByteBuffer buffer, int minSize, int maxSize, double growFactor) throws BufferOverflowException {
      int capacity = buffer.capacity();
      int position = buffer.position();
      if (capacity == position) {
         if (capacity > maxSize) {
            throw new BufferOverflowException();
         }

         buffer = ByteBuffer.allocate((int)((double)capacity * growFactor)).put(buffer.array());
      } else if (capacity > minSize && position < minSize) {
         buffer = ByteBuffer.allocate(minSize).put(buffer.array(), 0, position);
      }

      return buffer;
   }

   public static ByteBuffer readFile(File file) throws IOException {
      RandomAccessFile raf = new RandomAccessFile(file, "r");

      ByteBuffer var3;
      try {
         byte[] ba = new byte[(int)file.length()];
         raf.readFully(ba);
         var3 = ByteBuffer.wrap(ba);
      } finally {
         raf.close();
      }

      return var3;
   }

   public static byte[] readBytes(ByteBuffer buffer, int len) {
      byte[] ba = new byte[len];
      buffer.get(ba);
      return ba;
   }

   public static byte[] concat(byte[] a, byte[] b) {
      byte[] c = new byte[a.length + b.length];
      System.arraycopy(a, 0, c, 0, a.length);
      System.arraycopy(b, 0, c, a.length, b.length);
      return c;
   }

   public static int compare(byte[] a, byte[] b) {
      if (a.length < b.length) {
         return -1;
      } else if (a.length > b.length) {
         return 1;
      } else {
         for(int i = 0; i < a.length; ++i) {
            if (a[i] < b[i]) {
               return -1;
            }

            if (a[i] > b[i]) {
               return 1;
            }
         }

         return 0;
      }
   }

   public static String toHexString(byte[] ba) {
      return toHexString(ba, "");
   }

   public static String toHexString(byte[] ba, String separator) {
      StringBuilder builder = new StringBuilder();
      byte[] arr$ = ba;
      int len$ = ba.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         byte b = arr$[i$];
         builder.append(Integer.toHexString(b >> 4 & 15)).append(Integer.toHexString(b & 15)).append(separator);
      }

      return builder.toString();
   }

   public static String toPrintableString(ByteBuffer buffer) {
      ByteBuffer b = buffer.duplicate();
      String s = new String(readBytes(b, b.limit()));
      s = s.replaceAll("\r", "\\\\r");
      s = s.replaceAll("\n", "\\\\n\n");
      return s;
   }
}
