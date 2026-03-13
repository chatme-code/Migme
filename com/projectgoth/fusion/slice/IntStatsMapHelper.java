package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class IntStatsMapHelper {
   public static void write(BasicStream __os, Map<String, Integer> __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.size());
         Iterator i$ = __v.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, Integer> __e = (Entry)i$.next();
            __os.writeString((String)__e.getKey());
            __os.writeInt((Integer)__e.getValue());
         }
      }

   }

   public static Map<String, Integer> read(BasicStream __is) {
      Map<String, Integer> __v = new HashMap();
      int __sz0 = __is.readSize();

      for(int __i0 = 0; __i0 < __sz0; ++__i0) {
         String __key = __is.readString();
         int __value = __is.readInt();
         __v.put(__key, __value);
      }

      return __v;
   }
}
