package com.projectgoth.fusion.reputation.util;

import java.util.Iterator;
import java.util.List;

public abstract class MeanUtils {
   public static int meanValue(List<Integer> values) {
      if (values != null && !values.isEmpty()) {
         long total = 0L;

         Integer value;
         for(Iterator i$ = values.iterator(); i$.hasNext(); total += (long)value) {
            value = (Integer)i$.next();
            System.out.println(value);
         }

         return (int)(total / (long)values.size());
      } else {
         return 0;
      }
   }
}
