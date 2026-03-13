package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class StringToServiceStatsLongFieldValueMapHelper {
   public static void write(BasicStream __os, Map<String, ServiceStatsLongFieldValue> __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.size());
         Iterator i$ = __v.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, ServiceStatsLongFieldValue> __e = (Entry)i$.next();
            __os.writeString((String)__e.getKey());
            ((ServiceStatsLongFieldValue)__e.getValue()).__write(__os);
         }
      }

   }

   public static Map<String, ServiceStatsLongFieldValue> read(BasicStream __is) {
      Map<String, ServiceStatsLongFieldValue> __v = new HashMap();
      int __sz0 = __is.readSize();

      for(int __i0 = 0; __i0 < __sz0; ++__i0) {
         String __key = __is.readString();
         ServiceStatsLongFieldValue __value = new ServiceStatsLongFieldValue();
         __value.__read(__is);
         __v.put(__key, __value);
      }

      return __v;
   }
}
