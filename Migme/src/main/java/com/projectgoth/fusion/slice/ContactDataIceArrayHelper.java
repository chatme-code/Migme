package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;

public final class ContactDataIceArrayHelper {
   public static void write(BasicStream __os, ContactDataIce[] __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.length);

         for(int __i0 = 0; __i0 < __v.length; ++__i0) {
            __v[__i0].__write(__os);
         }
      }

   }

   public static ContactDataIce[] read(BasicStream __is) {
      int __len0 = __is.readSize();
      __is.startSeq(__len0, 76);
      ContactDataIce[] __v = new ContactDataIce[__len0];

      for(int __i0 = 0; __i0 < __len0; ++__i0) {
         __v[__i0] = new ContactDataIce();
         __v[__i0].__read(__is);
         __is.checkSeq();
         __is.endElement();
      }

      __is.endSeq(__len0);
      return __v;
   }
}
