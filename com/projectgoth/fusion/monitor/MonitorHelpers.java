package com.projectgoth.fusion.monitor;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class MonitorHelpers {
   public static Map sortDesc(Map unsorted) {
      return sortDesc(unsorted, (Integer)null);
   }

   public static Map sortDesc(Map unsorted, Integer limit) {
      List list = new LinkedList(unsorted.entrySet());
      Collections.sort(list, new Comparator() {
         public int compare(Object o1, Object o2) {
            return ((Comparable)((Entry)((Entry)o2)).getValue()).compareTo(((Entry)((Entry)o1)).getValue());
         }
      });
      Map sorted = new LinkedHashMap();
      Iterator it = list.iterator();

      do {
         if (!it.hasNext()) {
            return sorted;
         }

         Entry entry = (Entry)it.next();
         sorted.put(entry.getKey(), entry.getValue());
      } while(limit == null || sorted.size() < limit);

      return sorted;
   }

   public static String countsMapToString(Map<String, ? extends Number> counts, Long total, DecimalFormat decFormat) {
      StringBuilder sb = new StringBuilder();
      Set<String> keys = counts.keySet();

      for(Iterator i$ = keys.iterator(); i$.hasNext(); sb.append("\n")) {
         String key = (String)i$.next();
         Number count = (Number)counts.get(key);
         sb.append(key + "=" + decFormat.format(count));
         if (total != null) {
            double percent = 100.0D * (count.doubleValue() / (double)total);
            sb.append(" (" + decFormat.format(percent) + "%)");
         }
      }

      return sb.toString();
   }
}
