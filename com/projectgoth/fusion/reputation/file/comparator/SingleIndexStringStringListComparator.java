package com.projectgoth.fusion.reputation.file.comparator;

import com.projectgoth.fusion.common.ConfigUtils;
import java.util.Comparator;
import java.util.List;
import org.apache.log4j.Logger;

public class SingleIndexStringStringListComparator implements Comparator<List<String>> {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SingleIndexStringStringListComparator.class));
   private int index;

   public SingleIndexStringStringListComparator(int index) {
      this.index = index;
   }

   public int compare(List<String> lhs, List<String> rhs) {
      try {
         String lhsValue = (String)lhs.get(this.index);
         String rhsValue = (String)rhs.get(this.index);
         if (lhsValue == null && rhsValue != null) {
            return 1;
         } else {
            return lhsValue != null && rhsValue == null ? -1 : lhsValue.compareTo(rhsValue);
         }
      } catch (RuntimeException var5) {
         log.error("failed to compare lhs [" + lhs + "] with rhs [" + rhs + "]", var5);
         throw var5;
      }
   }
}
