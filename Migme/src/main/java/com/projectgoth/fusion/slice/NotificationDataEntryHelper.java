package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class NotificationDataEntryHelper {
   public static void write(BasicStream __os, Map<String, Map<String, String>> __v) {
      if (__v == null) {
         __os.writeSize(0);
      } else {
         __os.writeSize(__v.size());
         Iterator i$ = __v.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, Map<String, String>> __e = (Entry)i$.next();
            __os.writeString((String)__e.getKey());
            NotificationDataParamHelper.write(__os, (Map)__e.getValue());
         }
      }

   }

   public static Map<String, Map<String, String>> read(BasicStream __is) {
      Map<String, Map<String, String>> __v = new HashMap();
      int __sz0 = __is.readSize();

      for(int __i0 = 0; __i0 < __sz0; ++__i0) {
         String __key = __is.readString();
         Map<String, String> __value = NotificationDataParamHelper.read(__is);
         __v.put(__key, __value);
      }

      return __v;
   }
}
