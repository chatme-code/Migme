package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;

public final class SessionProxyArrayHelper {
   public static void write(BasicStream __os, SessionPrx[] __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.length);

         for(int __i0 = 0; __i0 < __v.length; ++__i0) {
            SessionPrxHelper.__write(__os, __v[__i0]);
         }
      }

   }

   public static SessionPrx[] read(BasicStream __is) {
      int __len0 = __is.readSize();
      __is.startSeq(__len0, 2);
      SessionPrx[] __v = new SessionPrx[__len0];

      for(int __i0 = 0; __i0 < __len0; ++__i0) {
         __v[__i0] = SessionPrxHelper.__read(__is);
         __is.checkSeq();
         __is.endElement();
      }

      __is.endSeq(__len0);
      return __v;
   }
}
