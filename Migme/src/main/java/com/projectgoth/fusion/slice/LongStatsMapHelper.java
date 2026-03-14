package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class LongStatsMapHelper {
   public static void write(BasicStream __os, Map<String, Long> __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.size());
         Iterator i$ = __v.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, Long> __e = (Entry)i$.next();
            __os.writeString((String)__e.getKey());
            __os.writeLong((Long)__e.getValue());
         }
      }

   }

   public static Map<String, Long> read(BasicStream __is) {
      Map<String, Long> __v = new HashMap();
      int __sz0 = __is.readSize();

      for(int __i0 = 0; __i0 < __sz0; ++__i0) {
         String __key = __is.readString();
         long __value = __is.readLong();
         __v.put(__key, __value);
      }

      return __v;
   }
}
