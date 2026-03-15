package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class ParamMapHelper {
   public static void write(BasicStream __os, Map<String, String> __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.size());
         Iterator i$ = __v.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, String> __e = (Entry)i$.next();
            __os.writeString((String)__e.getKey());
            __os.writeString((String)__e.getValue());
         }
      }

   }

   public static Map<String, String> read(BasicStream __is) {
      Map<String, String> __v = new HashMap();
      int __sz0 = __is.readSize();

      for(int __i0 = 0; __i0 < __sz0; ++__i0) {
         String __key = __is.readString();
         String __value = __is.readString();
         __v.put(__key, __value);
      }

      return __v;
   }
}
