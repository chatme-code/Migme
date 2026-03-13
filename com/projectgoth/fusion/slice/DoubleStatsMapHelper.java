package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class DoubleStatsMapHelper {
   public static void write(BasicStream __os, Map<String, Double> __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.size());
         Iterator i$ = __v.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, Double> __e = (Entry)i$.next();
            __os.writeString((String)__e.getKey());
            __os.writeDouble((Double)__e.getValue());
         }
      }

   }

   public static Map<String, Double> read(BasicStream __is) {
      Map<String, Double> __v = new HashMap();
      int __sz0 = __is.readSize();

      for(int __i0 = 0; __i0 < __sz0; ++__i0) {
         String __key = __is.readString();
         double __value = __is.readDouble();
         __v.put(__key, __value);
      }

      return __v;
   }
}
