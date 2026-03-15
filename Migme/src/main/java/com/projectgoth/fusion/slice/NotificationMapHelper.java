package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class NotificationMapHelper {
   public static void write(BasicStream __os, Map<Integer, Integer> __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.size());
         Iterator i$ = __v.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<Integer, Integer> __e = (Entry)i$.next();
            __os.writeInt((Integer)__e.getKey());
            __os.writeInt((Integer)__e.getValue());
         }
      }

   }

   public static Map<Integer, Integer> read(BasicStream __is) {
      Map<Integer, Integer> __v = new HashMap();
      int __sz0 = __is.readSize();

      for(int __i0 = 0; __i0 < __sz0; ++__i0) {
         int __key = __is.readInt();
         int __value = __is.readInt();
         __v.put(__key, __value);
      }

      return __v;
   }
}
