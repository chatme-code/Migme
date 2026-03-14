package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;

public final class ByteArrayHelper {
   public static void write(BasicStream __os, byte[] __v) {
      __os.writeByteSeq(__v);
   }

   public static byte[] read(BasicStream __is) {
      byte[] __v = __is.readByteSeq();
      return __v;
   }
}
