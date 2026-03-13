package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;

public final class IntArrayHelper {
   public static void write(BasicStream __os, int[] __v) {
      __os.writeIntSeq(__v);
   }

   public static int[] read(BasicStream __is) {
      int[] __v = __is.readIntSeq();
      return __v;
   }
}
