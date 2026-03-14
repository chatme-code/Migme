package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;

public final class ScoreAndLevelSequenceHelper {
   public static void write(BasicStream __os, ScoreAndLevel[] __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.length);

         for(int __i0 = 0; __i0 < __v.length; ++__i0) {
            __v[__i0].__write(__os);
         }
      }

   }

   public static ScoreAndLevel[] read(BasicStream __is) {
      int __len0 = __is.readSize();
      __is.checkFixedSeq(__len0, 8);
      ScoreAndLevel[] __v = new ScoreAndLevel[__len0];

      for(int __i0 = 0; __i0 < __len0; ++__i0) {
         __v[__i0] = new ScoreAndLevel();
         __v[__i0].__read(__is);
      }

      return __v;
   }
}
