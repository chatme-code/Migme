package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;

public final class SuspectIceArrayHelper {
   public static void write(BasicStream __os, SuspectIce[] __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.length);

         for(int __i0 = 0; __i0 < __v.length; ++__i0) {
            __v[__i0].__write(__os);
         }
      }

   }

   public static SuspectIce[] read(BasicStream __is) {
      int __len0 = __is.readSize();
      __is.startSeq(__len0, 30);
      SuspectIce[] __v = new SuspectIce[__len0];

      for(int __i0 = 0; __i0 < __len0; ++__i0) {
         __v[__i0] = new SuspectIce();
         __v[__i0].__read(__is);
         __is.checkSeq();
         __is.endElement();
      }

      __is.endSeq(__len0);
      return __v;
   }
}
