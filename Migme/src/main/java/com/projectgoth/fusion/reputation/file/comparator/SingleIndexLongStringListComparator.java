package com.projectgoth.fusion.reputation.file.comparator;

import java.util.Comparator;
import java.util.List;

public class SingleIndexLongStringListComparator implements Comparator<List<String>> {
   private int index;

   public SingleIndexLongStringListComparator(int index) {
      this.index = index;
   }

   public int compare(List<String> lhs, List<String> rhs) {
      long lhsValue = Long.parseLong((String)lhs.get(this.index));
      long rhsValue = Long.parseLong((String)rhs.get(this.index));
      if (lhsValue < rhsValue) {
         return -1;
      } else {
         return lhsValue > rhsValue ? 1 : 0;
      }
   }
}
