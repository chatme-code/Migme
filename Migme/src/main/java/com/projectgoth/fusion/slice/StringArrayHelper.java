package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;

public final class StringArrayHelper {
   public static void write(BasicStream __os, String[] __v) {
      __os.writeStringSeq(__v);
   }

   public static String[] read(BasicStream __is) {
      String[] __v = __is.readStringSeq();
      return __v;
   }
}
